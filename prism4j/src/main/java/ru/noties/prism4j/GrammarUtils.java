package ru.noties.prism4j;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GrammarUtils {

    public interface TokenFilter {
        boolean test(@NonNull Prism4j.Token token);
    }

    @Nullable
    public static Prism4j.Token findToken(@NonNull Prism4j.Grammar grammar, @NonNull String path) {
        final String[] parts = path.split("/");
        return findToken(grammar, parts, 0);
    }

    @Nullable
    private static Prism4j.Token findToken(@NonNull Prism4j.Grammar grammar, @NonNull String[] parts, int index) {

        final String part = parts[index];
        final boolean last = index == parts.length - 1;

        for (Prism4j.Token token : grammar.tokens()) {
            if (part.equals(token.name())) {
                if (last) {
                    return token;
                } else {
                    final Prism4j.Grammar inside = findFirstInsideGrammar(token);
                    if (inside != null) {
                        return findToken(inside, parts, index + 1);
                    } else {
                        break;
                    }
                }
            }
        }

        return null;
    }

    // won't work if there are multiple patterns provided for a token (each with inside grammar)
    public static void insertBeforeToken(
            @NonNull Prism4j.Grammar grammar,
            @NonNull String path,
            Prism4j.Token... tokens
    ) {

        if (tokens == null
                || tokens.length == 0) {
            return;
        }

        final String[] parts = path.split("/");

        insertBeforeToken(grammar, parts, 0, tokens);
    }

    private static void insertBeforeToken(
            @NonNull Prism4j.Grammar grammar,
            @NonNull String[] parts,
            int index,
            @NonNull Prism4j.Token[] tokens) {

        final String part = parts[index];
        final boolean last = index == parts.length - 1;

        final List<Prism4j.Token> grammarTokens = grammar.tokens();

        Prism4j.Token token;

        for (int i = 0, size = grammarTokens.size(); i < size; i++) {

            token = grammarTokens.get(i);

            if (part.equals(token.name())) {

                // here we must decide what to do next:
                //  - it can be out found one
                //  - or we need to go deeper (c)
                if (last) {
                    // here we go, it's our token
                    insertTokensAt(i, grammarTokens, tokens);
                } else {
                    // now we must find a grammar that is inside
                    // token can have multiple patterns
                    // but as they are not identified somehow (no name or anything)
                    // we will try to find first pattern with inside grammar
                    final Prism4j.Grammar inside = findFirstInsideGrammar(token);
                    if (inside != null) {
                        insertBeforeToken(inside, parts, index + 1, tokens);
                    }
                }

                // break after we have found token with specified name (most likely it won't repeat itself)
                break;
            }
        }
    }

    @Nullable
    public static Prism4j.Grammar findFirstInsideGrammar(@NonNull Prism4j.Token token) {
        Prism4j.Grammar grammar = null;
        for (Prism4j.Pattern pattern : token.patterns()) {
            if (pattern.inside() != null) {
                grammar = pattern.inside();
                break;
            }
        }
        return grammar;
    }

    private static void insertTokensAt(
            int start,
            @NonNull List<Prism4j.Token> grammarTokens,
            @NonNull Prism4j.Token[] tokens
    ) {
        for (int i = 0, length = tokens.length; i < length; i++) {
            grammarTokens.add(start + i, tokens[i]);
        }
    }

    @NonNull
    public static Prism4j.Grammar clone(@NonNull Prism4j.Grammar grammar) {
        final List<Prism4j.Token> tokens = grammar.tokens();
        final List<Prism4j.Token> copy = new ArrayList<>(tokens.size());
        for (Prism4j.Token token : tokens) {
            copy.add(clone(token));
        }
        return new GrammarImpl(grammar.name(), copy);
    }

    @NonNull
    public static Prism4j.Token clone(@NonNull Prism4j.Token token) {
        final List<Prism4j.Pattern> patterns = token.patterns();
        final List<Prism4j.Pattern> copy = new ArrayList<>(patterns.size());
        for (Prism4j.Pattern pattern : patterns) {
            copy.add(clone(pattern));
        }
        return new TokenImpl(token.name(), copy);
    }

    @NonNull
    public static Prism4j.Pattern clone(@NonNull Prism4j.Pattern pattern) {
        final Prism4j.Grammar inside = pattern.inside();
        return new PatternImpl(
                pattern.regex(),
                pattern.lookbehind(),
                pattern.greedy(),
                pattern.alias(),
                inside != null ? clone(inside) : null
        );
    }

    @NonNull
    public static Prism4j.Grammar extend(
            @NonNull Prism4j.Grammar grammar,
            @NonNull String name,
            Prism4j.Token... tokens) {

        // we clone the whole grammar, but override top-most tokens that are passed here

        final int size = tokens != null
                ? tokens.length
                : 0;

        if (size == 0) {
            return new GrammarImpl(name, clone(grammar).tokens());
        }

        final Map<String, Prism4j.Token> overrides = new HashMap<>(size);
        for (Prism4j.Token token : tokens) {
            overrides.put(token.name(), token);
        }

        final List<Prism4j.Token> origins = grammar.tokens();
        final List<Prism4j.Token> out = new ArrayList<>(origins.size());

        Prism4j.Token override;

        for (Prism4j.Token origin : origins) {
            override = overrides.get(origin.name());
            if (override != null) {
                out.add(override);
            } else {
                out.add(clone(origin));
            }
        }

        return new GrammarImpl(name, out);
    }

    @NonNull
    public static Prism4j.Grammar extend(
            @NonNull Prism4j.Grammar grammar,
            @NonNull String name,
            @NonNull TokenFilter filter,
            Prism4j.Token... tokens) {

        final int size = tokens != null
                ? tokens.length
                : 0;

        final Map<String, Prism4j.Token> overrides;
        if (size == 0) {
            overrides = Collections.emptyMap();
        } else {
            overrides = new HashMap<>(size);
            for (Prism4j.Token token : tokens) {
                overrides.put(token.name(), token);
            }
        }

        final List<Prism4j.Token> origins = grammar.tokens();
        final List<Prism4j.Token> out = new ArrayList<>(origins.size());

        Prism4j.Token override;

        for (Prism4j.Token origin : origins) {

            // filter out undesired tokens
            if (!filter.test(origin)) {
                continue;
            }

            override = overrides.get(origin.name());
            if (override != null) {
                out.add(override);
            } else {
                out.add(clone(origin));
            }
        }

        return new GrammarImpl(name, out);
    }

    @NonNull
    public static Prism4j.Grammar require(@NonNull Prism4j prism4j, @NonNull String name) {
        final Prism4j.Grammar grammar = prism4j.grammar(name);
        if (grammar == null) {
            throw new IllegalStateException("Unexpected state, requested language is not found: " + name);
        }
        return grammar;
    }

    @NonNull
    static List<Prism4j.Token> extend(
            @NonNull List<Prism4j.Token> origin,
            @NonNull List<Prism4j.Token> replace
    ) {

        // we copy everything into a new list from origin only if it's not overriden by replace

        // prepare a map with replace tokens
        final Map<String, Prism4j.Token> overrides = new HashMap<>(replace.size());
        for (Prism4j.Token token : replace) {
            overrides.put(token.name(), token);
        }

        final List<Prism4j.Token> out = new ArrayList<>(origin.size());
        for (Prism4j.Token token : origin) {
            final Prism4j.Token replacement = overrides.get(token.name());
            if (replacement != null) {
                out.add(replacement);
            } else {
                out.add(token);
            }
        }

        return out;
    }

    private GrammarUtils() {
    }
}
