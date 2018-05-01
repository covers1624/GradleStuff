package net.covers1624.gradlestuff.util

import java.io._

import org.gradle.api.{NamedDomainObjectCollection, Project, Task}

import scala.io.Source
import scala.collection.JavaConverters._

/**
 * Created by covers1624 on 4/02/2018.
 */
object JavaImplicits {

    implicit class IS(self: InputStream) {
        def toBytes: Array[Byte] = {
            val buffer: Array[Byte] = Array.ofDim(16384)
            Stream.continually(buffer.slice(0, self.read(buffer))).takeWhile(!_.isEmpty).flatten.toArray
        }
    }

    implicit class F(val self: File) {
        def toBytes: Array[Byte] = {
            val is = self.in
            val bytes = is.toBytes
            is.closeQuietly()
            bytes
        }

        def out: OutputStream = new FileOutputStream(self)

        def in: InputStream = new FileInputStream(self)

        def copyFrom(other: File) {
            val is = other.in
            val os = self.makeFile.out
            os.write(is.toBytes)
            is.closeQuietly()
            os.closeQuietly()
        }

        def write(out: OutputStream => Unit) {
            val os = self.makeFile.out
            out(os)
            os.closeQuietly()
        }

        def write(bytes: Array[Byte]) {
            val os = self.makeFile.out
            os.write(bytes)
            os.closeQuietly()
        }

        def makeFile: File = {
            if (!self.exists()) {
                if (!self.getParentFile.exists) {
                    self.getParentFile.mkdirs()
                }
                self.createNewFile()
            }
            self
        }

        def makeDirs: File = {
            if (self.exists() && self.isDirectory) {
                self
            } else if (!self.mkdirs()) {
                throw new RuntimeException(s"Cannot create directory '$self'")
            } else {
                self
            }
        }

        def delContent() {
            val l = self.list()
            if (l != null) {
                for (l <- l.map(self.child)) {
                    if (l.isDirectory) {
                        l.delContent()
                    } else {
                        l.delete()
                    }
                }
            }
        }

        def getLines = Source.fromFile(self).getLines().toList

        def child(child: String) = new File(self, child)

        def relative(other: File): String = self.toPath.relativize(other.toPath).toString

        def nameNoExt: String = {
            if (self.getName.contains(".")) {
                self.getName.substring(0, self.getName.lastIndexOf("."))
            } else {
                self.getName
            }
        }

        def walkFiles(p: File => Unit) {
            if (self.isDirectory) {
                Option(self.listFiles()) match {
                    case Some(v) =>
                        for (f <- v) {
                            f.walkFiles(p)
                        }
                    case None =>
                }
            } else {
                p(self)
            }
        }
    }

    implicit class Close(val self: Closeable) {
        def closeQuietly(): Unit = {
            try {
                self.close()
            } catch {
                case _: Throwable =>
            }
        }
    }

    implicit class Proj(val self: Project) {
        def getTask(name: String, recursive:Boolean = false) = self.getTasksByName(name, recursive).asScala.headOption
        def hasTask(name: String, recursive:Boolean = false): Boolean = getTask(name, recursive).nonEmpty
        def makeTask[T <: Task](name:String, clazz:Class[T]) = self.getTasks.create(name, clazz)
    }

}
