# Prism4j

Simplified Java clone of [prism-js](https://github.com/PrismJS/prism). No rendering, no themes,
no hooks. But still _a_ language parsing.

## Api

## Bundler

## Contributing

If you want to contribute to this project porting grammar definitions would be the best start.
But before you begin please create an issue with `language-support` tag so others can see that
a language is being worked at. This issue will be also the great place to discuss things that
could arise whilst in process.

Language definitions are at the `/languages` folder (go down the `ru.noties.prism4j.languages`
package to find the files). A new file should follow simple naming convention:
`Prism_{real_language_name}.java`. So, a definition for the `json` would be `Prism_json.java`.

In order to provide `bundler` with meta-information about a language `@Aliases`, `@Extend`
and `@Modify` annotations can be used:

* `@Aliases` specifies what aliases a language has. For example `markup` language has
these: `@Aliases({"html", "xml", "mathml", "svg"})`. So when a `GrammarLocator` will be
asked for a `svg` language the `markup` will be returned.

* `@Extend` annotation indicates if a language definition is a sibling of another one.
So even if a parent language is not included in `@PrismBundle` it will be added to a project
anyway. For example, `c`:

```java
@Extend("clike")
public class Prism_c {}
```

* `@Modify` annotation makes sure that if a language definition modifies another one,
modified language will be processed before returning to a caller. This does not include a
language that is being modified to a project. But if it's present, it will be modified.
For example, `css`:

```java
@Modify("markup")
public class Prism_css {}
```

`@Modify` accepts an array of language names

---

After you are done (haha!) with a language definition please make sure that you also move
test cases from [prism-js]() for the project (for newly added language of cause). Thankfully
just a byte of work required here as `prism4j-languages` module understands native format
of _prism-js_ test cases (that are ending with `*.test`). Please inspect test folder of the
`prism4j-languages` module for further info.

Then, if you run:
```bash
./gradlew :prism4j-languages:test
```

and all tests pass (including your newly added), then it's _safe_ to issue a pull request. **Good job! 👏**

<i><sup>*</sup> BTW, if you know how can test classes be generated via a simple configuration
step, so there is no need to actually manually create them, it would really cool if you'd share
this. Java regex grammar parsers would not forget you ever! $-$</i>

### Grammar definitions

```java
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static ru.noties.prism4j.Prism4j.grammar;
import static ru.noties.prism4j.Prism4j.pattern;
import static ru.noties.prism4j.Prism4j.token;

@Aliases("jsonp")
public class Prism_json {

  @NonNull
  public static Prism4j.Grammar create(@NonNull Prism4j prism4j) {
    return grammar(
      "json",
      token("property", pattern(compile("\"(?:\\\\.|[^\\\\\"\\r\\n])*\"(?=\\s*:)", CASE_INSENSITIVE))),
      token("string", pattern(compile("\"(?:\\\\.|[^\\\\\"\\r\\n])*\"(?!\\s*:)"), false, true)),
      token("number", pattern(compile("\\b0x[\\dA-Fa-f]+\\b|(?:\\b\\d+\\.?\\d*|\\B\\.\\d+)(?:[Ee][+-]?\\d+)?"))),
      token("punctuation", pattern(compile("[{}\\[\\]);,]"))),
      token("operator", pattern(compile(":"))),
      token("boolean", pattern(compile("\\b(?:true|false)\\b", CASE_INSENSITIVE))),
      token("null", pattern(compile("\\bnull\\b", CASE_INSENSITIVE)))
    );
  }
}
```