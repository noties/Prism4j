package ru.noties.prism4j;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Basic class to locate grammars
 *
 * @see Prism4j#Prism4j(GrammarLocator)
 */
public interface GrammarLocator {

    @Nullable
    Prism4j.Grammar grammar(@NonNull Prism4j prism4j, @NonNull String language);
}
