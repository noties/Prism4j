package ru.noties.prism4j;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.regex.Pattern;

public abstract class Prism4jPattern {

    @NonNull
    public abstract Pattern pattern();

    public abstract boolean lookbehind();

    public abstract boolean greedy();

    @Nullable
    public abstract String alias();

    @Nullable
    public abstract Prism4jGrammar inside();


    @NonNull
    public static Prism4jPattern create(@NonNull Pattern pattern) {
        return builder(pattern).build();
    }

    @NonNull
    public static Prism4jPattern create(@NonNull Pattern pattern, boolean lookbehind) {
        return builder(pattern).lookbehind(lookbehind).build();
    }

    @NonNull
    public static Prism4jPattern create(@NonNull Pattern pattern, boolean lookbehind, boolean greedy) {
        return builder(pattern).lookbehind(lookbehind).greedy(greedy).build();
    }

    @NonNull
    public static Prism4jPattern create(@NonNull Pattern pattern, boolean lookbehind, boolean greedy, @Nullable String alias) {
        return builder(pattern).lookbehind(lookbehind).greedy(greedy).alias(alias).build();
    }

    @NonNull
    public static Prism4jPattern create(@NonNull Pattern pattern, boolean lookbehind, boolean greedy, @Nullable String alias, @Nullable Prism4jGrammar inside) {
        return builder(pattern).lookbehind(lookbehind).greedy(greedy).alias(alias).inside(inside).build();
    }

    @NonNull
    public static Builder builder(@NonNull Pattern pattern) {
        return new Builder(pattern);
    }

    public static class Builder {

        private final Pattern pattern;

        private boolean lookbehind;
        private boolean greedy;
        private String alias;
        private Prism4jGrammar inside;

        Builder(@NonNull Pattern pattern) {
            this.pattern = pattern;
        }

        @NonNull
        public Builder lookbehind(boolean lookbehind) {
            this.lookbehind = lookbehind;
            return this;
        }

        @NonNull
        public Builder greedy(boolean greedy) {
            this.greedy = greedy;
            return this;
        }

        @NonNull
        public Builder alias(@Nullable String alias) {
            this.alias = alias;
            return this;
        }

        @NonNull
        public Builder inside(@Nullable Prism4jGrammar inside) {
            this.inside = inside;
            return this;
        }

        @NonNull
        public Prism4jPattern build() {
            return new Impl(pattern, lookbehind, greedy, alias, inside);
        }
    }

    static class Impl extends Prism4jPattern {

        private final Pattern pattern;
        private final boolean lookbehind;
        private final boolean greedy;
        private final String alias;
        private final Prism4jGrammar inside;

        Impl(
                @NonNull Pattern pattern,
                boolean lookbehind,
                boolean greedy,
                @Nullable String alias,
                @Nullable Prism4jGrammar inside
        ) {
            this.pattern = pattern;
            this.lookbehind = lookbehind;
            this.greedy = greedy;
            this.alias = alias;
            this.inside = inside;
        }

        @NonNull
        @Override
        public Pattern pattern() {
            return pattern;
        }

        @Override
        public boolean lookbehind() {
            return lookbehind;
        }

        @Override
        public boolean greedy() {
            return greedy;
        }

        @Nullable
        @Override
        public String alias() {
            return alias;
        }

        @Nullable
        @Override
        public Prism4jGrammar inside() {
            return inside;
        }
    }
}
