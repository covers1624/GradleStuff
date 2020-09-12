package net.covers1624.gradlestuff.dependencies

import org.gradle.api.artifacts.*
import org.gradle.api.publish.maven.MavenPublication

/**
 * Created by covers1624 on 9/9/20.
 */
class PublicationUtils {

    /**
     * Adds all Dependencies of a Configuration to a MavenPublication
     *
     * @param publication
     * @param configuration
     * @param transitive
     * @param scope
     * @return
     */
    def addDeps(MavenPublication publication, Configuration configuration, boolean transitive = true, String scope = 'compile') {
        publication.pom { pom ->
            pom.withXml { xml ->
                def rootNode = xml.asNode().appendNode('dependencies')

                DependencySet deps = transitive ? configuration.allDependencies : configuration.dependencies
                deps.each {
                    if (it instanceof ModuleDependency) {
                        ModuleDependency moduleDep = (ModuleDependency) it
                        Set<DependencyArtifact> artifacts = moduleDep.getArtifacts()
                        if (!artifacts.empty) {
                            artifacts.each {
                                def node = addDependency(rootNode, moduleDep, scope)
                                if (it.classifier) {
                                    node.appendNode('classifier', it.classifier)
                                }
                                if (it.type && it.type != DependencyArtifact.DEFAULT_TYPE) {
                                    node.appendNode('type', it.type)
                                }
                            }
                        } else {
                            addDependency(rootNode, moduleDep, scope)
                        }
                    } else if (it instanceof ProjectDependency || !(it instanceof SelfResolvingDependency)) {
                        addDependency(rootNode, it, scope)
                    }
                }
            }
        }
    }

    def addDependency(Node rootNode, Dependency dep, String scope) {
        def node = rootNode.appendNode('dependency')
        if (!dep.version || dep.version.empty) {
            throw new IllegalArgumentException("Empty version for dependency '${dep}'")
        }
        if (dep.version.endsWith("+")) {
            throw new IllegalArgumentException("Incomplete version for dependency '${dep}'")
        }
        node.appendNode('groupId', dep.group)
        node.appendNode('artifactId', dep.name)
        node.appendNode('version', dep.version)
        node.appendNode('scope', scope)
        return node
    }
}
