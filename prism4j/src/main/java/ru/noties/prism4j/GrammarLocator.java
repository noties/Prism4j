package ru.noties.prism4j;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.noties.prism4j.annotations.PrismBundle;

@PrismBundle(include = {"clike", "markup"}, name = ".GrammarLocatorGen")
public interface GrammarLocator {

    @Nullable
    Prism4j.Grammar grammar(@NonNull Prism4j prism4j, @NonNull String language);
}
