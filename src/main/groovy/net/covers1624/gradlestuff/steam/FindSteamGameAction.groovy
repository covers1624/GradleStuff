/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.steam

import net.covers1624.quack.annotation.Requires
import net.platinumdigitalgroup.jvdf.VDFNode
import net.platinumdigitalgroup.jvdf.VDFParser
import org.gradle.api.Project
import org.gradle.api.logging.Logger

/**
 * Created by covers1624 on 4/05/19.
 */
@Requires('net.platinumdigitalgroup:JVDF')
@Requires('net.rubygrapefruit:native-platform:0.14')
class FindSteamGameAction implements FindSteamGameSpec {

    private final Project project
    private final Logger logger
    private int appId
    private File gameInstall

    FindSteamGameAction(Project project) {
        this.project = project
        logger = project.logger
    }

    def execute() {
        FindSteamInstallSpec spec = new FindSteamInstallAction(project)
        spec.execute()

        File steamInstall = spec.steamInstall
        File steamApps = chooseExists(steamInstall, "steamapps", "SteamApps")
        List<File> steamLibraries = [steamApps]

        logger.info("Loading libraryfolders.vdf..")
        File lfFile = new File(steamApps, "libraryfolders.vdf")
        VDFNode lfRoot = new VDFParser().parse(lfFile.readLines().toArray(new String[0]))
        VDFNode lfNode = lfRoot.getSubNode("LibraryFolders")
        for (String key : lfNode.keySet()) {
            if (key.isInteger()) {
                File base = new File(lfNode.getString(key))
                logger.info("Found library '{}'.", base.absolutePath)
                steamLibraries += chooseExists(base, "steamapps", "SteamApps")
            }
        }
        for (File libraryFolder : steamLibraries) {
            logger.info("Searching library '{}' for app '{}'.", libraryFolder, appId)
            File attempt = new File(libraryFolder, "appmanifest_" + appId + ".acf")
            if (attempt.exists()) {
                VDFNode node = new VDFParser().parse(attempt.readLines().toArray(new String[0]))
                VDFNode appNode = node.getSubNode("AppState")
                gameInstall = new File(new File(libraryFolder, "common"), appNode.getString("installdir"))
                logger.info("Found game '{}'({}) at '{}'.", appNode.getString("name"), appId, gameInstall)
                return
            }
        }
        logger.error("Unable to find app '{}' on your system. Please ensure the game is installed in steam. Re-Run with --info to see debug output.", appId)
        throw new RuntimeException()
    }


    @Override
    void setAppId(int id) {
        appId = id
    }

    @Override
    File getGameInstall() {
        return gameInstall
    }

    private static File chooseExists(File base, String a, String b) {
        File fA = new File(base, a)
        File fB = new File(base, b)
        if (fA.exists()) {
            return fA
        } else if (fB.exists()) {
            return fB
        }
        throw new FileNotFoundException(fA.absolutePath)
    }

}
