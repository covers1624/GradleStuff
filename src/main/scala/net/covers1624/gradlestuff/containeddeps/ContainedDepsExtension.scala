package net.covers1624.gradlestuff.containeddeps

import org.gradle.api.Project

import scala.beans.BeanProperty

/**
 * Created by covers1624 on 30/04/18.
 */
class ContainedDepsExtension(@transient val plugin: ContainedDepsPlugin, @transient val project: Project) {

    @BeanProperty
    var configuration: String = _
    @BeanProperty
    var tasks: String = _


    def getTaskNames = tasks.split(",").map(_.trim).toList
}
