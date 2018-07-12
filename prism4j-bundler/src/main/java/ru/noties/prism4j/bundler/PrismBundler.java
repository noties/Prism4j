package ru.noties.prism4j.bundler;

import android.support.annotation.NonNull;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

import ix.Ix;
import ru.noties.prism4j.annotations.PrismBundle;

import static javax.tools.Diagnostic.Kind.NOTE;

public class PrismBundler extends AbstractProcessor {

    private static final String LANGUAGES_PACKAGE = "ru.noties.prism4j.languages";
    private static final String LANGUAGE_SOURCE_PATTERN = "languages/ru/noties/prism4j/languages/Prism_%1$s.java";

    private static final String TEMPLATE_PACKAGE_NAME = "{{package-name}}";
    private static final String TEMPLATE_IMPORTS = "{{imports}}";
    private static final String TEMPLATE_CLASS_NAME = "{{class-name}}";
    private static final String TEMPLATE_REAL_LANGUAGE_NAME = "{{real-language-name}}";
    private static final String TEMPLATE_OBTAIN_GRAMMAR = "{{obtain-grammar}}";
    private static final String TEMPLATE_TRIGGER_MODIFY = "{{trigger-modify}}";

    private final AnnotationsInformation annotationsInformation = AnnotationsInformation.create();

    private Messager messager;
    private Elements elements;
    private Filer filer;

    // this override might've killed me... without it processor cannot find ANY resources...
    @Override
    public Set<String> getSupportedOptions() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(PrismBundle.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        elements = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if (!roundEnvironment.processingOver()) {
            final long start = System.currentTimeMillis();
            final Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(PrismBundle.class);

            // generate locator for each annotated element
            // extract languages that are used by each locator
            // then write them
            if (elements != null) {

                final Set<LanguageInfo> languages = new HashSet<>();

                for (Element element : elements) {
                    if (element != null) {
                        languages.addAll(process(element));
                    }
                }

                if (languages.size() > 0) {
                    writeLanguages(languages);
                }
            }
            final long end = System.currentTimeMillis();
            messager.printMessage(NOTE, "[prism4j-bundler] Processing took: " + (end - start) + " ms");
        }

        return false;
    }

    // we must return a set of languageInfos here (so we do not copy language more than once
    // thus allowing multiple grammarLocators)

