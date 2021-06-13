/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.fg2

import net.minecraftforge.gradle.user.TaskSingleReobf
import net.minecraftforge.gradle.user.UserConstants
import org.gradle.api.GradleScriptException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by covers1624 on 3/05/19.
 */
class RepackedDepsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin("net.minecraftforge.gradle.forge")) {
            throw new GradleScriptException("RepackedDeps plugin must be loaded after Forge. 'net.minecraftforge.gradle.forge'")
        }

        def extension = project.extensions.create("repackedDeps", RepackedDepsExtension, project)

        project.afterEvaluate {
            def configuration = extension.configuration
            def tasks = extension.tasks
            def reobfTask = project.tasks.withType(TaskSingleReobf).findByName(UserConstants.TASK_REOBF)
            reobfTask.addExtraSrgLines(extension.extraLines)
            tasks.each { t ->
                configuration.each { f ->
                    t.from(project.zipTree(f)) {
                        exclude("META-INF", "META-INF/**")
                    }
                }
            }
        }
    }
}
