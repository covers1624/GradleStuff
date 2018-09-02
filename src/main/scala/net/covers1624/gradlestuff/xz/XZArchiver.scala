package net.covers1624.gradlestuff.xz

import java.io._
import java.net.URI

import org.apache.commons.compress.compressors.xz.{XZCompressorInputStream, XZCompressorOutputStream}
import org.apache.commons.compress.utils.IOUtils
import org.gradle.api.internal.file.archive.compression.{ArchiveOutputStreamFactory, CompressedReadableResource}
import org.gradle.api.resources.{MissingResourceException, ResourceException}

/**
 * Created by covers1624 on 2/09/18.
 */
class XZArchiver(val file:File) extends CompressedReadableResource {
    override def getBackingFile = file

    override def read: InputStream = {
        var is:InputStream = null
        try {
            is = new XZCompressorInputStream(new FileInputStream(file))
            is
        } catch {
            case e: FileNotFoundException =>
                IOUtils.closeQuietly(is)
                throw new MissingResourceException(String.format("Could not read '%s' as it does not exist.", getDisplayName), e)
            case e: IOException =>
                IOUtils.closeQuietly(is)
                throw new ResourceException(String.format("Could not read %s.", getDisplayName), e)
        }
    }

    override def getDisplayName = file.getAbsolutePath

    override def getURI = file.toURI

    override def getBaseName = file.getName
}
