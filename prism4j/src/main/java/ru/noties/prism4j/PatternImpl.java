package ru.noties.prism4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatternImpl implements Prism4j.Pattern {

    private final java.util.regex.Pattern regex;
    private final boolean lookbehind;
    private final boolean greedy;
    private final String alias;
    private final Prism4j.Grammar inside;

    public PatternImpl(
            @NotNull java.util.regex.Pattern regex,
            boolean lookbehind,
            boolean greedy,
            @Nullable String alias,
            @Nullable Prism4j.Grammar inside) {
        this.regex = regex;
        this.lookbehind = lookbehind;
        this.greedy = greedy;
        this.alias = alias;
        this.inside = inside;
    }

    @NotNull
    @Override
    public java.util.regex.Pattern regex() {
        return regex;
    }

    @Override
    public boolean lookbehind() {
        return lookbehind;
    }

    @Override
    public boolean greedy() {
        return greedy;
    }

    @Nullable
    @Override
    public String alias() {
        return alias;
    }

    @Nullable
    @Override
    public Prism4j.Grammar inside() {
        return inside;
    }

    @Override
    public String toString() {
        return ToString.toString(this);
    }
}
