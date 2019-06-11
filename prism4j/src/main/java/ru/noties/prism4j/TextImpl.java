package ru.noties.prism4j;

import org.jetbrains.annotations.NotNull;

public class TextImpl implements Prism4j.Text {

    private final String literal;

    public TextImpl(@NotNull String literal) {
        this.literal = literal;
    }

    @Override
    public int textLength() {
        return literal.length();
    }

    @Override
    public final boolean isSyntax() {
        return false;
    }

    @NotNull
    @Override
    public String literal() {
        return literal;
    }

    @Override
    public String toString() {
        return "TextImpl{" +
                "literal='" + literal + '\'' +
                '}';
    }
}
