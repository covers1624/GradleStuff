/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.dependencies;

import net.covers1624.gradlestuff.sourceset.SourceSetDependencyPlugin;
import net.covers1624.quack.maven.MavenNotation;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Visits dependencies in a given configuration.
 * <p>
 * Created by covers1624 on 2/8/19.
 */
public interface ConfigurationVisitor {

    /**
     * Start visiting a given {@link Configuration}.
     * <p>
     * Will not be called multiple times without {@link #visitEnd()} being called.
     *
     * @param config The configuration being visited.
     */
    default void visitStart(Configuration config) { }

    /**
     * Called once per Module dependency.
     *
     * @param notation Notation describing this dependency.
     * @param classes  The classes for this notation.
     * @param sources  The sources for this notation.
     * @param javadoc  The javadoc for this notation.
     */
    default void visitModuleDependency(MavenNotation notation, File classes, @Nullable File sources, @Nullable File javadoc) { }

    /**
     * Visits a {@link SourceSet} dependency.
     * {@link SourceSetDependencyPlugin} must be enabled to receive these.
     *
     * @param ss The {@link SourceSet}.
     */
    default void visitSourceSetDependency(SourceSet ss) { }

    /**
     * Visits a {@link Project} dependency.
     *
     * @param project The {@link Project}.
     */
    default void visitProjectDependency(Project project) { }

    /**
     * Finished visiting the current {@link Configuration}.
     */
    default void visitEnd() { }
}
