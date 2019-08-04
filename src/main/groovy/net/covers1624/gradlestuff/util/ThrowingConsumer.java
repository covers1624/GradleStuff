package net.covers1624.gradlestuff.util;

/**
 * Created by covers1624 on 11/02/19.
 */
public interface ThrowingConsumer<T, E extends Throwable> {

    void accept(T thing) throws E;

}
