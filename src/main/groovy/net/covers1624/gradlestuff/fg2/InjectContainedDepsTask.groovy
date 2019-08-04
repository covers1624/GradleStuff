package net.covers1624.gradlestuff.fg2

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar

import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Created by covers1624 on 3/05/19.
 */
class InjectContainedDepsTask extends DefaultTask {
    private final def MAVEN_ARTIFACT = new Attributes.Name('Maven-Artifact')

    List<Jar> tasks
    Configuration configuration

    @TaskAction
    def doTask() {
        def tmpDir = temporaryDir
        if (tmpDir.exists()) {
            tmpDir.deleteDir()
        }

        configuration.resolvedConfiguration.resolvedArtifacts.each {
            def file = it.file
            def mavenStr = it.id.componentIdentifier.toString()
            if (!hasArtifactAttrib(file)) {
                def mf = new Manifest()
                def attributes = mf.mainAttributes
                attributes.put(Attributes.Name.MANIFEST_VERSION, '1.0')
                attributes.put(MAVEN_ARTIFACT, mavenStr)
                def f = new File(tmpDir, file.name + '.meta')
                if (!f.parentFile.exists()) {
                    f.parentFile.mkdirs()
                }
                def out = f.newOutputStream()
                mf.write(out)
                out.close()
            }
        }
        def str = configuration.collect { it.name }.join(" ")
        tasks.each {
            it.from(tmpDir) {
                rename('(.+.jar.meta)', 'META-INF/libraries/$1')
            }
            it.from(configuration) {
                rename('(.+.jar)', 'META-INF/libraries/$1')
            }
            it.manifest {
                attibutes ContainedDeps: str
            }
        }
    }

    private def hasArtifactAttrib(File file) {
        def zif = new ZipInputStream(file.newInputStream())

        ZipEntry ze
        while ((ze = zif.nextEntry) != null) {
            if (ze.name.equalsIgnoreCase(JarFile.MANIFEST_NAME)) {
                val mf = new Manifest(zif)
                if (mf.mainAttributes.containsKey(MAVEN_ARTIFACT)) {
                    zif.close()
                    return true
                }
            }
        }
        zif.close()
        return false
    }


}
