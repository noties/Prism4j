package ru.noties.prism4j.languages;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import ru.noties.prism4j.Prism4j;
import ru.noties.prism4j.TestUtils;
import ru.noties.prism4j.annotations.PrismBundle;

@RunWith(Parameterized.class)
@PrismBundle(include = {"markup", "css"}, name = ".GrammarLocatorMarkupCss")
public class Test_markup_css {

    @Parameterized.Parameters(name = "{0}")
    @NonNull
    public static Collection<Object> parameters() {
        return TestUtils.testFiles("markup!+css");
    }

    private Prism4j prism4j;

    @Before
    public void before() {
        prism4j = new Prism4j(new GrammarLocatorMarkupCss());
    }

    private String file;

    public Test_markup_css(@NonNull String file) {
        this.file = file;
    }

    @Test
    public void test() {
        final TestUtils.Case c = TestUtils.readCase(file);

        // yes, now we have to request manually css (as without it won't _modify_ markup to accept styles)
        prism4j.grammar("css");
        TestUtils.assertCase(c, prism4j.tokenize(c.input, prism4j.grammar("markup")));
    }
}
