package net.covers1624.gradlestuff.repackeddeps

import org.gradle.api.Project

import scala.beans.BeanProperty

class RepackedDepsExtension(@transient project: Project) {

    @BeanProperty
    var configuration: String = _
    @BeanProperty
    var tasks: String = _
    @transient
    var extraLines = Set.empty[String]

    def getTaskNames = tasks.split(",").map(_.trim).toList

    def addExtraSrgLine(line: String) = extraLines += line
}
