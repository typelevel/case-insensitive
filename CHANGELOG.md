# Changelog

## v1.1.0 (2021-03-06)

* Add a `ci` interpolator and `ci` matcher, in the manner of standard library's `s`.

## v1.0.0 (2021-02-18)

### Cross-builds

* Add scala-3.0.0-RC1
* Drop scala-3.0.0-M2

### Dependencies

* cats-2.4.2
* scalacheck-1.15.3

## v1.0.0-RC2 (2021-01-13)

### Enhancements

* [#93](https://github.com/typelevel/case-insensitive/issues/93): Support for ScalaJS-1.x
* [#104](https://github.com/typelevel/case-insensitive/issues/104): Support for Scala 3.0.0-M2 and Scala-3.0.0-M3
* [#105](https://github.com/typelevel/case-insensitive/issues/104): Add `transform`, `isEmpty`, `nonEmpty`, and `trim` functions

### Dependencies

* cats-core-2.3.1
* scalacheck-1.15.1

## v0.3.0 (2020-05-21)

### Breaking changes

* [#17](https://github.com/typelevel/case-insensitive/issues/17): Changed name of Cats instances to reflect the new package. Because it's usually found implicitly, this change is unlikely to be noticed.

### Enhancements

* [#18](https://github.com/typelevel/case-insensitive/issues/18): `CIString` is now serializable

## v0.2.0 (2020-05-16)

case-insensitive is now a Typelevel project.

### Breaking changes

* Maven group ID changed from `com.rossabaker` to `org.typelevel`
* Package name changed from `com.rossabaker.ci` to `org.typelevel.ci`

### Dependency updates

* cats-2.1.1
* scalacheck-1.14.3

## v0.1.0 (2020-05-04)

* Initial release
