# 1.1.0
* Fix issue when cloning a grammar was causing stack overflow (recursive copying)
* Add language support:
* * `groovy` (no string interpolation)
* * `markdown`
* * `scala`
* * `swift`

# 1.0.1
* Fix for `javascript` and `yaml` grammar definitions to work on Android (escape `}` char in regex's)