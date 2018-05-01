package net.covers1624.gradlestuff.containeddeps

import net.covers1624.gradlestuff.util.JavaImplicits._
import org.gradle.api.specs.Spec
import org.gradle.api.{Plugin, Project}
import org.gradle.jvm.tasks.Jar

/**
 * This plugin adds smart ContainedDeps support to ForgeGradle based gradle projects.
 * Created by covers1624 on 30/04/18.
 */
class ContainedDepsPlugin extends Plugin[Project] {

    var extension: ContainedDepsExtension = _
    var project: Project = _

    override def apply(project: Project) {
        this.project = project
        if (!project.getPlugins.hasPlugin("net.minecraftforge.gradle.forge")) {
            throw new IllegalStateException("ContainedDeps plugin must be loaded after Forge's plugin(net.minecraftforge.gradle.forge).")
        }
        extension = project.getExtensions.create("containedDeps", classOf[ContainedDepsExtension], this, project)
        project.afterEvaluate(_ => afterEvaluate)
    }


    def afterEvaluate() {
        if(project.getConfigurations.findByName(extension.configuration) == null) {
            throw new IllegalStateException(s"Configuration '${extension.configuration}' does not exist in the project.")
        }
        for(task <- extension.getTaskNames) {
            if(!project.hasTask(task)) {
                throw new IllegalStateException(s"Task '$task' does not exist in the project.")
            } else if(!project.getTask(task).get.isInstanceOf[Jar]) {
                throw new IllegalStateException(s"Task '$task' is not a 'Jar' task.")
            }
        }
        val makeLibTask = project.makeTask("makeLibraryMetas", classOf[MakeLibraryMetasTask])
        makeLibTask.project = project
        makeLibTask.tasks = extension.getTaskNames.map(project.getTask(_).get.asInstanceOf[Jar])
        makeLibTask.configuration = project.getConfigurations.findByName(extension.getConfiguration)
        makeLibTask.tasks.foreach(_.dependsOn(makeLibTask))
        makeLibTask.getOutputs.upToDateWhen(_ => false)
    }
}
