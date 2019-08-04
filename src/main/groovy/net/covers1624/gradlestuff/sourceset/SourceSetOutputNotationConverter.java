package net.covers1624.gradlestuff.sourceset;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.internal.exceptions.DiagnosticsVisitor;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.typeconversion.NotationConvertResult;
import org.gradle.internal.typeconversion.NotationConverter;
import org.gradle.internal.typeconversion.TypeConversionException;

/**
 * Created by covers1624 on 31/05/19.
 */
public class SourceSetOutputNotationConverter implements NotationConverter<Object, Dependency> {

    private final Instantiator instantiator;

    public SourceSetOutputNotationConverter(Instantiator instantiator) {
        this.instantiator = instantiator;
    }

    @Override
    public void convert(Object notation, NotationConvertResult<? super Dependency> result) throws TypeConversionException {
        if (notation instanceof SourceSetOutput) {
            result.converted(instantiator.newInstance(SourceSetDependency.class, notation));
        }
    }

    @Override
    public void describe(DiagnosticsVisitor visitor) {
        visitor.candidate("SourceSetOutput").example("sourceSets.main.output");
    }
}
