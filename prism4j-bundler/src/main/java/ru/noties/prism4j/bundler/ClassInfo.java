package ru.noties.prism4j.bundler;

import android.support.annotation.NonNull;

public class ClassInfo {

    public final String packageName;
    public final String className;

    public ClassInfo(@NonNull String packageName, @NonNull String className) {
        this.packageName = packageName;
        this.className = className;
    }
}
