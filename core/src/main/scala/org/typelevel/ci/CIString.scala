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

/** A case-insensitive String.
  *
  * Comparisions are based on the case folded representation of the `String`
  * as defined by the Unicode standard. See [[CaseFoldedString]] for a full
  * discussion on those rules.
  *
  * @note This class differs from [[CaseFoldedString]] in that it keeps a
  *       reference to original input `String` in whatever form it was
  *       given. This makes [[CIString]] useful if you which to perform case
  *       insensitive operations on a `String`, but then recover the original,
  *       unaltered form. If you do not care about the original input form,
  *       and just want a single case insensitive `String` value, then
  *       [[CaseFoldedString]] is more efficient and you should consider using
  *       that directly.
  *
  * @param toString
  *   The original value the CI String was constructed with.
  */
final class CIString private (override val toString: String, val asCaseFoldedString: CaseFoldedString)
    extends Ordered[CIString]
    with Serializable {

  @deprecated(message = "Please provide a CaseFoldedString directly.", since = "1.3.0")
  private def this(toString: String) = {
    this(toString, CaseFoldedString(toString))
  }

  override def equals(that: Any): Boolean =
    that match {
      case that: CIString =>
        // Note java.lang.String.equalsIgnoreCase _does not_ handle all title
        // case unicode characters, so we can't use it here. See the tests for
        // an example.
        this.asCaseFoldedString == that.asCaseFoldedString
      case _ => false
    }

  override def hashCode(): Int =
    asCaseFoldedString.hashCode

  override def compare(that: CIString): Int =
    asCaseFoldedString.compare(that.asCaseFoldedString)

  def transform(f: String => String): CIString = CIString(f(toString))

  def isEmpty: Boolean = this.toString.isEmpty

  def nonEmpty: Boolean = this.toString.nonEmpty

  def trim: CIString = transform(_.trim)

  def length: Int = toString.length

  @deprecated("Use toString", "0.1.0")
  def value: String = toString
}

@suppressUnusedImportWarningForCompat
object CIString {

  def apply(value: String, useTurkicFolding: Boolean): CIString =
    new CIString(value, CaseFoldedString(value, useTurkicFolding))

  def apply(value: String): CIString =
    apply(value, false)

  def fromCaseFoldedString(value: CaseFoldedString): CIString =
    new CIString(value.toString, value)

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
