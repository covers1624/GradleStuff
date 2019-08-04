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
