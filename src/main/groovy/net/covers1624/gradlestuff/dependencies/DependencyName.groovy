package net.covers1624.gradlestuff.dependencies

import org.apache.commons.lang3.StringUtils

/**
 * Created by covers1624 on 2/8/19.
 */
class DependencyName {
    String group
    String module
    String version
    String classifier
    String extension

    DependencyName() {
    }

    DependencyName(String group, String module, String version, String classifier, String extension) {
        this.group = group
        this.module = module
        this.version = version
        this.classifier = classifier
        this.extension = extension
    }

    static def parseMaven(String str) {
        String[] segs = str.split(":")
        if (segs.length > 4 || segs.length < 3) {
            throw new RuntimeException("Invalid maven string: " + str)
        }
        String ext = "jar";
        if (segs[segs.length - 1].contains("@")) {
            String s = segs[segs.length - 1]
            int at = s.indexOf("@")
            ext = s.substring(at + 1)
            segs[segs.length - 1] = s.substring(0, at)
        }
        return new DependencyName(segs[0], segs[1], segs[2], segs.length > 3 ? segs[3] : "", ext)
    }

    @Override
    boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true
        }
        if (!(obj instanceof DependencyName)) {
            return false
        }
        def other = (DependencyName) obj
        return StringUtils.equals(other.group, group) &&
                StringUtils.equals(other.module, module) &&
                StringUtils.equals(other.version, version) &&
                StringUtils.equals(other.classifier, classifier) &&
                StringUtils.equals(other.extension, extension)
    }

    @Override
    int hashCode() {
        int result = 0
        result = 31 * result + group.hashCode()
        result = 31 * result + module.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + classifier != null ? classifier.hashCode() : 0
        result = 31 * result + extension.hashCode()
        return result
    }
}
