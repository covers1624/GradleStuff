package net.covers1624.gradlestuff.repackeddeps

import net.covers1624.gradlestuff.util.JavaImplicits._
import net.covers1624.gradlestuff.util.Utils._
import net.minecraftforge.gradle.user.{TaskSingleReobf, UserConstants}
import org.gradle.api.{Plugin, Project}
import org.gradle.jvm.tasks.Jar

import scala.collection.JavaConverters._

/**
 * Created by covers1624 on 1/05/18.
 */
class RepackedDepsPlugin extends Plugin[Project] {

    var extension: RepackedDepsExtension = _
    var project: Project = _

    override def apply(project: Project) {
        this.project = project
        if (!project.getPlugins.hasPlugin("net.minecraftforge.gradle.forge")) {
            throw new IllegalStateException("ContainedDeps plugin must be loaded after Forge's plugin(net.minecraftforge.gradle.forge).")
        }
        extension = project.getExtensions.create("repackedDeps", classOf[RepackedDepsExtension], project)
        project.afterEvaluate(_ => afterEvaluate)
    }


    def afterEvaluate() {
        project.getLogger.lifecycle("Loading RepackedDeps plugin!")
        if (project.getConfigurations.findByName(extension.configuration) == null) {
            throw new IllegalStateException(s"Configuration '${extension.configuration}' does not exist in the project.")
        }
        val task = project.getTask(UserConstants.TASK_REOBF).get.asInstanceOf[TaskSingleReobf]
        task.addExtraSrgLines(extension.extraLines.asJavaCollection)
        for (task <- extension.getTaskNames) {
            if (!project.hasTask(task)) {
                throw new IllegalStateException(s"Task '$task' does not exist in the project.")
            } else if (!project.getTask(task).get.isInstanceOf[Jar]) {
                throw new IllegalStateException(s"Task '$task' is not a 'Jar' task.")
            }
        }
        val configuration = project.getConfigurations.findByName(extension.getConfiguration)
        val tasks = extension.getTaskNames.map(project.getTask(_).get.asInstanceOf[Jar])
        tasks.foreach(t => configuration.forEach(f => from(t, project.zipTree(f), s => s.exclude("META-INF", "META-INF/**"))))
    }


}
