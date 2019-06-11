package ru.noties.prism4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class Cloner {

    @NotNull
    abstract Prism4j.Grammar clone(@NotNull Prism4j.Grammar grammar);

    @NotNull
    abstract Prism4j.Token clone(@NotNull Prism4j.Token token);

    @NotNull
    abstract Prism4j.Pattern clone(@NotNull Prism4j.Pattern pattern);

    @NotNull
    static Cloner create() {
        return new Impl();
    }

    static class Impl extends Cloner {

        interface Context {

            @Nullable
            Prism4j.Grammar grammar(@NotNull Prism4j.Grammar origin);

            @Nullable
            Prism4j.Token token(@NotNull Prism4j.Token origin);

            @Nullable
            Prism4j.Pattern pattern(@NotNull Prism4j.Pattern origin);


            void save(@NotNull Prism4j.Grammar origin, @NotNull Prism4j.Grammar clone);

            void save(@NotNull Prism4j.Token origin, @NotNull Prism4j.Token clone);

            void save(@NotNull Prism4j.Pattern origin, @NotNull Prism4j.Pattern clone);
        }

        @NotNull
        @Override
        Prism4j.Grammar clone(@NotNull Prism4j.Grammar grammar) {
            return clone(new ContextImpl(), grammar);
        }

        @NotNull
        @Override
        Prism4j.Token clone(@NotNull Prism4j.Token token) {
            return clone(new ContextImpl(), token);
        }

        @NotNull
        @Override
        Prism4j.Pattern clone(@NotNull Prism4j.Pattern pattern) {
            return clone(new ContextImpl(), pattern);
        }

        @NotNull
        private Prism4j.Grammar clone(@NotNull Context context, @NotNull Prism4j.Grammar grammar) {

            Prism4j.Grammar clone = context.grammar(grammar);
            if (clone != null) {
                return clone;
            }

            final List<Prism4j.Token> tokens = grammar.tokens();
            final List<Prism4j.Token> out = new ArrayList<>(tokens.size());

            clone = new GrammarImpl(grammar.name(), out);
            context.save(grammar, clone);

            for (Prism4j.Token token : tokens) {
                out.add(clone(context, token));
            }

            return clone;
        }

        @NotNull
        private Prism4j.Token clone(@NotNull Context context, @NotNull Prism4j.Token token) {

            Prism4j.Token clone = context.token(token);
            if (clone != null) {
                return clone;
            }

            final List<Prism4j.Pattern> patterns = token.patterns();
            final List<Prism4j.Pattern> out = new ArrayList<>(patterns.size());

            clone = new TokenImpl(token.name(), out);
            context.save(token, clone);

            for (Prism4j.Pattern pattern : patterns) {
                out.add(clone(context, pattern));
            }

            return clone;
        }

        @NotNull
        private Prism4j.Pattern clone(@NotNull Context context, @NotNull Prism4j.Pattern pattern) {

            Prism4j.Pattern clone = context.pattern(pattern);
            if (clone != null) {
                return clone;
            }

            final Prism4j.Grammar inside = pattern.inside();

            clone = new PatternImpl(
                    pattern.regex(),
                    pattern.lookbehind(),
                    pattern.greedy(),
                    pattern.alias(),
                    inside != null ? clone(context, inside) : null
            );

            context.save(pattern, clone);

            return clone;
        }

        private static class ContextImpl implements Context {

            private final Map<Integer, Object> cache = new HashMap<>(3);

            @Nullable
            @Override
            public Prism4j.Grammar grammar(@NotNull Prism4j.Grammar origin) {
                return (Prism4j.Grammar) cache.get(key(origin));
            }

            @Nullable
            @Override
            public Prism4j.Token token(@NotNull Prism4j.Token origin) {
                return (Prism4j.Token) cache.get(key(origin));
            }

            @Nullable
            @Override
            public Prism4j.Pattern pattern(@NotNull Prism4j.Pattern origin) {
                return (Prism4j.Pattern) cache.get(key(origin));
            }

            @Override
            public void save(@NotNull Prism4j.Grammar origin, @NotNull Prism4j.Grammar clone) {
                cache.put(key(origin), clone);
            }

            @Override
            public void save(@NotNull Prism4j.Token origin, @NotNull Prism4j.Token clone) {
                cache.put(key(origin), clone);
            }

            @Override
            public void save(@NotNull Prism4j.Pattern origin, @NotNull Prism4j.Pattern clone) {
                cache.put(key(origin), clone);
            }

            private static int key(@NotNull Object o) {
                return System.identityHashCode(o);
            }
        }
    }
}
