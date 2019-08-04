package net.covers1624.gradlestuff.dependencies

import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSet

/**
 * Created by covers1624 on 2/8/19.
 */
interface ConfigurationVisitor {

    void startVisit(Configuration configuration)

    void visitModuleDependency(DependencyName name, File classes, File sources, File javadoc);

    void visitSourceSetDependency(SourceSet ss)

    void endVisit()
}
