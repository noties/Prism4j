package {{package-name}};

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        @NotNull
        @Override
        public String name() {
            return null;
        }

        @NotNull
        @Override
        public List<Prism4j.Token> tokens() {
            return null;
        }
    };

    private final Map<String, Prism4j.Grammar> cache = new HashMap<>(3);

    @Nullable
    @Override
    public Prism4j.Grammar grammar(@NotNull Prism4j prism4j, @NotNull String language) {

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

    @NotNull
    protected String realLanguageName(@NotNull String name) {
        {{real-language-name}}
    }

    @Nullable
    protected Prism4j.Grammar obtainGrammar(@NotNull Prism4j prism4j, @NotNull String name) {
        {{obtain-grammar}}
    }

    protected void triggerModify(@NotNull Prism4j prism4j, @NotNull String name) {
        {{trigger-modify}}
    }

    @Override
    @NotNull
    public Set<String> languages() {
        {{languages}}
    }
}
