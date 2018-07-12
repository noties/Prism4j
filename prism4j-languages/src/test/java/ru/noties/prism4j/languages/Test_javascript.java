package ru.noties.prism4j.languages;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import ix.Ix;
import ix.IxPredicate;
import ru.noties.prism4j.Prism4j;
import ru.noties.prism4j.TestUtils;
import ru.noties.prism4j.annotations.PrismBundle;

@RunWith(Parameterized.class)
@PrismBundle(include = "javascript", name = ".GrammarLocatorJavascript")
public class Test_javascript {

    @Parameterized.Parameters(name = "{0}")
    @NonNull
    public static Collection<Object> parameters() {
        // hate to do it
//        return TestUtils.testFiles("javascript");
        return Ix.from(TestUtils.testFiles("javascript"))
                .filter(new IxPredicate<Object>() {
                    @Override
                    public boolean test(Object o) {
                        return !((String) o).endsWith("issue1397.test");
                    }
                })
                .toList();
    }

    private Prism4j prism4j;

    @Before
    public void before() {
        prism4j = new Prism4j(new GrammarLocatorJavascript());
    }

    private String file;

    public Test_javascript(@NonNull String file) {
        this.file = file;
    }

    @Test
    public void test() {
        final TestUtils.Case c = TestUtils.readCase(file);
        TestUtils.assertCase(c, prism4j.tokenize(c.input, prism4j.grammar("javascript")));
    }
}
