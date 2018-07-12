
package ru.noties.prism4j.zzz;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.noties.prism4j.GrammarLocator;
import ru.noties.prism4j.Prism4j;
import ru.noties.prism4j.languages.Prism_css;
import ru.noties.prism4j.languages.Prism_markup;

public class ZZZLocator2 implements GrammarLocator {

    @SuppressWarnings("ConstantConditions")
    private static final Prism4j.Grammar NULL =
            new Prism4j.Grammar() {
                @NonNull
                @Override
                public String name() {
                    return null;
                }

                @NonNull
                @Override
                public List<Prism4j.Token> tokens() {
                    return null;
                }
            };

    private final Map<String, Prism4j.Grammar> cache = new HashMap<>(3);

    @Nullable
    @Override
    public Prism4j.Grammar grammar(@NonNull Prism4j prism4j, @NonNull String language) {

        final String name = realLanguageName(language);

        Prism4j.Grammar grammar = cache.get(name);
        if (grammar != null) {
            if (NULL == grammar) {
                grammar = null;
            }
            return grammar;
        }

        grammar = obtainGrammar(prism4j, name);
        if (grammar == null) {
            cache.put(name, NULL);
        } else {
            cache.put(name, grammar);
            // maybe trigger modify language here?
        }

        return grammar;
    }

    @NonNull
    protected String realLanguageName(@NonNull String name) {
        final String out;
        switch (name) {
            case "xml":
            case "html":
            case "mathml":
            case "svg":
                out = "markup";
                break;
            default:
                out = name;
        }
        return out;
    }

    @Nullable
    protected Prism4j.Grammar obtainGrammar(@NonNull Prism4j prism4j, @NonNull String name) {
        final Prism4j.Grammar grammar;
        switch (name) {
            case "css":
                grammar = Prism_css.create(prism4j);
                break;
            case "markup":
                grammar = Prism_markup.create(prism4j);
                // if there are languages that modify THIS one, trigger their initialization
                // but... it will trigger prism4j.grammar('name') which is not finished yet
                break;
            default:
                grammar = null;
        }
        return grammar;
    }

    protected void triggerModify(@NonNull Prism4j prism4j, @NonNull String name) {
        switch (name) {
            case "markup":
                grammar(prism4j, "css");
                grammar(prism4j, "html");
                prism4j.grammar("css");
                break;
        }
    }
}
