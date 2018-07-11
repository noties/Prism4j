package ru.noties.prism4j.languages;

import android.support.annotation.NonNull;

import ru.noties.prism4j.GrammarUtils;
import ru.noties.prism4j.Prism4j;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static ru.noties.prism4j.Prism4j.grammar;
import static ru.noties.prism4j.Prism4j.pattern;
import static ru.noties.prism4j.Prism4j.token;

@SuppressWarnings("unused")
public abstract class Prism_css {

    @NonNull
    public static Prism4j.Grammar create(@NonNull Prism4j prism4j) {

        final Prism4j.Grammar grammar = grammar(
                "css",
                token("comment", pattern(compile("\\/\\*[\\s\\S]*?\\*\\/"))),
                token(
                        "atrule",
                        pattern(
                                compile("@[\\w-]+?.*?(?:;|(?=\\s*\\{))", CASE_INSENSITIVE),
                                false,
                                false,
                                null,
                                grammar(
                                        "inside",
                                        token("rule", pattern(compile("@[\\w-]+")))
                                )
                        )
                ),
                token(
                        "url",
                        pattern(compile("url\\((?:([\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1|.*?)\\)", CASE_INSENSITIVE))
                ),
                token("selector", pattern(compile("[^{}\\s][^{};]*?(?=\\s*\\{)"))),
                token(
                        "string",
                        pattern(compile("(\"|')(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1"), false, true)
                ),
                token(
                        "property",
                        pattern(compile("[-_a-z\\xA0-\\uFFFF][-\\w\\xA0-\\uFFFF]*(?=\\s*:)", CASE_INSENSITIVE))
                ),
                token(
                        "important",
                        pattern(compile("\\B!important\\b", CASE_INSENSITIVE))
                ),
                token(
                        "function",
                        pattern(compile("[-a-z0-9]+(?=\\()", CASE_INSENSITIVE))
                ),
                token("punctuation", pattern(compile("[(){};:]")))
        );

        // now we need to put the all tokens from grammar inside `atrule`
        final Prism4j.Token atrule = GrammarUtils.findToken(grammar, "atrule");
        final Prism4j.Grammar inside = atrule.patterns().get(0).inside();

        // most likely stackoverflow here
//        inside.tokens().addAll(grammar.tokens());

        for (Prism4j.Token token: grammar.tokens()) {
            if (!"atrule".equals(token.name())) {
                inside.tokens().add(token);
            }
        }

        return grammar;
    }

    private Prism_css() {
    }
}
