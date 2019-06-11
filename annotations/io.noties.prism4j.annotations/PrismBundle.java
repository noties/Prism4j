package io.noties.prism4j.annotations;

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
     * since 1.1.0 provides default value because of the {@link #includeAll()} option
     */
    String[] include() default {};

    /**
     * Specifies the name of generated GrammarLocator. Can be fully qualified name (with package info:
     * `my.package.MyGrammarLocator`) or a simple name (starting with a dot `.MyGrammarLocator`) to
     * place generated class in the same package as annotated element
     *
     * @since 1.1.0 (previously just `name`)
     */
    String grammarLocatorClassName() default ".GrammarLocatorDef";

    /**
     * Argument to include all _currently_ supported languages. Please note that if this option is
     * true, then {@link #include()} values won\'t be considered
     *
     * @since 1.1.0
     */
    boolean includeAll() default false;
}
