package net.covers1624.gradlestuff.repackeddeps

import org.gradle.api.Project

class RepackedDepsExtension(@transient project: Project) {

    var configuration: String = _
    var tasks: String = _
    @transient
    var extraLines = Set.empty[String]

    def getTaskNames = tasks.split(",").map(_.trim).toList

    def addExtraSrgLine(line: String) = extraLines += line

    //Groovy bullshit.
    //@formatter:off
    def getConfiguration = configuration
    def setConfiguration(s:String) = configuration = s
    def getTasks = tasks
    def setTasks(s:String) = tasks = s
    //@formatter:on
}
