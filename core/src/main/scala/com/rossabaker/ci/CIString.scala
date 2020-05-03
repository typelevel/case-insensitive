package com.rossabaker.ci

import cats.Show
import cats.kernel.{Hash, LowerBounded, Monoid, Order, PartialOrder}
import scala.math.Ordered

final class CIString private (override val toString: String) extends Ordered[CIString] {
  override def equals(that: Any): Boolean =
    that match {
      case that: CIString =>
        this.toString.equalsIgnoreCase(that.toString)
      case _ => false
    }

  override def hashCode(): Int = {
    def hashChar(c: Char) = c.toUpper.toLower.##
    toString.foldLeft(7)((acc, c) => acc * 31 + hashChar(c))
  }

  override def compare(that: CIString): Int =
    this.toString.compareToIgnoreCase(that.toString)
}

object CIString {
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

      // Show
      def show(t: CIString): String = t.toString
    }
}
