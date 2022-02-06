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

final class CIStringS private (
    override val toString: String,
    val asSimpleCaseFoldedString: SimpleCaseFoldedString)
    extends Serializable {

  override def equals(that: Any): Boolean =
    that match {
      case that: CIStringS =>
        asSimpleCaseFoldedString == that.asSimpleCaseFoldedString
      case _ =>
        false
    }

  override def hashCode(): Int =
    asSimpleCaseFoldedString.hashCode
}

object CIStringS {

  def apply(value: String): CIStringS =
    new CIStringS(
      value,
      SimpleCaseFoldedString(value)
    )

  val empty: CIStringS = apply("")

  implicit val hashAndOrderForCIStringS: Hash[CIStringS] with Order[CIStringS] =
    new Hash[CIStringS] with Order[CIStringS] {
      override def hash(x: CIStringS): Int =
        x.hashCode

      override def compare(x: CIStringS, y: CIStringS): Int =
        x.asSimpleCaseFoldedString.compare(y.asSimpleCaseFoldedString)
    }

  implicit val orderingForCIStringS: Ordering[CIStringS] =
    hashAndOrderForCIStringS.toOrdering

  implicit val showForCIStringS: Show[CIStringS] =
    Show.fromToString

  implicit val lowerBoundForCIStringS: LowerBounded[CIStringS] =
    new LowerBounded[CIStringS] {
      override val partialOrder: PartialOrder[CIStringS] =
        hashAndOrderForCIStringS

      override val minBound: CIStringS =
        empty
    }

  implicit val monoidForCIStringS: Monoid[CIStringS] =
    new Monoid[CIStringS] {
      override val empty: CIStringS = CIStringS.empty

      override def combine(x: CIStringS, y: CIStringS): CIStringS =
        CIStringS(x.toString + y.toString)

      override def combineAll(xs: IterableOnce[CIStringS]): CIStringS = {
        val sb: StringBuilder = new StringBuilder
        xs.iterator.foreach(cfs => sb.append(cfs.toString))
        CIStringS(sb.toString)
      }
    }
}
