/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.sourceset

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.internal.HasConvention
import org.gradle.api.internal.artifacts.DefaultDependencyFactory
import org.gradle.api.internal.artifacts.dsl.dependencies.DefaultDependencyHandler
import org.gradle.api.internal.artifacts.dsl.dependencies.DependencyFactory
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.typeconversion.CompositeNotationConverter
import org.gradle.internal.typeconversion.NotationConverter
import org.gradle.internal.typeconversion.NotationConverterToNotationParserAdapter
import org.gradle.internal.typeconversion.NotationParser
import sun.misc.Unsafe

import java.lang.reflect.Field

/**
 * Created by covers1624 on 2/8/19.
 */
class SourceSetDependencyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def pluginConvention = project.convention.findPlugin(JavaPluginConvention)
        if(pluginConvention != null) {
            injectSourceSetDependencyConverter(project)
            pluginConvention.sourceSets.all { SourceSet ss ->
                def output = ss.output
                def convention = (output as HasConvention).convention
                convention.create(SourceAwareOutputExtension, "sourceSet", DefaultSourceAwareOutputExtension, ss)
            }
        }
    }

    //region Reflection Hax
    //This was originally written in java for use in WorkspaceTool, probably a cleaner groovy impl around.
    private static final Unsafe unsafe
    private static final Field f_dependencyFactory
    private static final Field f_dependencyNotationParser
    private static final Field f_notationParserDelegate
    private static final Field f_notationConverterDelegate
    private static final int f_notationConverterDelegateOffset

    static {
        try {
            Field f_theUnsafe = Unsafe.class.getDeclaredField("theUnsafe")
            f_theUnsafe.setAccessible(true)
            unsafe = f_theUnsafe.get(null)
            f_dependencyFactory = DefaultDependencyHandler.class.getDeclaredField("dependencyFactory")
            f_dependencyNotationParser = DefaultDependencyFactory.class.getDeclaredField("dependencyNotationParser")
            f_notationParserDelegate = Class.forName("org.gradle.internal.typeconversion.ErrorHandlingNotationParser").getDeclaredField("delegate")
            f_notationConverterDelegate = NotationConverterToNotationParserAdapter.class.getDeclaredField("converter")

            f_dependencyFactory.setAccessible(true)
            f_dependencyNotationParser.setAccessible(true)
            f_notationParserDelegate.setAccessible(true)
            f_notationConverterDelegate.setAccessible(true)
            f_notationConverterDelegateOffset = unsafe.objectFieldOffset(f_notationConverterDelegate)
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * This black magic does a lot of reflection and injects a NotationConverter into DependencyFactory.
     * As of writing this there is currently no api for adding NotationConverters so these hacks exist.
     */
    private def injectSourceSetDependencyConverter(Project project) {
        ProjectInternal projectInternal = (ProjectInternal) project
        Instantiator instantiator = projectInternal.getServices().get(Instantiator.class)
        DependencyHandler handler = project.getDependencies()
        DependencyFactory factory = f_dependencyFactory.get(handler)
        NotationParser notationParser = f_dependencyNotationParser.get(factory)
        NotationParser delegateParser = f_notationParserDelegate.get(notationParser)

        while (true) {
            if (!(delegateParser instanceof NotationConverterToNotationParserAdapter)) {
                if (!delegateParser.class.name.endsWith(".JustReturningParser")) {
                    throw new RuntimeException("Unexpected NotationParser type ${delegateParser.class.name}")
                }
                Field f = delegateParser.class.getDeclaredField("delegate")
                f.setAccessible(true)
                delegateParser = f.get(delegateParser)
                continue
            }
            break
        }

        List<NotationConverter> converters = new LinkedList<>()
        converters.add(new SourceSetOutputNotationConverter(instantiator))
        converters.add(f_notationConverterDelegate.get(delegateParser))
        unsafe.putObject(delegateParser, f_notationConverterDelegateOffset, new CompositeNotationConverter(converters))
    }
    //endregion
}
