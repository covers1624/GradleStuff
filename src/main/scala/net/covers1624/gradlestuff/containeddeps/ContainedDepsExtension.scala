package net.covers1624.gradlestuff.containeddeps

import org.gradle.api.Project

/**
 * Created by covers1624 on 30/04/18.
 */
class ContainedDepsExtension(@transient val plugin: ContainedDepsPlugin, @transient val project: Project) {

    var configuration: String = _
    var tasks: String = _


    def getTaskNames = tasks.split(",").map(_.trim).toList

    //Groovy bullshit.
    //@formatter:off
    def getConfiguration = configuration
    def setConfiguration(s:String) = configuration = s
    def getTasks = tasks
    def setTasks(s:String) = tasks = s
    //@formatter:on

}
