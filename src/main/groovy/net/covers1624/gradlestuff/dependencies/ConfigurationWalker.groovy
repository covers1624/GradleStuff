package net.covers1624.gradlestuff.dependencies

import net.covers1624.gradlestuff.sourceset.SourceSetDependency
import org.apache.commons.lang3.StringUtils
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.component.Artifact
import org.gradle.internal.component.external.model.DefaultModuleComponentArtifactIdentifier
import org.gradle.jvm.JvmLibrary
import org.gradle.language.base.artifact.SourcesArtifact
import org.gradle.language.java.artifact.JavadocArtifact

/**
 * Created by covers1624 on 2/8/19.
 */
class ConfigurationWalker {

    private final DependencyHandler dependencyHandler

    ConfigurationWalker(DependencyHandler dependencyHandler) {
        this.dependencyHandler = dependencyHandler
    }

    def walkTree(Configuration config, ConfigurationVisitor visitor, boolean hasSources = true, boolean hasJavadoc = true, boolean forceResolveDeps = false) {
        walkTree(Collections.singletonList(config), visitor, hasSources, hasJavadoc, forceResolveDeps)
    }

    def walkTree(Collection<Configuration> configs, ConfigurationVisitor visitor, boolean hasSources = true, boolean hasJavadoc = true, boolean forceResolveDeps = false) {
        def seen = new HashSet<String>()
        def toWalk = []
        def queue = new ArrayDeque<Configuration>()
        queue.addAll(configs)
        while (!queue.empty) {
            def now = queue.pop()
            if (seen.add(now.name)) {
                queue.addAll(now.extendsFrom)
                toWalk << now
            }
        }
        walk(toWalk, visitor, hasSources, hasJavadoc, forceResolveDeps)
    }


    def walk(Collection<Configuration> configurations, ConfigurationVisitor visitor, boolean hasSources = true, boolean hasJavadoc = true, boolean forceResolveDeps = false) {
        def extraArtifacts = []
        if (hasSources) extraArtifacts << SourcesArtifact
        if (hasJavadoc) extraArtifacts << JavadocArtifact

        configurations.each { config ->
            visitor.startVisit(config)
            if (config.canBeResolved || forceResolveDeps) {
                Map<ComponentArtifactIdentifier, ResolvedArtifactResult> resolvedArtifacts = [:]
                Table<ComponentIdentifier, Class<? extends Artifact>, Set<ResolvedArtifactResult>> resolvedAuxiliary = new Table()
                config.dependencies.each {
                    if (it instanceof SourceSetDependency) {
                        visitor.visitSourceSetDependency(it.sourceSet)
                    }
                }
                if (config.canBeResolved) {
                    config.copy().incoming.artifactView({ it.lenient = true }).artifacts.each {
                        resolvedArtifacts.put(it.id, it)
                    }
                    def components = resolvedArtifacts.keySet()
                            .findAll { it.componentIdentifier instanceof ModuleComponentIdentifier }
                            .collect { it.componentIdentifier as ModuleComponentIdentifier }
                    if (!extraArtifacts.empty) {
                        def result = dependencyHandler.createArtifactResolutionQuery()
                                .forComponents(components)
                                .withArtifacts(JvmLibrary, extraArtifacts)
                                .execute()
                        result.resolvedComponents.each { ar ->
                            extraArtifacts.each { type ->
                                ar.getArtifacts(type).each { artifact ->
                                    if (artifact instanceof ResolvedArtifactResult) {
                                        resolvedAuxiliary.computeIfAbsent(ar.id, type, { new HashSet() }) << artifact
                                    }
                                }
                            }
                        }
                    }
                    resolvedArtifacts.each { key, value ->
                        def componentIdentifier = value.id.componentIdentifier
                        if (componentIdentifier instanceof ModuleComponentIdentifier) {
                            def moduleIdentifier = componentIdentifier as ModuleComponentIdentifier
                            File classes = value.file.absoluteFile
                            def sourcesArtifacts = resolvedAuxiliary.getOrDefault(componentIdentifier, SourcesArtifact, Collections.emptySet())
                            def javadocArtifacts = resolvedAuxiliary.getOrDefault(componentIdentifier, JavadocArtifact, Collections.emptySet())
                            def depName = DependencyName.parseMaven(moduleIdentifier.toString())
                            if (value.id instanceof DefaultModuleComponentArtifactIdentifier) {
                                def id = value.id as DefaultModuleComponentArtifactIdentifier
                                def ivyName = id.name
                                depName.classifier = ivyName.classifier
                                depName.extension = StringUtils.isNotEmpty(ivyName.extension) ? ivyName.extension : 'jar'
                            }
                            def sources = sourcesArtifacts.empty ? null : sourcesArtifacts.first().file.absoluteFile
                            def javadoc = javadocArtifacts.empty ? null : javadocArtifacts.first().file.absoluteFile
                            visitor.visitModuleDependency(depName, classes, sources, javadoc)
                        }
                    }
                }
            }
            visitor.endVisit()
        }
    }

    private class Table<R, C, V> {
        private Map<R, Map<C, V>> table = [:]

        def put(R r, C c, V v) {
            def row = getRow(r)
            row[c] = v
        }

        V computeIfAbsent(R r, C c, Closure<V> v) {
            def row = getRow(r)
            return row.computeIfAbsent(c, { v.call() })
        }

        V getOrDefault(R r, C c, V default_) {
            return (table[r] ?: Collections.emptyMap())[c] ?: default_
        }

        V getRow(R r) {
            return table.computeIfAbsent(r, { [:] })
        }
    }
}
