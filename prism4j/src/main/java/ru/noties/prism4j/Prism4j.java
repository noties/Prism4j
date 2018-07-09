package ru.noties.prism4j;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import static java.util.regex.Pattern.compile;

public class Prism4j {

    // todo: rest thing (just override/add to existing grammar)
    // todo: insertBefore
    // todo: allow delete (it sometimes happen)

    public interface Grammar {

        @NonNull
        String name();

        // should mention that returned array is mutable
        @NonNull
        List<Token> tokens();
    }

    public interface Token {

        @NonNull
        String name();

        @NonNull
        List<Pattern> patterns();
    }

    public interface Pattern {

        @NonNull
        java.util.regex.Pattern regex();

        boolean lookbehind();

        boolean greedy();

        @Nullable
        String alias();

        @Nullable
        Grammar inside();
    }

    @NonNull
    public static Grammar grammar(@NonNull String name, @NonNull List<Token> tokens) {
        return new GrammarImpl(name, tokens);
    }

    @NonNull
    public static Grammar grammar(@NonNull String name, Token... tokens) {
        return new GrammarImpl(name, ArrayUtils.toList(tokens));
    }

    @NonNull
    public static Token token(@NonNull String name, @NonNull List<Pattern> patterns) {
        return new TokenImpl(name, patterns);
    }

    @NonNull
    public static Token token(@NonNull String name, Pattern... patterns) {
        return new TokenImpl(name, ArrayUtils.toList(patterns));
    }

    @NonNull
    public static Pattern pattern(@NonNull String regex) {
        return pattern(compile(regex));
    }

    @NonNull
    public static Pattern pattern(@NonNull String regex, int flags) {
        return pattern(compile(regex, flags));
    }

    @NonNull
    public static Pattern pattern(@NonNull java.util.regex.Pattern regex) {
        return new PatternImpl(regex, false, false, null, null);
    }

    @NonNull
    public static Pattern pattern(@NonNull java.util.regex.Pattern regex, boolean lookbehind) {
        return new PatternImpl(regex, lookbehind, false, null, null);
    }

    @NonNull
    public static Pattern pattern(
            @NonNull java.util.regex.Pattern regex,
            boolean lookbehind,
            boolean greedy) {
        if (greedy && !isPatternGlobal(regex)) {
            regex = makePatternGlobal(regex);
        }
        return new PatternImpl(regex, lookbehind, greedy, null, null);
    }

    @NonNull
    public static Pattern pattern(
            @NonNull java.util.regex.Pattern regex,
            boolean lookbehind,
            boolean greedy,
            @Nullable String alias) {
        if (greedy && !isPatternGlobal(regex)) {
            regex = makePatternGlobal(regex);
        }
        return new PatternImpl(regex, lookbehind, greedy, alias, null);
    }

    @NonNull
    public static Pattern pattern(
            @NonNull java.util.regex.Pattern regex,
            boolean lookbehind,
            boolean greedy,
            @Nullable String alias,
            @Nullable Grammar inside) {
        if (greedy && !isPatternGlobal(regex)) {
            regex = makePatternGlobal(regex);
        }
        return new PatternImpl(regex, lookbehind, greedy, alias, inside);
    }

//    public static void main(String[] args) throws Throwable {
//
//        final String content = "whatever\n\"at the top\"\n\n/* this is\nwith multiple lines\n comment */\n//newline\nclass Hel.lo \"another string\" { private int time = 0xff * 15; if (true) { throw new \"strings are thrown!\"; }";
//
//        final Prism4j prism4j = new Prism4j();
//        final long start = System.currentTimeMillis();
//        final List<Node> entries = prism4j.tokenize(content, clike());
//        final long end = System.currentTimeMillis();
//        for (Node entry : entries) {
//            System.out.println(entry.toString());
//        }
//        System.out.printf("took: %d ms", end - start);
//
//    }

    public interface Node {
        int rawTextLength();
    }

    public static class Literal implements Node {

        final String literal;

