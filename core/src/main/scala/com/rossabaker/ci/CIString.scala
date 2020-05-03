package com.rossabaker.ci

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
}
