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
