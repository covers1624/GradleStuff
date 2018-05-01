package net.covers1624.gradlestuff.containeddeps

import java.io.{File, FileInputStream}
import java.util.jar.{Attributes, JarFile, Manifest}
import java.util.zip.ZipInputStream

import net.covers1624.gradlestuff.util.JavaImplicits._
import org.gradle.api.{Action, DefaultTask, Project}
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.{OutputDirectory, TaskAction}
import org.gradle.jvm.tasks.Jar
import MakeLibraryMetasTask._
import net.covers1624.gradlestuff.util.BsWrapper._
import org.gradle.api.file.FileCopyDetails

import scala.collection.JavaConverters._


/**
 * Created by covers1624 on 1/05/18.
 */
class MakeLibraryMetasTask extends DefaultTask {

    var tasks: List[Jar] = _
    var configuration: Configuration = _
    var project:Project = _

    @TaskAction
    def doTask() {
        val temp = getTemporaryDirFactory.create()
        if (temp.exists()) {
            temp.delContent()
        }
        configuration.getResolvedConfiguration.getResolvedArtifacts.forEach(dep => {
            val file = dep.getFile
            val maven_artifact = dep.getId.getComponentIdentifier.toString
            if(!hasMavenArtifactAttrib(project, file)) {
                val manifest = new Manifest()
                val attributes = manifest.getMainAttributes
                attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0")
                attributes.put(MAVEN_ARTIFACT, maven_artifact)
                temp.child(file.getName + ".meta").write(manifest.write _)
            }
        })
        val str = configuration.asScala.map(_.getName).mkString(" ")
        tasks.foreach(from(_, temp, s => s.rename("(.+.jar.meta)", "META-INF/libraries/$1")))
        tasks.foreach(from(_, configuration, s => s.rename("(.+.jar)", "META-INF/libraries/$1")))
        tasks.foreach(_.getManifest.getAttributes.put("ContainedDeps", str))
    }

}

object MakeLibraryMetasTask {
    val MAVEN_ARTIFACT = new Attributes.Name("Maven-Artifact")

    def hasMavenArtifactAttrib(project:Project, file: File): Boolean = {
        val zi = new ZipInputStream(new FileInputStream(file))

        var ze = zi.getNextEntry
        do {
            if (ze.getName.equalsIgnoreCase(JarFile.MANIFEST_NAME)) {
                val manifest = new Manifest(zi)
                if (manifest.getMainAttributes.containsKey(MAVEN_ARTIFACT)) {
                    println("SKIP DA SHIT")
                    zi.closeQuietly()
                    return true
                }
            }
            ze = zi.getNextEntry
        } while (ze != null)
        zi.closeQuietly()
        false
    }
}
