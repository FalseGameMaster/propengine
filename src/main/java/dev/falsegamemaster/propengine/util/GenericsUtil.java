package dev.falsegamemaster.propengine.util;

import java.util.Optional;

public class GenericsUtil {
    @SuppressWarnings("unchecked")
    public static <B> B castOrDefault(Object a, B b) {
        try {
            b = (B) a;
        } catch (ClassCastException ignored) {}
        return b;
    }

    public static <B> B castOrNull(Object a) {
        return castOrDefault(a, null);
    }

    public static <B> Optional<B> tryCast(Object a) {
        return Optional.ofNullable(castOrNull(a));
    }
}
