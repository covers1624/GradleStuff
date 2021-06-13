/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.steam

/**
 * Created by covers1624 on 3/05/19.
 */
interface FindSteamInstallSpec {

    /**
     * Finds a steam install.
     * Should only be called after the task has executed.
     *
     * @return Returns the found SteamInstall directory.
     */
    File getSteamInstall()

}
