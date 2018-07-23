package ru.noties.prism4j;

import org.junit.Before;
import org.junit.Test;

import ix.Ix;
import ix.IxConsumer;
import ix.IxFunction;
import ru.noties.prism4j.annotations.PrismBundle;

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
                .orderBy(new IxFunction<String, Comparable>() {
                    @Override
                    public Comparable apply(String s) {
                        return s;
                    }
                })
                .foreach(new IxConsumer<String>() {
                    @Override
                    public void accept(String s) {
                        GrammarUtils.clone(prism4j.grammar(s));
                    }
                });
    }
}
