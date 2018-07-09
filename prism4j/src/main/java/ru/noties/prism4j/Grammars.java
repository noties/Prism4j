package ru.noties.prism4j;

import android.support.annotation.NonNull;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static ru.noties.prism4j.Prism4j.grammar;
import static ru.noties.prism4j.Prism4j.pattern;
import static ru.noties.prism4j.Prism4j.token;

public abstract class Grammars {

    @NonNull
    public static Prism4j.Grammar markup() {
        final Prism4j.Token entity = token("entity", pattern(compile("&#?[\\da-z]{1,8};", Pattern.CASE_INSENSITIVE)));
        return grammar(
                "markup",
                token("comment", pattern(compile("<!--[\\s\\S]*?-->"))),
                token("prolog", pattern(compile("<\\?[\\s\\S]+?\\?>"))),
                token("doctype", pattern(compile("<!DOCTYPE[\\s\\S]+?>", Pattern.CASE_INSENSITIVE))),
                token("cdata", pattern(compile("<!\\[CDATA\\[[\\s\\S]*?]]>", Pattern.CASE_INSENSITIVE))),
                token(
                        "tag",
                        pattern(
                                compile("<\\/?(?!\\d)[^\\s>\\/=$<%]+(?:\\s+[^\\s>\\/=]+(?:=(?:(\"|')(?:\\\\[\\s\\S]|(?!\\1)[^\\\\])*\\1|[^\\s'\">=]+))?)*\\s*\\/?>", Pattern.CASE_INSENSITIVE),
                                false,
                                true,
                                null,
                                grammar(
                                        "inside",
                                        token(
                                                "tag",
                                                pattern(
                                                        compile("^<\\/?[^\\s>\\/]+", Pattern.CASE_INSENSITIVE),
                                                        false,
                                                        false,
                                                        null,
                                                        grammar(
                                                                "inside",
                                                                token("punctuation", pattern(compile("^<\\/?"))),
                                                                token("namespace", pattern(compile("^[^\\s>\\/:]+:")))
                                                        )
                                                )
                                        ),
                                        token(
                                                "attr-value",
                                                pattern(
                                                        compile("=(?:(\"|')(?:\\\\[\\s\\S]|(?!\\1)[^\\\\])*\\1|[^\\s'\">=]+)", Pattern.CASE_INSENSITIVE),
                                                        false,
                                                        false,
                                                        null,
                                                        grammar(
                                                                "inside",
                                                                token(
                                                                        "punctuation",
                                                                        pattern(compile("^=")),
                                                                        pattern(compile("(^|[^\\\\])[\"']"), true)
                                                                ),
                                                                entity
                                                        )
                                                )
                                        ),
                                        token("punctuation", pattern(compile("\\/?>"))),
                                        token(
                                                "attr-name",
                                                pattern(
                                                        compile("[^\\s>\\/]+"),
                                                        false,
                                                        false,
                                                        null,
                                                        grammar(
                                                                "inside",
                                                                token("namespace", pattern(compile("^[^\\s>\\/:]+:")))
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                entity
        );
    }

    @NonNull
    public static Prism4j.Grammar clike() {
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
                        pattern(compile("\\b(?:if|else|while|do|for|return|in|instanceof|function|new|try|throw|catch|finally|null|break|continue)\\b"))
                ),
                token("boolean", pattern(compile("\\b(?:true|false)\\b"))),
                token(
                        "function",
                        pattern(compile("[a-z0-9_]+(?=\\()", Pattern.CASE_INSENSITIVE))
                ),
                token(
                        "number",
                        pattern(compile("\\b0x[\\da-f]+\\b|(?:\\b\\d+\\.?\\d*|\\B\\.\\d+)(?:e[+-]?\\d+)?", Pattern.CASE_INSENSITIVE))
                ),
                token(
                        "operator",
                        pattern(compile("--?|\\+\\+?|!=?=?|<=?|>=?|==?=?|&&?|\\|\\|?|\\?|\\*|\\/|~|\\^|%"))
                ),
                token("punctuation", pattern(compile("[{}\\[\\];(),.:]")))
        );
    }

    private Grammars() {
    }
}
