/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.steam

import net.rubygrapefruit.platform.Native
import net.rubygrapefruit.platform.NativeException
import net.rubygrapefruit.platform.WindowsRegistry
import org.gradle.api.GradleScriptException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.internal.os.OperatingSystem

/**
 * Created by covers1624 on 3/05/19.
 */
class FindSteamInstallAction implements FindSteamInstallSpec {

    private static windowsFolderAttempts = [
            new File("C:\\Program Files\\Steam\\"),
            new File("C:\\Program Files (x86)\\Steam\\")
    ]

    private static windowsRegistryAttempts = [
            /*HKEY_LOCAL_MACHINE\\*/ "SOFTWARE\\Valve\\Steam",//
            /*HKEY_LOCAL_MACHINE\\*/ "SOFTWARE\\Wow6432Node\\Valve\\Steam"//
    ]

    private static unixFolderAttempts = [
            new File(System.getenv("HOME"), ".steam/steam/"),
            new File(System.getenv("HOME"), ".local/share/Steam/"),
    ]

    private final Project project
    private final Logger logger
    private File steamInstall

    FindSteamInstallAction(Project project) {
        this.project = project
        logger = project.logger
    }


    void execute() {
        def os = OperatingSystem.current()
        if (os.isWindows()) {
            List<File> fileGuesses = []
            List<String> registryGuesses = []
            logger.info("Detected windows, Searching for steam in standard locations..")
            for (File attempt : windowsFolderAttempts) {
                fileGuesses += attempt
                logger.info(" Attempting: '{}'.", attempt.absolutePath)
                if (isValid(attempt)) {
                    logger.info("Found steam directory at '{}'", attempt.absolutePath)
                    steamInstall = attempt
                    return
                }
            }
            logger.info("Failed to find steam in standard locations. Searching registry keys..")
            Native.init(null)
            WindowsRegistry registry = Native.get(WindowsRegistry)
            for (String key : windowsRegistryAttempts) {
                registryGuesses += key
                logger.info(" Attempting 'HKLM\\\\{}'", key)
                try {
                    String result = registry.getStringValue(WindowsRegistry.Key.HKEY_LOCAL_MACHINE, key, "InstallPath")
                    File attempt = new File(result)
                    if (isValid(attempt)) {
                        logger.lifecycle("Found steam directory at '{}' from registry key 'HKLM\\\\{}'", attempt.absolutePath, key)
                        steamInstall = attempt
                        return
                    }
                } catch (NativeException ignored) {
                }
            }
            logger.error("Unable to find steam directory. Searched the following locations:")
            logger.error("Files:")
            for (File file : fileGuesses) {
                logger.error("    '{}'", file.absolutePath)
            }
            logger.error("RegistryKeys:")
            for (String s : registryGuesses) {
                logger.error("    '{}'", s)
            }
            throw new GradleScriptException("Failed to find steam install.")
        } else if (os.isUnix()) {
            List<File> fileGuesses = []
            logger.info("Detected unix({}), Searching for steam in standard locations..", os.getFamilyName())
            for (File attempt : unixFolderAttempts) {
                fileGuesses += attempt
                logger.info(" Attempting: '{}'.", attempt.absolutePath)
                if (isValid(attempt)) {
                    logger.info("Found steam directory at '{}'", attempt.absolutePath)
                    steamInstall = attempt
                    return
                }
            }
            logger.error("Unable to find steam directory. Searched the following locations:")
            logger.error("Files:")
            for (File file : fileGuesses) {
                logger.error("    '{}'", file.absolutePath)
            }
            throw new GradleScriptException("Failed to find steam install.")
        } else {
            throw new RuntimeException("Unsupported operating system. " + os)
        }
    }


    @Override
    File getSteamInstall() {
        return steamInstall
    }

    private static boolean isValid(File file) {
        return file.exists() && (new File(file, "steamapps").exists() || new File(file, "SteamApps").exists())
    }

}
