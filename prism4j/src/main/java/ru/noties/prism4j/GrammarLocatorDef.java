package ru.noties.prism4j;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.noties.prism4j.languages.Prism_clike;
import ru.noties.prism4j.languages.Prism_markup;

public class GrammarLocatorDef implements GrammarLocator {

    private final Map<String, Set<String>> aliases = new HashMap<>(1);

    {
        registerAlias("markup", "xml", "html", "mathml", "svg");
    }

    @Nullable
    @Override
    public Prism4j.Grammar grammar(@NonNull Prism4j prism4j, @NonNull String language) {

        // we will check for default languages and then construct a possible class name and try to
        // obtain it via reflection

        final Prism4j.Grammar grammar;

        if ("clike".equals(language)) {
            grammar = Prism_clike.create(prism4j);
        } else if ("markup".equals(language)) {
            grammar = Prism_markup.create(prism4j);
        } else {
            grammar = obtainGrammar(language, prism4j);
        }

        return grammar;
    }

    @NonNull
    @Override
    public String grammarName(@NonNull String name) {

        // if root requested -> return immediately
        if (aliases.containsKey(name)) {
            return name;
        }

        for (Map.Entry<String, Set<String>> entry : aliases.entrySet()) {
            if (entry.getValue().contains(name)) {
                return entry.getKey();
            }
        }

        return name;
    }

    public void registerAlias(@NonNull String parent, String... aliases) {
        Set<String> strings = this.aliases.get(parent);
        if (strings == null) {
            strings = new HashSet<>(aliases.length);
            this.aliases.put(parent, strings);
        }
        Collections.addAll(strings, aliases);
    }

    @Nullable
    protected Prism4j.Grammar obtainGrammar(@NonNull String language, @NonNull Prism4j prism4j) {
        final String className = "ru.noties.prism4j.languages.Prism_" + language;
        try {
            final Class<?> type = Class.forName(className);
            final Method create = type.getMethod("create", Prism4j.class);
            return (Prism4j.Grammar) create.invoke(null, prism4j);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
}
