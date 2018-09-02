package net.covers1624.gradlestuff.xz

import java.io.{FileOutputStream, IOException, OutputStream}

import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream
import org.apache.commons.compress.utils.IOUtils
import org.gradle.api.internal.file.archive.TarCopyAction
import org.gradle.api.tasks.bundling.AbstractArchiveTask

/**
 * Created by covers1624 on 2/09/18.
 */
class TarXZ extends AbstractArchiveTask {
    getConventionMapping.map("extension", () => "tar.xz")

    override def createCopyAction() = new TarCopyAction(getArchivePath, destination => {
        var out: OutputStream = null
        try {
            out = new FileOutputStream(destination)
            new XZCompressorOutputStream(out)
        } catch {
            case e: IOException =>
                IOUtils.closeQuietly(out)
                throw new RuntimeException(String.format("Unable to create xz output stream for file %s.", destination), e)
        }
    }, isPreserveFileTimestamps)
}
