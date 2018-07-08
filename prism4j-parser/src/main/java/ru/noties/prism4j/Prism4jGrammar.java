package ru.noties.prism4j;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Prism4jGrammar {


    @NonNull
    public abstract String name();

    @NonNull
    public abstract List<Prism4jToken> tokens();


    @NonNull
    public static Prism4jGrammar create(@NonNull String name, @NonNull List<Prism4jToken> tokens) {
        return new Impl(name, tokens);
    }

    @NonNull
    public static Prism4jGrammar create(@NonNull String name, Prism4jToken... tokens) {

        final int length = tokens != null
                ? tokens.length
                : 0;

        final List<Prism4jToken> list = new ArrayList<>(length);
        if (length > 0) {
            Collections.addAll(list, tokens);
        }

        return create(name, list);
    }


    static class Impl extends Prism4jGrammar {

        private final String name;
        private final List<Prism4jToken> tokens;

        Impl(@NonNull String name, @NonNull List<Prism4jToken> tokens) {
            this.name = name;
            this.tokens = tokens;
        }

        @NonNull
        @Override
        public String name() {
            return name;
        }

        @NonNull
        @Override
        public List<Prism4jToken> tokens() {
            return tokens;
        }
    }
}
