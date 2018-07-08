package ru.noties.prism4j;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Prism4jToken {


    @NonNull
    public abstract String name();

    @NonNull
    public abstract List<Prism4jPattern> patterns();


    @NonNull
    public static Prism4jToken create(@NonNull String name, @NonNull List<Prism4jPattern> patterns) {
        return new Impl(name, patterns);
    }

    @NonNull
    public static Prism4jToken create(@NonNull String name, Prism4jPattern... patterns) {

        final int length = patterns != null
                ? patterns.length
                : 0;

        final List<Prism4jPattern> list = new ArrayList<>(length);
        if (length > 0) {
            Collections.addAll(list, patterns);
        }

        return create(name, list);
    }


    static class Impl extends Prism4jToken {

        private final String name;
        private final List<Prism4jPattern> patterns;

        Impl(@NonNull String name, @NonNull List<Prism4jPattern> patterns) {
            this.name = name;
            this.patterns = patterns;
        }

        @NonNull
        @Override
        public String name() {
            return name;
        }

        @NonNull
        @Override
        public List<Prism4jPattern> patterns() {
            return patterns;
        }
    }
}