        Literal(@NonNull String literal) {
            this.literal = literal;
        }

        @Override
        public int rawTextLength() {
            return literal.length();
        }

        @Override
        public String toString() {
            return "Literal{" +
                    "literal='" + literal + '\'' +
                    '}';
        }
    }

    public static class Entry implements Node {

        private final String type;
        private final List<? extends Node> content;
        private final String alias;
        private final String matchedString;
        private final boolean greedy;

        private Entry(
                @NonNull String type,
                @NonNull List<? extends Node> content,
                @Nullable String alias,
                @NonNull String matchedString,
                boolean greedy) {
            this.type = type;
            this.content = content;
            this.alias = alias;
            this.matchedString = matchedString;
            this.greedy = greedy;
        }

        @Override
        public int rawTextLength() {
            return matchedString.length();
        }

        @Override
        public String toString() {
            return "Token{" +
                    "type='" + type + '\'' +
                    ", content=" + content +
                    ", alias='" + alias + '\'' +
                    ", matchedString='" + matchedString + '\'' +
                    ", greedy=" + greedy +
                    '}';
        }
    }

    @NonNull
    private List<Node> tokenize(@NonNull String text, @NonNull Grammar grammar) {
        final List<Node> entries = new ArrayList<>(3);
        entries.add(new Literal(text));
        matchGrammar(text, entries, grammar, 0, 0, false, null);
        return entries;
    }

    private void matchGrammar(
            @NonNull String text,
            @NonNull List<Node> entries,
            @NonNull Grammar grammar,
            int index,
            int startPosition,
            boolean oneShot,
            @Nullable Token target
    ) {

        for (Token token : grammar.tokens()) {

            if (token == target) {
                return;
            }

            for (Pattern pattern : token.patterns()) {

                final boolean lookbehind = pattern.lookbehind();
                final boolean greedy = pattern.greedy();
                int lookbehindLength = 0;

                java.util.regex.Pattern regex = pattern.regex();

                // Don't cache rawTextLength as it changes during the loop
                for (int i = index, position = startPosition; i < entries.size(); position += entries.get(i).rawTextLength(), ++i) {

                    // todo: more meaningful thing here
                    if (entries.size() > text.length()) {
                        System.out.printf("entries: %s%n", entries);
                        throw new RuntimeException();
                    }

                    final Node node = entries.get(i);
                    if (node instanceof Entry) {
                        continue;
                    }

                    String str = ((Literal) node).literal;

                    final Matcher matcher;
                    final int deleteCount;
                    final boolean greedyMatch;
                    int greedyAdd = 0;

                    if (greedy && i != entries.size() - 1) {

                        matcher = regex.matcher(text);
                        // limit search to the position (?)
                        matcher.region(position, text.length());

                        if (!matcher.find()) {
                            break;
                        }

                        int from = matcher.start();

                        if (lookbehind) {
                            from += matcher.group(1).length();
                        }
                        final int to = matcher.start() + matcher.group(0).length();

                        int k = i;
                        int p = position;

                        for (int len = entries.size(); k < len && (p < to || (!isToken(entries.get(k)) && !isGreedy(entries.get(k - 1)))); ++k) {
                            p += entries.get(k).rawTextLength();
                            // Move the index i to the element in strarr that is closest to from
                            if (from >= p) {
                                i += 1;
                                position = p;
                            }
                        }

                        if (entries.get(i) instanceof Entry) {
                            continue;
                        }

                        deleteCount = k - i;
                        str = text.substring(position, p);
                        greedyMatch = true;
                        greedyAdd = -position;

                    } else {
                        matcher = regex.matcher(str);
                        deleteCount = 1;
                        greedyMatch = false;
                    }

                    if (!greedyMatch && !matcher.find()) {
                        if (oneShot) {
                            break;
                        }
                        continue;
                    }

                    if (lookbehind) {
                        final String group = matcher.group(1);
                        lookbehindLength = group != null ? group.length() : 0;
                    }

                    final int from = matcher.start() + greedyAdd + lookbehindLength;
                    final String match;
                    if (lookbehindLength > 0) {
                        match = matcher.group().substring(lookbehindLength);
                    } else {
                        match = matcher.group();
                    }
                    final int to = from + match.length();

                    for (int d = 0; d < deleteCount; d++) {
                        entries.remove(i);
                    }

                    int i2 = i;

                    if (from != 0) {
                        final String before = str.substring(0, from);
                        i += 1;
                        position += before.length();
                        entries.add(i2++, new Literal(before));
                    }

                    final List<? extends Node> tokenEntries;
                    final Grammar inside = pattern.inside();
                    if (inside != null) {
                        tokenEntries = tokenize(match, inside);
                    } else {
                        tokenEntries = Collections.singletonList(new Literal(match));
                    }

                    entries.add(i2++, new Entry(
                            token.name(),
                            tokenEntries,
                            pattern.alias(),
                            match,
                            greedy
                    ));

                    if (to != str.length() - 1) {
                        final String after = str.substring(to);
                        entries.add(i2, new Literal(after));
                    }

                    if (deleteCount != 1) {
                        matchGrammar(text, entries, grammar, i, position, true, token);
                    }

                    if (oneShot) {
                        break;
                    }
                }
            }
        }

    }

