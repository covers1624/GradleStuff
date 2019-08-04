package net.covers1624.gradlestuff.sourceset;

import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.internal.HasConvention;
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetOutput;

/**
 * Created by covers1624 on 31/05/19.
 */
public class SourceSetDependency extends DefaultSelfResolvingDependency {

    private final SourceSet sourceSet;

    public SourceSetDependency(SourceSetOutput sourceSetOutput) {
        super((FileCollectionInternal) sourceSetOutput);
        SourceAwareOutputExtension ext = ((HasConvention) sourceSetOutput).getConvention().findByType(SourceAwareOutputExtension.class);
        sourceSet = ext.getSourceSet();
    }

    public SourceSetDependency(ComponentIdentifier targetComponentId, SourceSetOutput sourceSetOutput) {
        super(targetComponentId, (FileCollectionInternal) sourceSetOutput);
        SourceAwareOutputExtension ext = ((HasConvention) sourceSetOutput).getConvention().findByType(SourceAwareOutputExtension.class);
        sourceSet = ext.getSourceSet();
    }

    public SourceSet getSourceSet() {
        return sourceSet;
    }
}
