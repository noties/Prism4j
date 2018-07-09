package ru.noties.prism4j;

import android.support.annotation.NonNull;

class TextImpl implements Prism4j.Text {

    private final String literal;

    TextImpl(@NonNull String literal) {
        this.literal = literal;
    }

    @Override
    public int textLength() {
        return literal.length();
    }

    @NonNull
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
