package ru.noties.prism4j;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

// this class should not cache grammars, but provide them `as-is` -> new instance each time it's requested
public interface GrammarLocator {

    @Nullable
    Prism4j.Grammar grammar(@NonNull Prism4j prism4j, @NonNull String language);

    // will check if name has registered aliases and return root one
    // otherwise the supplied `name` will be returned
    @NonNull
    String grammarName(@NonNull String name);

    void registerAlias(@NonNull String root, String... aliases);
}
