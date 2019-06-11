package io.noties.prism4j;

import org.junit.Before;
import org.junit.Test;

import ix.Ix;
import ix.IxConsumer;
import ix.IxFunction;
import io.noties.prism4j.annotations.PrismBundle;

@PrismBundle(includeAll = true, grammarLocatorClassName = ".GrammarLocatorGrammarUtils")
public class GrammarUtilsTest {

    private GrammarLocator grammarLocator;
    private Prism4j prism4j;

    @Before
    public void before() {
        grammarLocator = new GrammarLocatorGrammarUtils();
        prism4j = new Prism4j(grammarLocator);
    }

    @Test
    public void clone_grammar() {

        Ix.from(grammarLocator.languages())
                .orderBy(new IxFunction<String, String>() {
                    @Override
                    public String apply(String s) {
                        return s;
                    }
                })
                .foreach(new IxConsumer<String>() {
                    @Override
                    public void accept(String s) {
                        final Prism4j.Grammar grammar = prism4j.grammar(s);
                        if (grammar != null) {
                            System.err.printf("cloning language: %s%n", s);
                            GrammarUtils.clone(grammar);
                        }
                    }
                });
    }
}
