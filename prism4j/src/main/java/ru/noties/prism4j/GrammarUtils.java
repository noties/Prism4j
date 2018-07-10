package ru.noties.prism4j;

import android.support.annotation.NonNull;

import java.util.List;

public abstract class GrammarUtils {

    @NonNull
    public static List<Prism4j.Token> extend(
            @NonNull List<Prism4j.Token> origin,
            @NonNull List<Prism4j.Token> replace
    ) {
        // we copy everything into a new list from origin only if it's not overriden by replace
        return null;
    }

    private GrammarUtils() {
    }
}
