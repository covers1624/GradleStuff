package net.covers1624.gradlestuff.steam

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by covers1624 on 4/05/19.
 */
class FindSteamInstallTask extends DefaultTask implements FindSteamInstallSpec {

    private final FindSteamInstallAction action

    FindSteamInstallTask() {
        action = new FindSteamInstallAction(project)
    }

    @TaskAction
    def doTask() {
        action.execute()
    }

    @Override
    File getSteamInstall() {
        return action.steamInstall
    }
}
