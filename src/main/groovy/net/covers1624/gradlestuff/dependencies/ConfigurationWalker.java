/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.dependencies;

import net.covers1624.gradlestuff.sourceset.SourceSetDependency;
import net.covers1624.quack.collection.ColUtils;
import net.covers1624.quack.maven.MavenNotation;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.result.ArtifactResolutionResult;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.component.Artifact;
import org.gradle.internal.component.external.model.DefaultModuleComponentArtifactIdentifier;
import org.gradle.internal.component.model.IvyArtifactName;
import org.gradle.jvm.JvmLibrary;
import org.gradle.language.base.artifact.SourcesArtifact;
import org.gradle.language.java.artifact.JavadocArtifact;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.*;

/**
 * Created by covers1624 on 26/10/21.
 */
public class ConfigurationWalker {

    private final DependencyHandler dependencyHandler;

    public ConfigurationWalker(DependencyHandler dependencyHandler) {
        this.dependencyHandler = dependencyHandler;
    }

    public void walkTree(Configuration config, ConfigurationVisitor visitor, ResolveOptions... options) {
        walkTree(singleton(config), visitor, options);
    }

    public void walkTree(Iterable<Configuration> configs, ConfigurationVisitor visitor, ResolveOptions... options) {
        Set<String> seen = new HashSet<>();
        LinkedList<Configuration> queue = new LinkedList<>();
        configs.forEach(queue::push);
        List<Configuration> toWalk = new LinkedList<>();
        while (!queue.isEmpty()) {
            Configuration config = queue.pop();
            if (seen.add(config.getName())) {
                queue.addAll(config.getExtendsFrom());
                toWalk.add(config);
            }
        }
        walk(toWalk, visitor, options);
    }

    public void walk(Iterable<Configuration> configs, ConfigurationVisitor visitor, ResolveOptions... options) {
        Set<ResolveOptions> optSet = new HashSet<>();
        Collections.addAll(optSet, options);
        List<Class<? extends Artifact>> extraArtifacts = optSet.stream()
                .map(e -> e.artifact)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (Configuration config : configs) {
            visitor.visitStart(config);
            if (config.isCanBeResolved() || optSet.contains(ResolveOptions.FORCE)) {
                config = config.copy();
                if (!config.isCanBeResolved()) {
                    config.setCanBeResolved(true);
                    config.setCanBeConsumed(true);
                }
                Map<ComponentArtifactIdentifier, ResolvedArtifactResult> resolvedArtifacts = new HashMap<>();
                Map<ComponentIdentifier, Map<Class<? extends Artifact>, Set<ResolvedArtifactResult>>> resolvedAuxiliary = new HashMap<>();
                for (Dependency dependency : config.getAllDependencies()) {
                    if (dependency instanceof SourceSetDependency) {
                        visitor.visitSourceSetDependency(((SourceSetDependency) dependency).getSourceSet());
                    } else if (dependency instanceof ProjectDependency) {
                        visitor.visitProjectDependency(((ProjectDependency) dependency).getDependencyProject());
                    }
                }
                boolean lenient = optSet.contains(ResolveOptions.LENIENT);
                for (ResolvedArtifactResult artifact : config.getIncoming().artifactView(e -> e.setLenient(lenient)).getArtifacts()) {
                    resolvedArtifacts.put(artifact.getId(), artifact);
                }
                List<ModuleComponentIdentifier> components = resolvedArtifacts.keySet().stream()
                        .filter(e -> e.getComponentIdentifier() instanceof ModuleComponentIdentifier)
                        .map(e -> (ModuleComponentIdentifier) e.getComponentIdentifier())
                        .collect(Collectors.toList());
                if (!extraArtifacts.isEmpty()) {
                    ArtifactResolutionResult result = dependencyHandler.createArtifactResolutionQuery()
                            .forComponents(components)
                            .withArtifacts(JvmLibrary.class, extraArtifacts)
                            .execute();
                    for (ComponentArtifactsResult ar : result.getResolvedComponents()) {
                        for (Class<? extends Artifact> type : extraArtifacts) {
                            for (ArtifactResult artifact : ar.getArtifacts(type)) {
                                if (artifact instanceof ResolvedArtifactResult) {
                                    resolvedAuxiliary.computeIfAbsent(ar.getId(), e -> new HashMap<>())
                                            .computeIfAbsent(type, e -> new HashSet<>())
                                            .add((ResolvedArtifactResult) artifact);
                                }
                            }
                        }
                    }
                }
                for (Map.Entry<ComponentArtifactIdentifier, ResolvedArtifactResult> entry : resolvedArtifacts.entrySet()) {
                    ResolvedArtifactResult value = entry.getValue();
                    ComponentIdentifier componentIdentifier = value.getId().getComponentIdentifier();
                    if (!(componentIdentifier instanceof ModuleComponentIdentifier)) continue;
                    ModuleComponentIdentifier moduleIdentifier = (ModuleComponentIdentifier) componentIdentifier;
                    Set<ResolvedArtifactResult> sourceArtifacts = resolvedAuxiliary
                            .getOrDefault(componentIdentifier, emptyMap())
                            .getOrDefault(SourcesArtifact.class, emptySet());
                    Set<ResolvedArtifactResult> javadocArtifacts = resolvedAuxiliary
                            .getOrDefault(componentIdentifier, emptyMap())
                            .getOrDefault(JavadocArtifact.class, emptySet());
                    MavenNotation notation = MavenNotation.parse(moduleIdentifier.toString());
                    if (value.getId() instanceof DefaultModuleComponentArtifactIdentifier) {
                        DefaultModuleComponentArtifactIdentifier id = (DefaultModuleComponentArtifactIdentifier) value.getId();
                        IvyArtifactName ivyName = id.getName();
                        notation = notation.withClassifier(ivyName.getClassifier());
                        notation = notation.withExtension(isEmpty(ivyName.getExtension()) ? "jar" : ivyName.getExtension());
                    }
                    File classes = value.getFile().getAbsoluteFile();
                    File sources = ColUtils.headOption(sourceArtifacts).map(e -> e.getFile().getAbsoluteFile()).orElse(null);
                    File javadoc = ColUtils.headOption(javadocArtifacts).map(e -> e.getFile().getAbsoluteFile()).orElse(null);
                    visitor.visitModuleDependency(notation, classes, sources, javadoc);
                }
            }
            visitor.visitEnd();
        }
    }

    private static boolean isEmpty(@Nullable CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public enum ResolveOptions {
        FORCE,
        LENIENT,
        SOURCES(SourcesArtifact.class),
        JAVADOC(JavadocArtifact.class);

        @Nullable
        public final Class<? extends Artifact> artifact;

        ResolveOptions() {
            this(null);
        }

        ResolveOptions(@Nullable Class<? extends Artifact> artifact) {
            this.artifact = artifact;
        }
    }
}
