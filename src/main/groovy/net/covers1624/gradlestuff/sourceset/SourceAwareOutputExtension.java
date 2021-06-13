/*
 * This file is part of GradleStuff and is Licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 covers1624 <https://github.com/covers1624>
 */
package net.covers1624.gradlestuff.sourceset;

import org.gradle.api.tasks.SourceSet;

/**
 * Created by covers1624 on 31/05/19.
 */
public interface SourceAwareOutputExtension {

    SourceSet getSourceSet();

}
