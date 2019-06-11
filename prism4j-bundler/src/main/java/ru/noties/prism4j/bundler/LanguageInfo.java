package ru.noties.prism4j.bundler;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LanguageInfo {

    public final String name;
    public final List<String> aliases;
    public final String extend;
    public final List<String> modify;
    public final String source;

    public LanguageInfo(
            @NotNull String name,
            @Nullable List<String> aliases,
            @Nullable String extend,
            @Nullable List<String> modify,
            @NotNull String source
    ) {
        this.name = name;
        this.aliases = aliases;
        this.extend = extend;
        this.modify = modify;
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LanguageInfo that = (LanguageInfo) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "LanguageInfo{" +
                "name='" + name + '\'' +
                '}';
    }
}
