package ru.noties.prism4j.markwon.theme;

import android.support.annotation.NonNull;

import ru.noties.markwon.SpannableBuilder;
import ru.noties.prism4j.Prism4j;

public abstract class Prism4jTheme {

    public abstract void apply(@NonNull SpannableBuilder builder, @NonNull Prism4j.Syntax syntax, int start, int end);

    @NonNull
    public static Prism4jTheme prism4jDefault() {
        return new Prism4jDefaultTheme();
    }
}
