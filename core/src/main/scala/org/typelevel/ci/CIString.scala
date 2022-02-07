/*
 * Copyright 2020 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.ci

import cats.Show
import cats.kernel.{Hash, LowerBounded, Monoid, Order, PartialOrder}
import java.io.Serializable
import org.typelevel.ci.compat._
import scala.math.Ordered

/** A case insensitive representation of a `String`.
  *
  * There are several different ways to define a case insensitive match with Unicode. According to
  * the Unicode standard, this is the "most correct" definition. If you are just looking for a case
  * insensitive `String`, you should either use this or [[CanonicalFullCaseFoldedString]].
  *
  * The only difference is whether or not you want to keep track of the original input `String`
  * value. If you don't care about that, then [[CanonicalFullCaseFoldedString]] uses less memory and
  * is likely ''slightly'' faster for most operations.
  *
  * {{{
  * scala> CIString("ß")
  * val res0: org.typelevel.ci.CIString = ß
  *
  * scala> CanonicalFullCaseFoldedString("ß")
  * val res1: org.typelevel.ci.CanonicalFullCaseFoldedString = ss
  *
  * scala> res0.asCanonicalFullCaseFoldedString == res1
  * val res2: Boolean = true
  *
  * scala> res0.toString
  * val res3: String = ß
  *
  * scala> res1.toString
  * val res4: String = ss
  *
  * scala> res0.asCanonicalFullCaseFoldedString.toString
  * val res5: String = ss
  * }}}
  *
  * @see
  *   [[https://www.unicode.org/versions/Unicode14.0.0/ch03.pdf#G34145 Unicode Caseless Matching]]
  */
final class CIString private (override val toString: String)
    extends Ordered[CIString]
    with Serializable {

  /** The [[CanonicalFullCaseFoldedString]] representation of this `String`.
    *
    * This is the input `String`, case folded using full Unicode case folding (without the Turkic
    * rules), and normalized for Unicode canonical caseless matching.
    *
    * For any two given Unicode text value, they are considered canonically caseless equivalent to
    * each other if they both result in this [[CanonicalFullCaseFoldedString]].
    */
  lazy val asCanonicalFullCaseFoldedString: CanonicalFullCaseFoldedString =
    CanonicalFullCaseFoldedString(this.toString)

  override def equals(that: Any): Boolean =
    that match {
      case that: CIString =>
        asCanonicalFullCaseFoldedString == that.asCanonicalFullCaseFoldedString
      case _ => false
    }

  override def hashCode(): Int =
    this.asCanonicalFullCaseFoldedString.hashCode

  override def compare(that: CIString): Int =
    Order[CanonicalFullCaseFoldedString].compare(
      asCanonicalFullCaseFoldedString,
      that.asCanonicalFullCaseFoldedString)

  def transform(f: String => String): CIString = CIString(f(toString))

  def isEmpty: Boolean = this.toString.isEmpty

  def nonEmpty: Boolean = this.toString.nonEmpty

  def trim: CIString = transform(_.trim)

  @deprecated(
    message =
      "Please use asCanonicalFullCaseFoldedString.length or toString.length, depending on your use case, instead. CIString represents a Unicode canonical caseless string with full case folding. Full case folding can change the length (in terms of number of Char values) of a String. This makes length on CIString confusing to use because it is unclear which length this method refers to. As 1.3.0 it is defined to refer to the length of the full case folded representation of the String, since this will be the same for all input Strings.",
    since = "1.3.0")
  def length: Int = asCanonicalFullCaseFoldedString.toString.length

  @deprecated("Use toString", "0.1.0")
  def value: String = toString
}

@suppressUnusedImportWarningForCompat
object CIString {

  def apply(value: String): CIString =
    new CIString(value)

  val empty = CIString("")

  implicit val catsInstancesForOrgTypelevelCIString: Order[CIString]
    with Hash[CIString]
    with LowerBounded[CIString]
    with Monoid[CIString]
    with Show[CIString] =
    new Order[CIString]
      with Hash[CIString]
      with LowerBounded[CIString]
      with Monoid[CIString]
      with Show[CIString] { self =>
      // Order
      def compare(x: CIString, y: CIString): Int = x.compare(y)

      // Hash
      def hash(x: CIString): Int = x.hashCode

      // LowerBounded
      def minBound: CIString = empty
      val partialOrder: PartialOrder[CIString] = self

      // Monoid
      val empty = CIString.empty
      def combine(x: CIString, y: CIString) = CIString(x.toString + y.toString)
      override def combineAll(xs: IterableOnce[CIString]): CIString = {
        val sb = new StringBuilder
        xs.iterator.foreach(ci => sb.append(ci.toString))
        CIString(sb.toString)
      }

      // Show
      def show(t: CIString): String = t.toString
    }
}
