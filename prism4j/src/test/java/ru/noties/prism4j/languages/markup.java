package ru.noties.prism4j.languages;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import ru.noties.prism4j.Grammars;
import ru.noties.prism4j.Prism4j;
import ru.noties.prism4j.TestUtils;

@RunWith(Parameterized.class)
public class markup {

    @Parameterized.Parameters(name = "{0}")
    @NonNull
    public static Collection<Object> parameters() {
        return TestUtils.testFiles("markup");
    }

    private Prism4j prism4j;

    @Before
    public void before() {
        prism4j = new Prism4j();
    }

    private String file;

    public markup(@NonNull String file) {
        this.file = file;
    }

    @Test
    public void test() {
        final TestUtils.Case c = TestUtils.readCase(file);
        TestUtils.assertCase(c, prism4j.tokenize(c.input, Grammars.markup()));
    }
}
