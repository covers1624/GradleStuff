/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.xz

import net.covers1624.quack.annotation.Requires
import org.gradle.api.internal.file.archive.compression.CompressedReadableResource
import org.gradle.api.resources.MissingResourceException
import org.gradle.api.resources.ResourceException
import org.tukaani.xz.SingleXZInputStream

/**
 * Created by covers1624 on 3/05/19.
 */
@Requires("org.tukaani:xz")
class XZArchiver implements CompressedReadableResource {

    private final File file

    XZArchiver(File file) {
        this.file = file
    }

    @Override
    File getBackingFile() {
        return file
    }

    @Override
    InputStream read() throws MissingResourceException, ResourceException {
        InputStream is
        try {
            return is = new SingleXZInputStream(file.newInputStream())
        } catch (FileNotFoundException e) {
            TarXZ.closeQuietly(is)
            throw new MissingResourceException(String.format("Could not read '%s' as it does not exist.", getDisplayName()), e)
        } catch (IOException e) {
            TarXZ.closeQuietly(is)
            throw new ResourceException(String.format("Could not read %s.", getDisplayName()), e)
        }
        return null
    }

    @Override
    String getDisplayName() {
        return file.absolutePath
    }

    @Override
    URI getURI() {
        return file.toURI()
    }

    @Override
    String getBaseName() {
        return file.name
    }
}
