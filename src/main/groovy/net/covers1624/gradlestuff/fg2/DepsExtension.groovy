/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.fg2

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar

/**
 * Created by covers1624 on 3/05/19.
 */
class DepsExtension {
    private final Project project

    DepsExtension(Project project) {
        this.project = project
    }

    Object configuration

    List<Object> tasks = []

    Configuration getConfiguration() {
        if (configuration == null) {
            return null
        } else if (configuration instanceof Configuration) {
            return configuration
        } else if (configuration instanceof CharSequence) {
            return project.configurations.findByName(configuration.toString())
        } else {
            throw new RuntimeException("Configuration must be set to a String or Configuration.")
        }
    }

    List<Jar> getTasks() {
        List<Jar> jTasks = []
        tasks.each {
            if (it instanceof Jar) {
                jTasks << it
            } else if (it instanceof TaskProvider) {
                def ret = it.get()
                if (ret instanceof Jar) {
                    jTasks << ret
                } else {
                    throw new GradleException("Got '" + ret.class + "'. Expected 'Jar' task.")
                }
            } else if (it instanceof CharSequence) {
                def ret = project.tasks.getByName(it.toString())
                if (ret instanceof Jar) {
                    jTasks << ret
                } else {
                    throw new GradleException("Got '" + ret.class + "'. Expected 'Jar' task.")
                }
            } else {
                throw new GradleException("Got '" + it.class + "'. Expected 'Jar' task.")
            }
        }
        return jTasks
    }
}
