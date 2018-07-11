package ru.noties.prism4j.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PrismBundle {

    /**
     * Array of languages to be included in final bundle. Please do not use aliases but _real_ language names.
     * For example `markup` instead of `svg` (as there is no `svg` language` defined)
     */
    String[] include();

    /**
     * Specifies the name of generated GrammarLocator. Can be fully qualified name (with package info:
     * `my.package.MyGrammarLocator`) or a simple name (starting with a dot `.MyGrammarLocator`) to
     * place generated class in the same package as annotated element
     */
    String name() default ".GrammarLocatorDef";
}
