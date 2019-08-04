package net.covers1624.gradlestuff.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * Created by covers1624 on 20/12/18.
 */
public class ConsumingOutputStream extends OutputStream {

    private final Consumer<String> consumer;

    private StringBuilder buffer = new StringBuilder();

    public ConsumingOutputStream(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void write(int b) throws IOException {
        char ch = (char) (b & 0xFF);
        buffer.append(ch);
        if (ch == '\n' || ch == '\r') {
            flush();
        }
    }

    @Override
    public void flush() throws IOException {
        String str = buffer.toString();
        if (str.endsWith("\r") || str.endsWith("\n")) {
            str = str.trim();
            if (!str.isEmpty()) {
                consumer.accept(str);
            }
            buffer = new StringBuilder();
        }
    }
}
