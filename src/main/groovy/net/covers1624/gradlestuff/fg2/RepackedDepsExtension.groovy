/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.fg2

import org.gradle.api.Project

/**
 * Created by covers1624 on 3/05/19.
 */
class RepackedDepsExtension extends DepsExtension {

    private List<String> extraLines = []

    RepackedDepsExtension(Project project) {
        super(project)
    }

    def addExtraSrgLine(String line) {
        extraLines += line
    }

    def getExtraLines() {
        return extraLines
    }
}
