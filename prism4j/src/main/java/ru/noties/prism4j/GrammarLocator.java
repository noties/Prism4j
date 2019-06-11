package ru.noties.prism4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Basic class to locate grammars
 *
 * @see Prism4j#Prism4j(GrammarLocator)
 */
public interface GrammarLocator {

    @Nullable
    Prism4j.Grammar grammar(@NotNull Prism4j prism4j, @NotNull String language);

    /**
     * @return collection of languages included into this locator
     * @since 1.1.0
     */
    @NotNull
    Set<String> languages();
}
