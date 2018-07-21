# Prism4j

Simplified Java clone of [prism-js](https://github.com/PrismJS/prism). No rendering, no themes,
no hooks, no plugins. But still _a_ language parsing. Primary aim of this library is to provide a _tokenization_ strategy of arbitrary syntaxes for later processing. Works on Android (great with [Markwon](https://github.com/noties/Markwon) - markdown display library). 

## Core

Core module `prism4j` is a lightweight module that comes with API (no language definitions).

[![prism4j](https://img.shields.io/maven-central/v/ru.noties/prism4j.svg?label=prism4j)](http://search.maven.org/#search|ga|1|g%3A%22ru.noties%22%20AND%20a%3A%22prism4j%22)

```groovy
implementation 'ru.noties:prism4j:{latest_version}'
```

```java
final Prism4j prism4j = new Prism4j(new MyGrammarLocator());
final Grammar grammar = prism4j.grammar("json");
if (grammar != null) {
    final List<Node> nodes = prism4j.tokenize(code, grammar);
    final AbsVisitor visitor = new AbsVisitor() {
            @Override
            protected void visitText(@NonNull Prism4j.Text text) {
                // raw text
                text.literal();
            }

            @Override
            protected void visitSyntax(@NonNull Prism4j.Syntax syntax) {
                // type of the syntax token
                syntax.type();
                visit(syntax.children());
            }
        };
    visitor.visit(nodes);
}
```

Where `MyGrammarLocator` can be as simple as:

```java
public class MyGrammarLocator implements GrammarLocator {

    @Nullable
    @Override
    public Prism4j.Grammar grammar(@NonNull Prism4j prism4j, @NonNull String language) {
        switch (language) {

            case "json":
                return Prism_json.create(prism4j);

            // everything else is omitted

            default:
                return null;
        }
    }
}
```

And language definition:

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

## Bundler

In order to simplify adding language definitions to your project there is a special module
called `prism4j-bundler` that will automatically add requested languages.

[![prism4j-bundler](https://img.shields.io/maven-central/v/ru.noties/prism4j-bundler.svg?label=prism4j-bundler)](http://search.maven.org/#search|ga|1|g%3A%22ru.noties%22%20AND%20a%3A%22prism4j-bundler%22)

```groovy
annotationProcessor 'ru.noties:prism4j-bundler:{latest-version}'
```

Please note that `bundler` can add languages that are _ported_ (see `./languages` folder for the list).
Currently it supports:
* `brainf*ck`
* `c`
* `clike`
* `clojure`
* `cpp`
* `csharp` (`dotnet`)
* `css` (+`css-extras`)
* `dart`
* `git`
* `go`
* `groovy` (no string interpolation)
* `java`
* `javascript` (`js`)
* `json` (`jsonp`)
* `kotlin`
* `latex`
* `makefile`
* `markdown`
* `markup` (`xml`, `html`, `mathml`, `svg`)
* `python`
* `sql`
* `yaml`

Please see `Contributing` section if you wish to port a language.

```java
@PrismBundle(
    includes = { "clike", "java", "c" },
    name = ".MyGrammarLocator"
)
public class MyClass {}
```

You can have multiple language bundles, just annotate different classes in your project. There are
no special requirements for a class to be annotated (in can be any class in your project).

* `includes` - indicates what supported languages to add to your project. Please use _real_
language name (not an alias). So `javascript` instead of `js`; `markup` instead of `xml`.

* `name` - is the Java class name of generated `GrammarLocator`. It can start with a `dot` to
 put generated `GrammarLocator` to the same package as annotated element. Or be fully qualified Java name (starting with a package).

### !important
**NB** generated `GrammarLocator` will create languages when they are requested (aka _lazy_ loading). Make sure this works for you by keeping as is or by manually triggering language creation via `prism4j.grammar("my-language");` when convenient at runtime.

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
```java
@Aliases({"html", "xml", "mathml", "svg"})
public class Prism_markup {}
```

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
test cases from [prism-js](https://github.com/PrismJS/prism) for the project (for newly added
language of cause). Thankfully just a byte of work required here as `prism4j-languages` module
understands native format of _prism-js_ test cases (that are ending with `*.test`).
Please inspect test folder of the `prism4j-languages` module for further info. In short: copy test cases from `prism-js` project (the whole folder for specific language) into `prism4j-languages/src/test/resources/languages/` folder. 

Then, if you run:
```bash
./gradlew :prism4j-languages:test
```

and all tests pass (including your newly added), then it's _safe_ to issue a pull request. **Good job!**

### Important note about regex for contributors

As this project _wants_ to work on Android, your regex's patterns must have `}` symbol escaped (`\\}`). Yes, _an_ IDE will warn you that this escape is not needed, but do not believe it. Pattern just won't compile at runtime (Android). I wish this could be unit-**tested** but unfortunately Robolectric compiles just fine (no surprise actually).



## License

```
  Copyright 2018 Dimitry Ivanov (mail@dimitryivanov.ru)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```
