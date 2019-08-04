package net.covers1624.gradlestuff.sourceset;

import org.gradle.api.tasks.SourceSet;

/**
 * Created by covers1624 on 31/05/19.
 */
public interface SourceAwareOutputExtension {

    SourceSet getSourceSet();

}
