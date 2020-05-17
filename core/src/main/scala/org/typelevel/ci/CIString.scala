/*
 * Copyright 2020 Typelevel Contributors
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel.ci

import cats.Show
import cats.kernel.{Hash, LowerBounded, Monoid, Order, PartialOrder}
import org.typelevel.ci.compat._
import scala.math.Ordered
import scala.util.hashing.MurmurHash3.{finalizeHash, mix, mixLast}

/**
  * A case-insensitive String.
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
final class CIString private (override val toString: String) extends Ordered[CIString] {
  import CIString._

  override def equals(that: Any): Boolean =
    that match {
      case that: CIString =>
        this.toString.equalsIgnoreCase(that.toString)
      case _ => false
    }

  private[this] var hash = 0
  override def hashCode(): Int = {
    // 1 in 2^32 strings will hash to UninitializedHash and always be
    // recalculated, but this is fast and lightweight for the other 2^32-1.
    if (hash == UninitializedHash)
      hash = calculateHash
    hash
  }

  private[this] def calculateHash: Int = {
    // This is just MurmurHash3.stringHashing, except each character is
    // normalized with a .toUpper.toLower round trip before mixing.
    var h = HashSeed
    var i = 0
    val len = toString.length
    def ncharAt(i: Int) = toString.charAt(i).toUpper.toLower
    while (i + 1 < len) {
      val data = (ncharAt(i) << 16) + ncharAt(i + 1)
      h = mix(h, data)
      i += 2
    }
    if (i < len) h = mixLast(h, ncharAt(i).toInt)
    finalizeHash(h, len)
    h
  }

  override def compare(that: CIString): Int =
    this.toString.compareToIgnoreCase(that.toString)
}

@suppressUnusedImportWarningForCompat
object CIString {
  private final val UninitializedHash = 0
  private final val HashSeed = 0xd9cff017 // "CIString".##

  def apply(value: String): CIString = new CIString(value)

  val empty = CIString("")

  implicit val catsInstancesForComRossbakerCIString: Order[CIString]
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
