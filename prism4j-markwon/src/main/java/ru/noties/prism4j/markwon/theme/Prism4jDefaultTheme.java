package ru.noties.prism4j.markwon.theme;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.ForegroundColorSpan;

import java.util.HashMap;
import java.util.Map;

import ru.noties.markwon.SpannableBuilder;
import ru.noties.prism4j.Prism4j;

public class Prism4jDefaultTheme extends Prism4jTheme {

    // a way to apply styling for specific languages... at least
    // todo: .language-css .token.string,
    // todo: .style .token.string

    public static class Color {

        @NonNull
        public static Color of(@ColorInt int color) {
            return new Color(color);
        }

        @ColorInt
        public final int color;

        public Color(@ColorInt int color) {
            this.color = color;
        }
    }

    private final Map<String, Color> map;

    public Prism4jDefaultTheme() {
        this.map = init();
    }

    // todo: expose language
    @Override
    public void apply(@NonNull SpannableBuilder builder, @NonNull Prism4j.Syntax syntax, int start, int end) {
        final int color = color(syntax.type(), syntax.alias());
        if (color != 0) {
            builder.setSpan(new ForegroundColorSpan(color), start, end);
        }
    }

    @ColorInt
    protected int color(@NonNull String name, @Nullable String alias) {
        // todo: maybe first use alias and only after that try name?
        Color color = map.get(name);
        if (color == null) {
            color = map.get(alias);
        }
        return color != null
                ? color.color
                : 0;
    }

    @NonNull
    protected Map<String, Color> init() {
        return new ColorHashMap()
                .add(0xFF708090, "comment", "prolog", "doctype", "cdata")
                .add(0xFF999999, "punctuation")
                .add(0xFF990055, "property", "tag", "boolean", "number", "constant", "symbol", "deleted")
                .add(0xFF669900, "selector", "attr-name", "string", "char", "builtin", "inserted")
                .add(0xFF9a6e3a, "operator", "entity", "url")
                .add(0xFF0077aa, "atrule", "attr-value", "keyword")
                .add(0xFFDD4A68, "function", "class-name")
                .add(0xFFee9900, "regex", "important", "variable");
    }

    protected static class ColorHashMap extends HashMap<String, Color> {

        @NonNull
        protected ColorHashMap add(@ColorInt int color, String... names) {
            final Color c = Color.of(color);
            for (String name : names) {
                put(name, c);
            }
            return this;
        }
    }
}
