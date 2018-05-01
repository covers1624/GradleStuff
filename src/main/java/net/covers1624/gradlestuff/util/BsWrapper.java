package net.covers1624.gradlestuff.util;

import org.gradle.api.Action;
import org.gradle.api.file.CopySpec;
import org.gradle.api.tasks.AbstractCopyTask;

/**
 * Created by covers1624 on 1/05/18.
 */
public class BsWrapper {

    //Because the scala compiler insisted on using the Vargs method..
    public static void from(AbstractCopyTask task, Object object, Action<CopySpec> action) {
        task.from(object, action);
    }

}
