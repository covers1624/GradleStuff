package net.covers1624.gradlestuff.download;

import org.gradle.api.DefaultTask;
import org.gradle.api.internal.tasks.TaskExecutionOutcome;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Inspired and vaguely based off https://github.com/michel-kraemer/gradle-download-task
 * <pre>
 * Notable differences:
 *  Wayyy simpler implementation.
 *  Lazy evaluation of file and URL inputs.
 *  Single file downloads.
 *  External validation of file for up-to-date checking.
 *  UserAgent spoofing. (Thanks mojang!)
 *  Ability to set the ProgressLogger to use.
 * </pre>
 *
 * This is split into an Action, Spec and Task.
 *
 * The Spec {@link DownloadSpec}, Provides the specification for how things work.
 *
 * The Action {@link DownloadAction}, What actually handles downloading
 * implements {@link DownloadSpec}, Useful for other tasks that need to download
 * something but not necessarily create an entire task to do said download.
 *
 * The Task {@link DownloadTask}, Task wrapper for {@link DownloadAction},
 * implements {@link DownloadSpec} and hosts the Action as a task.
 *
 * Created by covers1624 on 8/02/19.
 */
public class DownloadTask extends DefaultTask implements DownloadSpec {

    private final DownloadAction action;

    public DownloadTask() {
        action = new DownloadAction(getProject());
        getOutputs().upToDateWhen(e -> false);//Always run, we set our self to up-to-date after checks.
    }

    @TaskAction
    public void doTask() throws IOException {
        action.execute();
        //We always execute, but 'spoof' our self as up-to-date after etag &| onlyIfModified checks.
        if (action.isUpToDate()) {
            getState().setOutcome(TaskExecutionOutcome.UP_TO_DATE);
            setDidWork(false);
        }
    }

    //@formatter:off
    @Override public void fileUpToDateWhen(Spec<File> spec) { action.fileUpToDateWhen(spec); }
    @Override public URL getSrc() { return action.getSrc(); }
    @OutputFile @Override public File getDest() { return action.getDest(); }
    @Override public boolean getOnlyIfModified() { return action.getOnlyIfModified(); }
    @Override public DownloadAction.UseETag getUseETag() { return action.getUseETag(); }
    @Override public File getETagFile() { return action.getETagFile(); }
    @Override public String getUserAgent() { return action.getUserAgent(); }
    @Override public boolean isQuiet() { return action.isQuiet(); }
    @Override public boolean isUpToDate() { return action.isUpToDate(); }
    @Override public void setSrc(Object src) { action.setSrc(src); }
    @Override public void setDest(Object dest) { action.setDest(dest); }
    @Override public void setOnlyIfModified(boolean onlyIfModified) { action.setOnlyIfModified(onlyIfModified); }
    @Override public void setUseETag(Object useETag) { action.setUseETag(useETag); }
    @Override public void setETagFile(Object eTagFile) { action.setETagFile(eTagFile); }
    @Override public void setUserAgent(String userAgent) { action.setUserAgent(userAgent); }
    @Override public void setQuiet(boolean quiet) { action.setQuiet(quiet); }
    @Override public void setProgressLogger(ProgressLogger logger) { action.setProgressLogger(logger); }
    //@formatter:on
}
