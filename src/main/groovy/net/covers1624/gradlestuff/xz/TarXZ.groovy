package net.covers1624.gradlestuff.xz

import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream
import org.apache.commons.io.IOUtils
import org.gradle.api.internal.file.archive.TarCopyAction
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.tasks.bundling.AbstractArchiveTask

/**
 * Created by covers1624 on 3/05/19.
 */
class TarXZ extends AbstractArchiveTask {

    TarXZ() {
        conventionMapping("extension", { "tar.xz" })
    }

    @Override
    protected CopyAction createCopyAction() {
        return new TarCopyAction(archivePath, { dest ->
            OutputStream out
            try {
                return new XZCompressorOutputStream(out = dest.newOutputStream())
            } catch (IOException e) {
                IOUtils.closeQuietly(out)
                throw new RuntimeException(String.format("Unable to create xz output stream for file %s.", destination), e)
            }
        }, preserveFileTimestamps)
    }
}
