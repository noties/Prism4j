package {{package-name}};

import android.support.annotation.NonNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.noties.prism4j.GrammarLocator;
import ru.noties.prism4j.Prism4j;

{{imports}}

public class {{class-name}} implements GrammarLocator {

    @SuppressWarnings("ConstantConditions")
    private static final Prism4j.Grammar NULL = new Prism4j.Grammar() {
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
            triggerModify(prism4j, name);
        }

        return grammar;
    }

    @NonNull
    protected String realLanguageName(@NonNull String name) {
        {{real-language-name}}
    }

    @Nullable
    protected Prism4j.Grammar obtainGrammar(@NonNull Prism4j prism4j, @NonNull String name) {
        {{obtain-grammar}}
    }

    protected void triggerModify(@NonNull Prism4j prism4j, @NonNull String name) {
        {{trigger-modify}}
    }

    @Override
    @NonNull
    public Set<String> languages() {
        {{languages}}
    }
}
