package ru.noties.prism4j.zzz;

import com.google.gson.Gson;

import ru.noties.prism4j.GrammarUtils;
import ru.noties.prism4j.Prism4j;
import ru.noties.prism4j.annotations.PrismBundle;

@PrismBundle(include = {"css", "markup"}, name = ".ZZZLocator")
public class myClass {

    public static void main(String[] args) {

        final ZZZLocator locator = new ZZZLocator();
        final Prism4j prism4j = new Prism4j(locator);

//        if (true) {
//            System.out.printf("attr-value: %s%n", GrammarUtils.findToken(prism4j.grammar("markup"), "tag/attr-value"));
//            return;

//        if (true) {
////            final Prism4j.Token token = GrammarUtils.findToken(prism4j.grammar("markup"), "tag");
//            prism4j.grammar("css");
//            System.out.printf("inside: %s%n", toJson(GrammarUtils.findFirstInsideGrammar(GrammarUtils.findToken(prism4j.grammar("markup"), "tag"))));
//            return;
//        }
//        }

//        prism4j.grammar("css");

        System.out.printf("markup: %n%s%n", new Gson().toJson(prism4j.grammar("markup")));
    }

    private static String toJson(Object o) {
        return new Gson().toJson(o);
    }
}
