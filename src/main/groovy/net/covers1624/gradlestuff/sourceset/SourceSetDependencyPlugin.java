/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.sourceset;

import net.covers1624.quack.util.SneakyUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.internal.HasConvention;
import org.gradle.api.internal.artifacts.DefaultDependencyFactory;
import org.gradle.api.internal.artifacts.dsl.dependencies.DefaultDependencyHandler;
import org.gradle.api.internal.artifacts.dsl.dependencies.DependencyFactory;
import org.gradle.api.internal.notations.DependencyNotationParser;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.typeconversion.CompositeNotationConverter;
import org.gradle.internal.typeconversion.NotationConverter;
import org.gradle.internal.typeconversion.NotationConverterToNotationParserAdapter;
import org.gradle.internal.typeconversion.NotationParser;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 2/8/19.
 */
public class SourceSetDependencyPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        JavaPluginConvention pluginConvention = project.getConvention().findPlugin(JavaPluginConvention.class);
        if (pluginConvention != null) {
            SneakyUtils.sneaky(() -> injectSourceSetDependencyConverter(project));
            pluginConvention.getSourceSets().all(ss -> {
                SourceSetOutput output = ss.getOutput();
                Convention convention = ((HasConvention) output).getConvention();
                convention.create(SourceAwareOutputExtension.class, "sourceSet", DefaultSourceAwareOutputExtension.class, ss);
            });
        }
    }

    //region Reflection Hax
    private static final Unsafe unsafe;
    private static final Field f_dependencyFactory;
    private static final Field f_dependencyNotationParser;
    private static final Field f_notationParserDelegate;
    private static final Field f_notationConverterDelegate;
    private static final Field f_notationParser;
    private static final long f_notationConverterDelegateOffset;

    static {
        try {
            Field f_theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            f_theUnsafe.setAccessible(true);
            unsafe = (Unsafe) f_theUnsafe.get(null);
            f_dependencyFactory = DefaultDependencyHandler.class.getDeclaredField("dependencyFactory");
            f_dependencyNotationParser = DefaultDependencyFactory.class.getDeclaredField("dependencyNotationParser");
            f_notationParserDelegate = Class.forName("org.gradle.internal.typeconversion.ErrorHandlingNotationParser").getDeclaredField("delegate");
            f_notationConverterDelegate = NotationConverterToNotationParserAdapter.class.getDeclaredField("converter");

            // In Gradle 8, DependencyNotationParser.create went from directly returning the composite chain, to returning itself
            Field notationParserField;
            try {
                notationParserField = DependencyNotationParser.class.getDeclaredField("notationParser");
                notationParserField.setAccessible(true);
            } catch (Throwable ignored) {
                notationParserField = null;
            }
            f_notationParser = notationParserField;

            f_dependencyFactory.setAccessible(true);
            f_dependencyNotationParser.setAccessible(true);
            f_notationParserDelegate.setAccessible(true);
            f_notationConverterDelegate.setAccessible(true);
            f_notationConverterDelegateOffset = unsafe.objectFieldOffset(f_notationConverterDelegate);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * This black magic does a lot of reflection and injects a NotationConverter into DependencyFactory.
     * As of writing this there is currently no api for adding NotationConverters so these hacks exist.
     */
    @SuppressWarnings ({ "rawtypes", "unchecked" })
    private void injectSourceSetDependencyConverter(Project project) throws Throwable {
        ProjectInternal projectInternal = (ProjectInternal) project;
        Instantiator instantiator = projectInternal.getServices().get(Instantiator.class);
        DependencyHandler handler = project.getDependencies();
        Object factory = f_dependencyFactory.get(handler);
        Object notationParser = f_dependencyNotationParser.get(factory);
        if (f_notationParser != null) {
            notationParser = f_notationParser.get(notationParser);
        }
        NotationParser delegateParser = unsafeCast(f_notationParserDelegate.get(notationParser));

        while (true) {
            if (!(delegateParser instanceof NotationConverterToNotationParserAdapter)) {
                if (!delegateParser.getClass().getName().endsWith(".JustReturningParser")) {
                    throw new RuntimeException("Unexpected NotationParser type ${delegateParser.class.name}");
                }
                Field f = delegateParser.getClass().getDeclaredField("delegate");
                f.setAccessible(true);
                delegateParser = unsafeCast(f.get(delegateParser));
                continue;
            }
            break;
        }

        List<NotationConverter> converters = new LinkedList<>();
        converters.add(new SourceSetOutputNotationConverter(instantiator));
        converters.add(unsafeCast(f_notationConverterDelegate.get(delegateParser)));
        unsafe.putObject(delegateParser, f_notationConverterDelegateOffset, new CompositeNotationConverter(converters));
    }
    //endregion
}
