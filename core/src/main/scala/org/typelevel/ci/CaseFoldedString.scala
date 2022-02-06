package org.typelevel.ci

import cats._
import cats.kernel.LowerBounded
import org.typelevel.ci.compat._
import scala.annotation.tailrec

/** A case folded `String`. This is a `String` which has been converted into a
  * state which is suitable for case insensitive matching under the Unicode
  * standard.
  *
  * This type differs from [[CIString]] in that it does ''not'' retain the
  * original input `String` value. That is, this is a destructive
  * transformation. You should use [[CaseFoldedString]] instead of
  * [[CIString]] when you only want the case insensitive `String` and you
  * never want to return the `String` back into the input value. In such cases
  * [[CaseFoldedString]] will be more efficient than [[CIString]] as it only
  * has to keep around a single `String` in memory.
  *
  * Case insensitive `String` values under Unicode are not always intuitive,
  * especially on the JVM. There are three character cases to consider, lower
  * case, upper case, and title case, and not all Unicode codePoints have all
  * 3, some only have 2, some only 1. For some codePoints, the JRE standard
  * operations don't always work as you'd expect.
  *
  * {{{
  * scala> val codePoint: Int = 8093
  * val codePoint: Int = 8093
  *
  * scala> new String(Character.toChars(codePoint))
  * val res0: String = ᾝ
  *
  * scala> res0.toUpperCase
  * val res1: String = ἭΙ
  *
  * scala> res0.toUpperCase.toLowerCase == res0.toLowerCase
  * val res2: Boolean = false
  *
  * scala> Character.getName(res0.head)
  * val res3: String = GREEK CAPITAL LETTER ETA WITH DASIA AND OXIA AND PROSGEGRAMMENI
  *
  * scala> res0.toUpperCase.toLowerCase.equalsIgnoreCase(res0.toLowerCase)
  * val res4: Boolean = false
  * }}}
  *
  * In this example, given the Unicode character \u1f9d, converting it to
  * upper case, then to lower case, is not equal under normal String
  * equality. `String.equalsIgnoreCase` also does not work correctly by the
  * Unicode standard.
  *
  * Making matters more complicated, for certain Turkic languages, the case
  * folding rules change. See the Unicode standard for a full discussion of
  * the topic.
  *
  * @note For most `String` values the `toString` form of this is lower case
  *       (when the given character has more than one case), but this is not
  *       always the case. Certain Unicode scripts have exceptions to this and
  *       will be case folded into upper case. If you want/need an only lower
  *       case `String`, you should call `.toString.toLowerCase`.
  *
  * @see [[https://www.unicode.org/versions/Unicode14.0.0/ch05.pdf#G21790]]
  */
final case class CaseFoldedString private (override val toString: String) extends AnyVal {

  def isEmpty: Boolean = toString.isEmpty

  def nonEmpty: Boolean = !isEmpty

  def length: Int = toString.length

  def size: Int = length

  def trim: CaseFoldedString =
    CaseFoldedString(toString.trim)

  private final def copy(toString: String): CaseFoldedString =
    CaseFoldedString(toString)
}

object CaseFoldedString {

  /** Create a [[CaseFoldedString]] from a `String`.
    *
    * @param turkicFoldingRules if `true`, use the case folding rules for
    *                           applicable to some Turkic languages.
    */
  def apply(value: String, turkicFoldingRules: Boolean): CaseFoldedString = {
    val builder: java.lang.StringBuilder = new java.lang.StringBuilder(value.length * 3)
    val foldCodePoint: Int => Array[Int] =
      if (turkicFoldingRules) {
        CaseFolds.turkicFullCaseFoldedCodePoints
      } else {
        CaseFolds.fullCaseFoldedCodePoints
      }

    @tailrec
    def loop(index: Int): String =
      if (index >= value.length) {
        builder.toString
      } else {
        val codePoint: Int = value.codePointAt(index)
        foldCodePoint(codePoint).foreach(c => builder.appendCodePoint(c))
        val inc: Int = if (codePoint >= 0x10000) 2 else 1
        loop(index + inc)
      }

    new CaseFoldedString(loop(0))
  }

  /** Create a [[CaseFoldedString]] from a `String`.
    *
    * @note This factory method does ''not'' use the Turkic case folding
    *       rules. For the majority of languages this is the correct method of
    *       case folding. If you know your `String` is specific to one of the
    *       Turkic languages which use special case folding rules, you can use
    *       the secondary factory method to enable case folding under those
    *       rules.
    */
  def apply(value: String): CaseFoldedString =
    apply(value, false)

  val empty: CaseFoldedString =
    CaseFoldedString("")

  implicit val hashAndOrderForCaseFoldedString: Hash[CaseFoldedString] with Order[CaseFoldedString] =
    new Hash[CaseFoldedString] with Order[CaseFoldedString] {
      override def hash(x: CaseFoldedString): Int =
        x.hashCode

      override def compare(x: CaseFoldedString, y: CaseFoldedString): Int =
        x.compare(y)
    }

  implicit val orderingForCaseFoldedString: Ordering[CaseFoldedString] =
    hashAndOrderForCaseFoldedString.toOrdering

  implicit val showForCaseFoldedString: Show[CaseFoldedString] =
    Show.fromToString

  implicit val lowerBoundForCaseFoldedString: LowerBounded[CaseFoldedString] =
    new LowerBounded[CaseFoldedString] {
      override val partialOrder: PartialOrder[CaseFoldedString] =
        hashAndOrderForCaseFoldedString

      override val minBound: CaseFoldedString =
        empty
    }

  implicit val monoidForCaseFoldedString: Monoid[CaseFoldedString] =
    new Monoid[CaseFoldedString] {
      override val empty: CaseFoldedString = CaseFoldedString.empty

      override def combine(x: CaseFoldedString, y: CaseFoldedString): CaseFoldedString =
        new CaseFoldedString(x.toString + y.toString)

      override def combineAll(xs: IterableOnce[CaseFoldedString]): CaseFoldedString = {
        val sb: StringBuilder = new StringBuilder
        xs.iterator.foreach(cfs => sb.append(cfs.toString))
        new CaseFoldedString(sb.toString)
      }
    }
}
