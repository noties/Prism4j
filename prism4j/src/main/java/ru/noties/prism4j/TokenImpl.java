package ru.noties.prism4j;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TokenImpl implements Prism4j.Token {

    private final String name;
    private final List<Prism4j.Pattern> patterns;

    public TokenImpl(@NotNull String name, @NotNull List<Prism4j.Pattern> patterns) {
        this.name = name;
        this.patterns = patterns;
    }

    @NotNull
    @Override
    public String name() {
        return name;
    }

    @NotNull
    @Override
    public List<Prism4j.Pattern> patterns() {
        return patterns;
    }

    @Override
    public String toString() {
        return ToString.toString(this);
    }
}
