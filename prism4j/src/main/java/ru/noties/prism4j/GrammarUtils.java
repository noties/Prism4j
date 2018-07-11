package ru.noties.prism4j;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GrammarUtils {

    @Nullable
    public static Prism4j.Token findToken(@NonNull Prism4j.Grammar grammar, @NonNull String name) {
        Prism4j.Token token = null;
        for (Prism4j.Token t : grammar.tokens()) {
            if (name.equals(t.name())) {
                token = t;
                break;
            }
        }
        return token;
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
