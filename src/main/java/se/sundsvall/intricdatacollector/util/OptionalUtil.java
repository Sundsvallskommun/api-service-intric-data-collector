package se.sundsvall.intricdatacollector.util;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class OptionalUtil {

    private OptionalUtil() { }

    public static <T> UnaryOperator<T> peek(final Consumer<T> c) {
        return x -> {
            c.accept(x);
            return x;
        };
    }
}
