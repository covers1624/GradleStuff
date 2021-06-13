/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.steam

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by covers1624 on 4/05/19.
 */
class FindSteamGameTask extends DefaultTask implements FindSteamGameSpec {

    private final FindSteamGameAction action

    FindSteamGameTask() {
        action = new FindSteamGameAction(project)
    }

    @TaskAction
    def doTask() {
        action.execute()
    }

    @Override
    void setAppId(int id) {
        action.setAppId(id)
    }

    @Override
    File getGameInstall() {
        return action.gameInstall
    }
}
