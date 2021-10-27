/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.xz

import net.covers1624.quack.annotation.Requires
import org.gradle.api.internal.file.archive.TarCopyAction
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.XZOutputStream

/**
 * Created by covers1624 on 3/05/19.
 */
@Requires("org.tukaani:xz")
class TarXZ extends AbstractArchiveTask {

    TarXZ() {
        conventionMapping("extension", { "tar.xz" })
    }

    @Override
    protected CopyAction createCopyAction() {
        return new TarCopyAction(archivePath, { dest ->
            OutputStream out
            try {
                out = dest.newOutputStream()
                return new XZOutputStream(out, new LZMA2Options())
            } catch (IOException e) {
                closeQuietly(out)
                throw new RuntimeException(String.format("Unable to create xz output stream for file %s.", destination), e)
            }
        }, preserveFileTimestamps)
    }

    static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (Throwable ignored) {
            }
        }
    }
}
