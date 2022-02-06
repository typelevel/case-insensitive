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

@deprecated(
  message =
    "Please use either CIStringCF, CIStringCS, or CIStringS instead. CIString/CIStringS implement Unicode default caseless matching on simple case folded strings. For most applications you probably want to use CIStringCF which implements Unicode canonical caseless matching on full case folded strings.",
  since = "1.3.0")
final class CIString private (override val toString: String, val asCIStringS: CIStringS)
    extends Ordered[CIString]
    with Serializable {

  @deprecated(message = "Please provide a CaseFoldedString directly.", since = "1.3.0")
  private def this(toString: String) =
    this(toString, CIStringS(toString))

  override def equals(that: Any): Boolean =
    that match {
      case that: CIString =>
        this.asCIStringS == that.asCIStringS
      case _ => false
    }

  override def hashCode(): Int =
    this.asCIStringS.hashCode

  override def compare(that: CIString): Int =
    Order[CIStringS].compare(asCIStringS, that.asCIStringS)

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

  @deprecated(
    message =
      "Please use either CIStringCF, CIStringCS, or CIStringS instead. CIString/CIStringS implement Unicode default caseless matching on simple case folded strings. For most applications you probably want to use CIStringCF which implements Unicode canonical caseless matching on full case folded strings.",
    since = "1.3.0")
  def apply(value: String): CIString =
    new CIString(value, CIStringS(value))

  @deprecated(
    message =
      "Please use either CIStringCF, CIStringCS, or CIStringS instead. CIString/CIStringS implement Unicode default caseless matching on simple case folded strings. For most applications you probably want to use CIStringCF which implements Unicode canonical caseless matching on full case folded strings.",
    since = "1.3.0")
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
