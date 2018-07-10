package ru.noties.prism4j.markwon;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import ru.noties.markwon.SpannableBuilder;
import ru.noties.markwon.SyntaxHighlight;
import ru.noties.prism4j.AbsVisitor;
import ru.noties.prism4j.Prism4j;
import ru.noties.prism4j.markwon.theme.Prism4jTheme;

public class Prism4jSyntaxHighlight implements SyntaxHighlight {

    // fallback option? in case of null, or if it's not found
    // theme
    // todo: somehow empty new line is removed when used this class

    @NonNull
    public static Prism4jSyntaxHighlight create(@NonNull Prism4j prism4j) {
        return new Prism4jSyntaxHighlight(prism4j, Prism4jTheme.prism4jDefault(), "clike");
    }

    private final Prism4j prism4j;
    private final Prism4jTheme theme;
    private final String fallback;

    public Prism4jSyntaxHighlight(
            @NonNull Prism4j prism4j,
            @NonNull Prism4jTheme theme,
            @Nullable String fallback
    ) {
        this.prism4j = prism4j;
        this.theme = theme;
        this.fallback = fallback;
    }

    @NonNull
    @Override
    public CharSequence highlight(@Nullable String info, @NonNull String code) {

        final CharSequence out;

        final Prism4j.Grammar grammar = grammar(info);
        if (grammar != null) {
            out = highlight(code, grammar);
        } else {
            out = code;
        }

        return out;
    }

    @Nullable
    private Prism4j.Grammar grammar(@Nullable String info) {

        Prism4j.Grammar grammar = null;

        if (!TextUtils.isEmpty(info)) {
            grammar = prism4j.grammar(info);
        }

        if (grammar == null
                && !TextUtils.isEmpty(fallback)) {
            grammar = prism4j.grammar(fallback);
        }

        return grammar;
    }

    @NonNull
    private CharSequence highlight(@NonNull String code, @NonNull Prism4j.Grammar grammar) {

        final List<Prism4j.Node> nodes = prism4j.tokenize(code, grammar);
        final SpannableBuilder builder = new SpannableBuilder();

        final Prism4j.Visitor visitor = new AbsVisitor() {
            @Override
            protected void visitText(@NonNull Prism4j.Text text) {
                builder.append(text.literal());
            }

            @Override
            protected void visitSyntax(@NonNull Prism4j.Syntax syntax) {
                final int start = builder.length();
                visit(syntax.children());
                theme.apply(builder, syntax, start, builder.length());
            }
        };

        visitor.visit(nodes);

        return builder.text();
    }
}
