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

import cats._
import cats.kernel._
import cats.syntax.all._
import scala.annotation.tailrec

final case class SimpleCaseFoldedString private (override val toString: String) extends AnyVal

object SimpleCaseFoldedString {
  def apply(value: String): SimpleCaseFoldedString =
    new SimpleCaseFoldedString(CaseFolding.simpleCaseFoldString(value))

  val empty: SimpleCaseFoldedString =
    apply("")

  implicit val hashAndOrderForSimpleCaseFoldedString
      : Hash[SimpleCaseFoldedString] with Order[SimpleCaseFoldedString] =
    new Hash[SimpleCaseFoldedString] with Order[SimpleCaseFoldedString] {
      override def hash(x: SimpleCaseFoldedString): Int =
        x.hashCode

      override def compare(x: SimpleCaseFoldedString, y: SimpleCaseFoldedString): Int =
        x.toString.compare(y.toString)
    }

  implicit val orderingForSimpleCaseFoldedString: Ordering[SimpleCaseFoldedString] =
    hashAndOrderForSimpleCaseFoldedString.toOrdering

  implicit val showForSimpleCaseFoldedString: Show[SimpleCaseFoldedString] =
    Show.fromToString

  implicit val lowerBoundForSimpleCaseFoldedString: LowerBounded[SimpleCaseFoldedString] =
    new LowerBounded[SimpleCaseFoldedString] {
      override val partialOrder: PartialOrder[SimpleCaseFoldedString] =
        hashAndOrderForSimpleCaseFoldedString

      override val minBound: SimpleCaseFoldedString =
        empty
    }

  implicit val monoidForSimpleCaseFoldedString: Monoid[SimpleCaseFoldedString] =
    new Monoid[SimpleCaseFoldedString] {
      override val empty: SimpleCaseFoldedString = SimpleCaseFoldedString.empty

      override def combine(
          x: SimpleCaseFoldedString,
          y: SimpleCaseFoldedString): SimpleCaseFoldedString =
        SimpleCaseFoldedString(x.toString + y.toString)

      override def combineAll(xs: IterableOnce[SimpleCaseFoldedString]): SimpleCaseFoldedString = {
        val sb: StringBuilder = new StringBuilder
        xs.iterator.foreach(cfs => sb.append(cfs.toString))
        SimpleCaseFoldedString(sb.toString)
      }
    }
}