    @NonNull
    private Set<LanguageInfo> process(@NonNull Element element) {

        final PrismBundle bundle = element.getAnnotation(PrismBundle.class);
        if (bundle == null) {
            return Collections.emptySet();
        }

        final Map<String, LanguageInfo> languages;
        {
            final List<String> names = processLanguageNames(bundle.include());
            final int size = names.size();
            if (size > 0) {
                languages = new LinkedHashMap<>(size);
                for (String name : names) {
                    languageInfo(languages, name);
                }
            } else {
                languages = null;
            }
        }

        if (languages == null
                || languages.size() == 0) {
            throw new RuntimeException("No languages are specified to be included");
        }

        final String template = grammarLocatorTemplate();
        final ClassInfo classInfo = classInfo(element, bundle.name());
        final String source = grammarLocatorSource(template, classInfo, languages);

        Writer writer = null;
        try {
            final JavaFileObject javaFileObject = filer.createSourceFile(classInfo.packageName + "." + classInfo.className);
            writer = javaFileObject.openWriter();
            writer.write(source);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // no op
                }
            }
        }

        return new HashSet<>(languages.values());

        // first obtain all languages from annotation `include` parameter
        // then validate that such a language is present in `languages` folder
        // then parse @Language annotation of a language source file
        //  additionally check for extend clause and add parent language to compilation also (and then again recursively)
        // then just copy all requested language files
        // then generate a grammarLocator implementation
        //  include all languages and additionally generate aliases information

        // so, to start we generate a map of languages to include

        // todo: I also think that caching grammars (and not exposing aliases to Prism4j) inside locator is better
    }

    private void writeLanguages(@NonNull Set<LanguageInfo> languages) {
        for (LanguageInfo info : languages) {
            Writer writer = null;
            try {
                final JavaFileObject javaFileObject = filer.createSourceFile(LANGUAGES_PACKAGE + ".Prism_" + info.name);
                writer = javaFileObject.openWriter();
                writer.write(info.source);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        // no op
                    }
                }
            }
        }
    }

    @NonNull
    private List<String> processLanguageNames(@NonNull String[] names) {
        return Ix.fromArray(names)
                .filter(Objects::nonNull)
                .filter(s -> s.length() > 0)
                .filter(s -> s.trim().length() > 0)
                .toList();
    }

    private void languageInfo(@NonNull Map<String, LanguageInfo> map, @NonNull String name) {

        if (map.containsKey(name)) {
            return;
        }

        // read info
        final String source;
        try {
            source = IOUtils.resourceToString(languageSourceFileName(name), StandardCharsets.UTF_8, PrismBundler.class.getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException(String.format(Locale.US, "Unable to read language `%1$s` " +
                    "source file. Either it is not defined yet or it was referenced as an alias " +
                    "when specifying extend clause", name), e);
        }

        final List<String> aliases = annotationsInformation.findAliasesInformation(source);
        final String extend = annotationsInformation.findExtendInformation(source);
        final List<String> modify = annotationsInformation.findModifyInformation(source);

        map.put(name, new LanguageInfo(name, aliases, extend, modify, source));

        if (extend != null) {
            languageInfo(map, extend);
        }
    }

    @NonNull
    private static String languageSourceFileName(@NonNull String name) {
        return String.format(Locale.US, LANGUAGE_SOURCE_PATTERN, name);
    }

    @NonNull
    private static String grammarLocatorTemplate() {
        try {
            return IOUtils.resourceToString("/GrammarLocator.template.java", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private ClassInfo classInfo(@NonNull Element element, @NonNull String name) {

        final String packageName;
        final String className;

        if ('.' == name.charAt(0)) {
            final PackageElement packageElement = elements.getPackageOf(element);
            packageName = packageElement.getQualifiedName().toString();
            className = name.substring(1);
        } else {
            final int index = name.lastIndexOf('.');
            packageName = name.substring(0, index);
            className = name.substring(index + 1);
        }

        return new ClassInfo(packageName, className);
    }

    @NonNull
    private static String grammarLocatorSource(
            @NonNull String template,
            @NonNull ClassInfo classInfo,
            @NonNull Map<String, LanguageInfo> languages) {
        final StringBuilder builder = new StringBuilder(template);
        replaceTemplate(builder, TEMPLATE_PACKAGE_NAME, classInfo.packageName);
        replaceTemplate(builder, TEMPLATE_IMPORTS, createImports(languages));
        replaceTemplate(builder, TEMPLATE_CLASS_NAME, classInfo.className);
        replaceTemplate(builder, TEMPLATE_REAL_LANGUAGE_NAME, createRealLanguageName(languages));
        replaceTemplate(builder, TEMPLATE_OBTAIN_GRAMMAR, createObtainGrammar(languages));
        replaceTemplate(builder, TEMPLATE_TRIGGER_MODIFY, createTriggerModify(languages));
        final Formatter formatter = new Formatter(JavaFormatterOptions.defaultOptions());
        try {
            return formatter.formatSource(builder.toString());
        } catch (FormatterException e) {
            System.out.printf("source: %n%s%n", builder.toString());
            throw new RuntimeException(e);
        }
    }

    private static void replaceTemplate(@NonNull StringBuilder template, @NonNull String name, @NonNull String content) {
        final int index = template.indexOf(name);
        template.replace(index, index + name.length(), content);
    }

    @NonNull
    private static String createImports(@NonNull Map<String, LanguageInfo> languages) {
        final StringBuilder builder = new StringBuilder();
        Ix.from(languages.values())
                .map(languageInfo -> languageInfo.name)
                .orderBy(String::compareTo)
                .foreach(s -> builder
                        .append("import ")
                        .append(LANGUAGES_PACKAGE)
                        .append(".Prism_")
                        .append(s)
                        .append(';')
                        .append('\n'));
        return builder.toString();
    }

    @NonNull
    private static String createRealLanguageName(@NonNull Map<String, LanguageInfo> languages) {
        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, LanguageInfo> entry : languages.entrySet()) {
            final List<String> aliases = entry.getValue().aliases;
            if (aliases != null
                    && aliases.size() > 0) {
                for (String alias : aliases) {
                    builder.append("case \"")
                            .append(alias)
                            .append('\"')
                            .append(':')
                            .append('\n');
                }
                builder.append("out = \"")
                        .append(entry.getKey())
                        .append('\"')
                        .append(';')
                        .append('\n')
                        .append("break;\n");
            }
        }

        if (builder.length() > 0) {
            builder.append("default:\n")
                    .append("out = name;\n")
                    .append("}\nreturn out;");
            builder.insert(0, "final String out;\nswitch (name) {");
            return builder.toString();
        } else {
            return "return name;";
        }
    }

    @NonNull
    private static String createObtainGrammar(@NonNull Map<String, LanguageInfo> languages) {
        final StringBuilder builder = new StringBuilder();
        builder
                .append("final Prism4j.Grammar grammar;\n")
                .append("switch(name) {\n");
        Ix.from(languages.keySet())
                .orderBy(String::compareTo)
                .foreach(s -> builder.append("case \"")
                        .append(s)
                        .append("\":\n")
                        .append("grammar = Prism_")
                        .append(s)
                        .append(".create(prism4j);\nbreak;\n"));
        builder.append("default:\ngrammar = null;\n}")
                .append("return grammar;");
        return builder.toString();
    }

    @NonNull
    private static String createTriggerModify(@NonNull Map<String, LanguageInfo> languages) {

        // so, create a map collection where each entry in `modify` is the key and languageInfo.name is value
        final Map<String, List<String>> map = new HashMap<>(3);

        List<String> modify;

        for (LanguageInfo info : languages.values()) {

            modify = info.modify;

            if (modify != null
                    && modify.size() > 0) {

                for (String name : modify) {
                    map.computeIfAbsent(name, k -> new ArrayList<>(3)).add(info.name);
                }
            }
        }

        if (map.size() == 0) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("switch(name){\n");
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            builder.append("case \"")
                    .append(entry.getKey())
                    .append("\":\n");
            for (String lang : entry.getValue()) {
                builder.append("prism4j.grammar(\"")
                        .append(lang)
                        .append("\");\n");
            }
            builder.append("break;\n");
        }
        builder.append("}");
        return builder.toString();
    }

    public static void main(String[] args) {
        final ClassInfo classInfo = new ClassInfo("ru.noties.prism4j.languages", "MyClass");
        final Map<String, LanguageInfo> languages = new HashMap<>();
        languages.put("clike", new LanguageInfo("clike", null, null, null, ""));
        languages.put("markup", new LanguageInfo("markup", null, null, null, ""));
        System.out.printf("source: %n%s%n", grammarLocatorSource(grammarLocatorTemplate(), classInfo, languages));
    }
}
