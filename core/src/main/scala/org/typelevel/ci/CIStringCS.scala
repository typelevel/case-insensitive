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

final class CIStringCS private (
    override val toString: String,
    val asCanonicalSimpleCaseFoldedString: CanonicalSimpleCaseFoldedString)
    extends Serializable {

  override def equals(that: Any): Boolean =
    that match {
      case that: CIStringCS =>
        asCanonicalSimpleCaseFoldedString == that.asCanonicalSimpleCaseFoldedString
      case _ =>
        false
    }

  override def hashCode(): Int =
    asCanonicalSimpleCaseFoldedString.hashCode
}

object CIStringCS {

  def apply(value: String): CIStringCS =
    new CIStringCS(
      value,
      CanonicalSimpleCaseFoldedString(value)
    )

  val empty: CIStringCS = apply("")

  implicit val hashAndOrderForCIStringCS: Hash[CIStringCS] with Order[CIStringCS] =
    new Hash[CIStringCS] with Order[CIStringCS] {
      override def hash(x: CIStringCS): Int =
        x.hashCode

      override def compare(x: CIStringCS, y: CIStringCS): Int =
        x.asCanonicalSimpleCaseFoldedString.compare(y.asCanonicalSimpleCaseFoldedString)
    }

  implicit val orderingForCIStringCS: Ordering[CIStringCS] =
    hashAndOrderForCIStringCS.toOrdering

  implicit val showForCIStringCS: Show[CIStringCS] =
    Show.fromToString

  implicit val lowerBoundForCIStringCS: LowerBounded[CIStringCS] =
    new LowerBounded[CIStringCS] {
      override val partialOrder: PartialOrder[CIStringCS] =
        hashAndOrderForCIStringCS

      override val minBound: CIStringCS =
        empty
    }

  implicit val monoidForCIStringCS: Monoid[CIStringCS] =
    new Monoid[CIStringCS] {
      override val empty: CIStringCS = CIStringCS.empty

      override def combine(x: CIStringCS, y: CIStringCS): CIStringCS =
        CIStringCS(x.toString + y.toString)

      override def combineAll(xs: IterableOnce[CIStringCS]): CIStringCS = {
        val sb: StringBuilder = new StringBuilder
        xs.iterator.foreach(cfs => sb.append(cfs.toString))
        CIStringCS(sb.toString)
      }
    }
}
