/*
 * Copyright 2020 Typelevel Contributors
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel.ci

import cats.Show
import cats.kernel.{Hash, LowerBounded, Monoid, Order, PartialOrder}
import java.io.Serializable
import org.typelevel.ci.compat._
import scala.math.Ordered

/** A case-insensitive String.
  *
  * Two CI strings are equal if and only if they are the same length, and each
  * corresponding character is equal after calling either `toUpper` or
  * `toLower`.
  *
  * Ordering is based on a string comparison after folding each
  * character to uppercase and then back to lowercase.
  *
  * All comparisons are insensitive to locales.
  *
  * @param toString The original value the CI String was constructed with.
  */
final class CIString private (override val toString: String)
    extends Ordered[CIString]
    with Serializable {
  override def equals(that: Any): Boolean =
    that match {
      case that: CIString =>
        this.toString.equalsIgnoreCase(that.toString)
      case _ => false
    }

  @transient private[this] var hash = 0
  override def hashCode(): Int = {
    if (hash == 0)
      hash = calculateHash
    hash
  }

  private[this] def calculateHash: Int = {
    var h = 17
    var i = 0
    val len = toString.length
    while (i < len) {
      // Strings are equal igoring case if either their uppercase or lowercase
      // forms are equal. Equality of one does not imply the other, so we need
      // to go in both directions. A character is not guaranteed to make this
      // round trip, but it doesn't matter as long as all equal characters
      // hash the same.
      h = h * 31 + toString.charAt(i).toUpper.toLower
      i += 1
    }
    h
  }

  override def compare(that: CIString): Int =
    this.toString.compareToIgnoreCase(that.toString)
}

@suppressUnusedImportWarningForCompat
object CIString {
  def apply(value: String): CIString = new CIString(value)

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
