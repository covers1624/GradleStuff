package net.covers1624.gradlestuff.util;

/**
 * Created by covers1624 on 11/02/19.
 */
public interface ThrowingProducer<T, E extends Throwable> {

    T get() throws E;
}
