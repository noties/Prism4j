package ru.noties.prism4j;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Prism4j {

    public static void main(String[] args) throws Throwable {

//        Debug.init(new SystemOutDebugOutput(true));

        final String content = "whatever\n\"at the top\"\n\n/* this is\nwith multiple lines\n comment */\n//newline\nclass Hel.lo \"another string\" { private int time = 0xff * 15; if (true) { throw new \"strings are thrown!\"; }";

//        if (true) {
//            final Pattern normal = Pattern.compile("([\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1");
//            final Pattern global = makePatternGlobal(normal);
//            System.out.printf("normal: %s, global: %s%n", isPatternGlobal(normal), isPatternGlobal(global));
//
//            final Matcher mNormal = normal.matcher(content);
//            final Matcher mGlobal = global.matcher(content);
//
//            while (mNormal.find()) {
//                System.out.printf("normal, str: `%s`%n", content.substring(mNormal.start(), mNormal.end()));
//            }
//
//            while (mGlobal.find()) {
//                System.out.printf("global, str: `%s`%n", content.substring(mGlobal.start(), mGlobal.end()));
//            }
//
//            return;
//        }

        final Prism4j prism4j = new Prism4j();
        final long start = System.currentTimeMillis();
        final List<Entry> entries = prism4j.tokenize(content, clike());
        final long end = System.currentTimeMillis();
        for (Entry entry : entries) {
            System.out.println(entry.toString());
        }
        System.out.printf("took: %d ms", end - start);

    }

    private interface Entry {
        int length();
    }

    private static class Literal implements Entry {

        final String literal;

        Literal(@NonNull String literal) {
            this.literal = literal;
        }

        @Override
        public int length() {
            return literal.length();
        }

        @Override
        public String toString() {
            return "Literal{" +
                    "literal='" + literal + '\'' +
                    '}';
        }
    }

    private static class Token implements Entry {

        private final String type;
        private final List<? extends Entry> content;
        private final String alias;
        private final String matchedString;
        private final boolean greedy;

        private Token(
                @NonNull String type,
                @NonNull List<? extends Entry> content,
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
        public int length() {
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
    private List<Entry> tokenize(@NonNull String text, @NonNull Prism4jGrammar grammar) {
        final List<Entry> entries = new ArrayList<>(3);
        entries.add(new Literal(text));
        matchGrammar(text, entries, grammar, 0, 0, false, null);
        return entries;
    }

    private void matchGrammar(
            @NonNull String text,
            @NonNull List<Entry> entries,
            @NonNull Prism4jGrammar grammar,
            int index,
            int startPosition,
            boolean oneShot,
            @Nullable Prism4jToken target
    ) {

        for (Prism4jToken token : grammar.tokens()) {

            if (token == target) {
                return;
            }

            for (Prism4jPattern pattern : token.patterns()) {

                final boolean lookbehind = pattern.lookbehind();
                final boolean greedy = pattern.greedy();
                int lookbehindLength = 0;

                Pattern regex = pattern.pattern();

                // if greedy and pattern is not global
                if (greedy && !isPatternGlobal(regex)) {
                    // here we should make it global
                    // todo: move to pattern itself
                    regex = makePatternGlobal(regex);
                }

                // Don’t cache length as it changes during the loop
                for (int i = index, position = startPosition; i < entries.size(); position += entries.get(i).length(), ++i) {

                    // todo: more meaningful thing here
                    if (entries.size() > text.length()) {
                        System.out.printf("entries: %s%n", entries);
                        throw new RuntimeException();
                    }

                    final Entry entry = entries.get(i);
                    if (entry instanceof Token) {
//                        System.out.printf("instanceof token: %s%n", entry);
                        continue;
                    }

                    String str = ((Literal) entry).literal;

//                    System.out.printf("token: %s, str: `%s`%n", token.name(), str);
//                    if ("string".equals(token.name())) {
//                        System.out.printf("string, str: `%s`%n", str);
//                    }

                    final Matcher matcher;
                    final int deleteCount;
                    final boolean greedyMatch;
                    int greedyAdd = 0;

                    if (greedy && i != entries.size() - 1) {

                        matcher = regex.matcher(text);
                        // limit search to the position (?)
                        matcher.region(position, text.length());

                        if (!matcher.find()) {
//                            if ("string".equals(token.name()))
//                            System.out.printf("cannot find greedy, i: %d, position: %d, entries: %s, str: `%s`%n", i, position, entries, str);
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
                            p += entries.get(k).length();
                            // Move the index i to the element in strarr that is closest to from
                            if (from >= p) {
                                i += 1;
                                position = p;
                            }
                        }

                        if (entries.get(i) instanceof Token) {
//                            System.out.printf("instanceof token: %s%n", entry);
                            continue;
                        }

                        deleteCount = k - i;
                        str = text.substring(position, p);
//                        hasMatch = true;
                        greedyMatch = true;
                        greedyAdd = -position;

//                        System.out.printf("position: %d, regionStart: %d, delCount: %d, position: %d, str: `%s`%n", position, matcher.regionStart(), deleteCount, position, str);
//                        matcher = regex.matcher(str);
//                        matcher.reset();

                        // if we do this: only one first string is found, if we do not -> all except first one
//                        final int start = matcher.regionStart();
//                        matcher.reset();
//                        matcher.region(start - position, str.length());

//                        matcher = regex.matcher(str);
//                        matcher.region(matcher.regionStart() - position, str.length());
//                        matcher.region(matcher.regionStart() - position, str.length());
//                        matcher.region(0, str.length());

                    } else {
                        matcher = regex.matcher(str);
                        deleteCount = 1;
//                        hasMatch = matcher.find();
                        greedyMatch = false;

//                        if ("string".equals(token.name())) {
//                            System.out.printf("");
//                        }
                    }

                    if (!greedyMatch && !matcher.find()) {
//                        System.out.printf("no find, token: %s, str: %s%n", token.name(), str);
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

//                    if ("string".equals(token.name())) {
//                        System.out.printf("from: %d, to: %d, str: `%s`%n", from, to, str);
//                    }

                    // replace `entries` collection at `i` (index) `deleteCount` elements count
                    // with before? token after?

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

                    final List<? extends Entry> tokenEntries;
                    final Prism4jGrammar inside = pattern.inside();
                    if (inside != null) {
                        tokenEntries = tokenize(match, inside);
                    } else {
                        tokenEntries = Collections.singletonList(new Literal(match));
                    }

                    entries.add(i2++, new Token(
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

    private static boolean isPatternGlobal(@NonNull Pattern pattern) {
        return Pattern.MULTILINE == (Pattern.MULTILINE & pattern.flags());
    }

    @NonNull
    private static Pattern makePatternGlobal(@NonNull Pattern pattern) {
        return Pattern.compile(pattern.pattern(), Pattern.MULTILINE | pattern.flags());
    }

    private static boolean isToken(@NonNull Entry entry) {
        return entry instanceof Token;
    }
//
//    private static boolean isLiteral(@NonNull Entry entry) {
//        return entry instanceof Literal;
//    }

    private static boolean isGreedy(@NonNull Entry entry) {
        return (entry instanceof Token) && ((Token) entry).greedy;
    }

    @NonNull
    private static Prism4jGrammar clike() {
        return Prism4jGrammar.create(
                "clike",
                Prism4jToken.create(
                        "comment",
                        Prism4jPattern.create(Pattern.compile("(^|[^\\\\])\\/\\*[\\s\\S]*?(?:\\*\\/|$)"), true),
                        Prism4jPattern.create(Pattern.compile("(^|[^\\\\:])\\/\\/.*"), true, true)
                ),
                Prism4jToken.create(
                        "string",
                        Prism4jPattern.create(Pattern.compile("([\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1"), false, true)
                ),
                Prism4jToken.create(
                        "class-name",
                        Prism4jPattern.create(
                                Pattern.compile("((?:\\b(?:class|interface|extends|implements|trait|instanceof|new)\\s+)|(?:catch\\s+\\())[\\w.\\\\]+"),
                                true,
                                false,
                                null,
                                Prism4jGrammar.create(
                                        "inside",
                                        Prism4jToken.create(
                                                "punctuation",
                                                Prism4jPattern.create(Pattern.compile("[.\\\\]"))
                                        )
                                )
                        )
                ),
                Prism4jToken.create(
                        "keyword",
                        Prism4jPattern.create(Pattern.compile("\\b(?:if|else|while|do|for|return|in|instanceof|function|new|try|throw|catch|finally|null|break|continue|class)\\b"))
                ),
                Prism4jToken.create(
                        "boolean",
                        Prism4jPattern.create(Pattern.compile("\\b(?:true|false)\\b"))
                ),
                Prism4jToken.create(
                        "function",
                        Prism4jPattern.create(Pattern.compile("a-z0-9_]+(?=\\()", Pattern.CASE_INSENSITIVE))
                ),
                Prism4jToken.create(
                        "number",
                        Prism4jPattern.create(Pattern.compile("\\b0x[\\da-f]+\\b|(?:\\b\\d+\\.?\\d*|\\B\\.\\d+)(?:e[+-]?\\d+)?", Pattern.CASE_INSENSITIVE))
                ),
                Prism4jToken.create(
                        "operator",
                        Prism4jPattern.create(Pattern.compile("--?|\\+\\+?|!=?=?|<=?|>=?|==?=?|&&?|\\|\\|?|\\?|\\*|\\/|~|\\^|%"))
                ),
                Prism4jToken.create(
                        "punctuation",
                        Prism4jPattern.create(Pattern.compile("[{}\\[\\];(),.:]"))
                )
        );
    }
}
