package net.covers1624.gradlestuff.util

import org.gradle.api.Action
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.AbstractCopyTask

/**
 * Created by covers1624 on 2/05/18.
 */
object Utils {

    //The scala compiler has issues with the lambda in the arguments.
    //Fix is to use 'task.from(o, ().asInstanceOf[Action[CopySpec]])' instead.
    //But this is cleaner.
    def from(task: AbstractCopyTask, o: Any, f: Action[CopySpec]) {
        task.from(o, f)
    }
}
