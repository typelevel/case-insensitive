# case-insensitive - Case Insensitive structures for Scala

[![Actions Status](https://github.com/typelevel/case-insensitive/workflows/CI/badge.svg)](https://github.com/typelevel/case-insensitive/actions)[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.typelevel/case-insensitive_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.typelevel/case-insensitive_2.13) ![Code of Conduct](https://img.shields.io/badge/Code%20of%20Conduct-Scala-blue.svg)

`case-insensitive` provides a case-insensitive string type for Scala.
Design goals are:

* light weight
* locale independence
* stability
* integration with Cats

Case-insensitive strings are useful as map keys for case-insensitive lookups.
They are also useful in case classes whose equality semantics are case insensitive for certain string fields.

## Quick Start

To use case-insensitive in an existing SBT project with Scala 2.12 or a later version, add the following dependencies to your
`build.sbt` depending on your needs:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "case-insensitive" % "<version>"
)
```

## Basic Usage

This library provides a `CIString` type.

```scala mdoc:silent
import org.typelevel.ci._
```

Construct case-insensitive strings with the apply method:

```scala mdoc
val hello = CIString("Hello")
```

More concisely, use the `ci` interpolator:

```scala mdoc
val name = "Otis"
val greeting = ci"Hello, ${name}"
```

Get the original string value with `toString`:

```scala mdoc
val original = hello.toString
```

`CIString`s are equal according to the rules of [equalsIgnoreCase]:

```scala mdoc
assert(CIString("hello") == CIString("Hello"))
```

This means that strings that change length when uppercased are not equal when wrapped in `CIString`:

```scala mdoc
assert("ß".toUpperCase == "SS")
assert(CIString("ß") != CIString("SS"))
```

It also means that comparisons are independent of the runtime's default locales:

```scala mdoc:invisible
val oldLocale = java.util.Locale.getDefault
```

```scala mdoc
import java.util.Locale

Locale.setDefault(Locale.ROOT)
assert("i".toUpperCase == "I")
assert(CIString("i") == CIString("I"))

Locale.setDefault(Locale.forLanguageTag("tr"))
assert("i".toUpperCase != "I")
assert(CIString("i") == CIString("I"))
```

```scala mdoc:invisible
java.util.Locale.setDefault(oldLocale)
```

We also implement `Ordering`, based on the rules of [compareToIgnoreCase]:

```scala mdoc
assert("a" > "B")
assert(CIString("a") < CIString("B"))
```

You can also match strings with the `ci` globbing matcher.  It works like `s`:

```scala mdoc
val ci"HELLO, ${appellation}" = ci"Hello, Alice"
```

## Cats integration

We provide instances of various Cats type classes. The most exciting of these are [`Eq`] and [`Monoid`]:

```scala mdoc:silent
import cats.implicits._

assert(CIString("Hello") === CIString("Hello"))
val combined = CIString("case") |+| CIString("-") |+| CIString("insensitive")
```

## Testing package

The case-insensitive-testing module provides instances of [Scalacheck's][Scalacheck] `Arbitrary` and `Cogen` for property testing models that include `CIString`:

Add the following dependency:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "case-insensitive-testing" % "<version>"
)
```

Import the arbitraries:

```scala mdoc:silent
import org.typelevel.ci.testing.arbitraries._
import org.scalacheck.Prop
```

And use them in your property tests:

```scala mdoc
Prop.forAll { (x: CIString) => x == x }.check()
```

## FAQ

### Why pay for a wrapper when there is `equalsIgnoreCase`?

This type integrates cleanly with various data structures that depend on universal equality and hashing, such as `scala.Map` and case classes.
These data structures otherwise require specialized implementations for case-insensitivity.

### Why pay for a wrapper when you can fold case before storing?

Sometimes it's useful to preserve the original value.

Also, the type of `String` doesn't change when folding case, making this an error-prone solution.

### Why not polymorphic?

Haskell's [Data.CaseInsensitive] provides a polymorphic `CI` type similar to:

```scala mdoc
class CI[A](val original: A) {
  override def equals(that: Any) = ???
}
```

The only functions available to us on `A` are those that apply to `Any`, which precludes us from using optimized equality operations such as [equalsIgnoreCase].  
The best we can do is (lazily) store a case-folded version of `A` on construction and use a slower `equals` check on that.

Haskell has myriad string-like types, which provide a rich set of instances.  In Scala, restricting ourselves to immutable types, it's hard to imagine any other than `CI[String]` and `CI[Char]`.

### Okay, then why is there no `CIChar` here?

I don't need it yet.  If you do, pull requests welcome!

[equalsIgnoreCase]: https://docs.oracle.com/javase/8/docs/api/java/lang/String.html#equalsIgnoreCase-java.lang.String-
[compareToIgnoreCase]: https://docs.oracle.com/javase/8/docs/api/java/lang/String.html#compareToIgnoreCase-java.lang.String-
[`Eq`]: https://typelevel.org/cats/typeclasses/eq.html
[`Monoid`]: https://typelevel.org/cats/typeclasses/monoid.html
[Scalacheck]: https://www.scalacheck.org/
[Data.CaseInsensitive]: https://hackage.haskell.org/package/case-insensitive-1.2.1.0
