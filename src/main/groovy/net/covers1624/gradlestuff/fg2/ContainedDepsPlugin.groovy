package net.covers1624.gradlestuff.fg2


import org.gradle.api.GradleScriptException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by covers1624 on 3/05/19.
 */
@SuppressWarnings("UnstableApiUsage")
class ContainedDepsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin("net.minecraftforge.gradle.forge")) {
            throw new GradleScriptException("ContainedDeps plugin must be loaded after Forge. 'net.minecraftforge.gradle.forge'")
        }

        def extension = project.extensions.create("containedDeps", DepsExtension, project)

        project.afterEvaluate {
            def tasks = extension.tasks
            def configuration = extension.configuration

            def injectTask = project.tasks.register("injectContainedDeps", InjectContainedDepsTask)
            injectTask.configure {
                it.configuration = configuration
                it.tasks = tasks
                it.outputs.upToDateWhen { false }
            }
            tasks.each { it.dependsOn(injectTask) }
        }
    }
}
