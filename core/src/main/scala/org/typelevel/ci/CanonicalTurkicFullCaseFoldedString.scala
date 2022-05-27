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
import java.text.Normalizer
import scala.annotation.tailrec

/** As [[CanonicalFullCaseFoldedString]], except it uses the special rules for certain Turkic
  * languages during the case folding step.
  *
  * @see
  *   [[https://www.unicode.org/versions/Unicode14.0.0/ch03.pdf#G34145 Unicode Caseless Matching]]
  */
final case class CanonicalTurkicFullCaseFoldedString private (override val toString: String)
    extends AnyVal

object CanonicalTurkicFullCaseFoldedString {
  def apply(value: String): CanonicalTurkicFullCaseFoldedString =
    new CanonicalTurkicFullCaseFoldedString(
      Normalizer.normalize(
        if (Normalizer.isNormalized(value, Normalizer.Form.NFD)) {
          CaseFolding.turkicFullCaseFoldString(value)
        } else {
          CaseFolding.turkicFullCaseFoldString(Normalizer.normalize(value, Normalizer.Form.NFD))
        },
        Normalizer.Form.NFD
      )
    )

  val empty: CanonicalTurkicFullCaseFoldedString =
    apply("")

  implicit val hashAndOrderForCanonicalTurkicFullCaseFoldedString
      : Hash[CanonicalTurkicFullCaseFoldedString] with Order[CanonicalTurkicFullCaseFoldedString] =
    new Hash[CanonicalTurkicFullCaseFoldedString] with Order[CanonicalTurkicFullCaseFoldedString] {
      override def hash(x: CanonicalTurkicFullCaseFoldedString): Int =
        x.hashCode

      override def compare(
          x: CanonicalTurkicFullCaseFoldedString,
          y: CanonicalTurkicFullCaseFoldedString): Int =
        x.toString.compare(y.toString)
    }

  implicit val orderingForCanonicalTurkicFullCaseFoldedString
      : Ordering[CanonicalTurkicFullCaseFoldedString] =
    hashAndOrderForCanonicalTurkicFullCaseFoldedString.toOrdering

  implicit val showForCanonicalTurkicFullCaseFoldedString
      : Show[CanonicalTurkicFullCaseFoldedString] =
    Show.fromToString

  implicit val lowerBoundForCanonicalTurkicFullCaseFoldedString
      : LowerBounded[CanonicalTurkicFullCaseFoldedString] =
    new LowerBounded[CanonicalTurkicFullCaseFoldedString] {
      override val partialOrder: PartialOrder[CanonicalTurkicFullCaseFoldedString] =
        hashAndOrderForCanonicalTurkicFullCaseFoldedString

      override val minBound: CanonicalTurkicFullCaseFoldedString =
        empty
    }

  implicit val monoidForCanonicalTurkicFullCaseFoldedString
      : Monoid[CanonicalTurkicFullCaseFoldedString] =
    new Monoid[CanonicalTurkicFullCaseFoldedString] {
      override val empty: CanonicalTurkicFullCaseFoldedString =
        CanonicalTurkicFullCaseFoldedString.empty

      override def combine(
          x: CanonicalTurkicFullCaseFoldedString,
          y: CanonicalTurkicFullCaseFoldedString): CanonicalTurkicFullCaseFoldedString =
        CanonicalTurkicFullCaseFoldedString(x.toString + y.toString)

      override def combineAll(xs: IterableOnce[CanonicalTurkicFullCaseFoldedString])
          : CanonicalTurkicFullCaseFoldedString = {
        val sb: StringBuilder = new StringBuilder
        xs.iterator.foreach(cfs => sb.append(cfs.toString))
        CanonicalTurkicFullCaseFoldedString(sb.toString)
      }
    }
}