    private static boolean isPatternGlobal(@NonNull java.util.regex.Pattern pattern) {
        return java.util.regex.Pattern.MULTILINE == (java.util.regex.Pattern.MULTILINE & pattern.flags());
    }

    @NonNull
    private static java.util.regex.Pattern makePatternGlobal(@NonNull java.util.regex.Pattern pattern) {
        return java.util.regex.Pattern.compile(pattern.pattern(), java.util.regex.Pattern.MULTILINE | pattern.flags());
    }

    // todo: as we have only two classes, maybe remove instanceof check (somehow)
    private static boolean isToken(@NonNull Node node) {
        return node instanceof Entry;
    }

    private static boolean isGreedy(@NonNull Node node) {
        return (node instanceof Entry) && ((Entry) node).greedy;
    }

    @NonNull
    private static Grammar clike() {
        return grammar(
                "clike",
                token(
                        "comment",
                        pattern(compile("(^|[^\\\\])\\/\\*[\\s\\S]*?(?:\\*\\/|$)"), true),
                        pattern(compile("(^|[^\\\\:])\\/\\/.*"), true, true)
                ),
                token(
                        "string",
                        pattern(compile("([\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1"), false, true)
                ),
                token(
                        "class-name",
                        pattern(
                                compile("((?:\\b(?:class|interface|extends|implements|trait|instanceof|new)\\s+)|(?:catch\\s+\\())[\\w.\\\\]+"),
                                true,
                                false,
                                null,
                                grammar(
                                        "inside", // name doesn't matter much here
                                        token(
                                                "punctuation",
                                                pattern(compile("[.\\\\]"))
                                        )
                                )
                        )
                ),
                token(
                        "keyword",
                        pattern(compile("\\b(?:if|else|while|do|for|return|in|instanceof|function|new|try|throw|catch|finally|null|break|continue|class)\\b"))
                ),
                token("boolean", pattern(compile("\\b(?:true|false)\\b"))),
                token(
                        "function",
                        pattern(compile("a-z0-9_]+(?=\\()", java.util.regex.Pattern.CASE_INSENSITIVE))
                ),
                token(
                        "number",
                        pattern(compile("\\b0x[\\da-f]+\\b|(?:\\b\\d+\\.?\\d*|\\B\\.\\d+)(?:e[+-]?\\d+)?", java.util.regex.Pattern.CASE_INSENSITIVE))
                ),
                token(
                        "operator",
                        pattern(compile("--?|\\+\\+?|!=?=?|<=?|>=?|==?=?|&&?|\\|\\|?|\\?|\\*|\\/|~|\\^|%"))
                ),
                token("punctuation", pattern(compile("[{}\\[\\];(),.:]")))
        );
    }
}
