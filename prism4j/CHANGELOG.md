# 1.1.0
* Fix issue when cloning a grammar was causing stack overflow (recursive copying)
* Fix issue with recursive toString in Grammar, Token adn Pattern
* Add `includeAll` option for `@PrismBundle`
* Rename `name` -> `grammarLocatorClassName` in `@PrismBundle`
* Allow `include` option to be empty in `@PrismBundle` in case of `includeAll` is set
* add `languages()` method to `GrammarLocator` to return all included languages