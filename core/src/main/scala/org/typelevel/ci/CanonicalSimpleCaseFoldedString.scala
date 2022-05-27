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
import java.text.Normalizer
import scala.annotation.tailrec

/** A caseless `String`, normalized using Unicode canonical caseless matching and simple case
  * folding. This differs from [[CanonicalFullCaseFoldedString]] in that it uses "simple" case
  * folding.
  *
  * "Simple" case folding does not change the number of code points in the input string, unlike
  * "Full" case folding which can.
  *
  * "Canonical" has a specific meaning in Unicode. From the standard,
  *
  * {{{
  * Canonical equivalence is a fundamental equivalency between characters
  * or sequences of characters which represent the same abstract character,
  * and which when correctly displayed should always have the same visual
  * appearance and behavior.
  * }}}
  *
  * The definition of canonical caseless equivalence in Unicode is,
  *
  * {{{
  * D145 A string X is a canonical caseless match for a string Y if and only if:
  *      NFD(toCasefold(NFD(X))) = NFD(toCasefold(NFD(Y )))
  * }}}
  *
  * Where "NFD" is the function which performs "Canonical Decomposition" and "toCasefold" is one, of
  * several, case folding operations. This type uses simple case folding, without the special rules
  * for some Turkic languages.
  *
  * Thus, the `String` in this type is the result of applying `NFD(toCasefold(NFD(X)))` to the input
  * `String`, `X`.
  *
  * @see
  *   [[https://www.unicode.org/versions/Unicode14.0.0/ch03.pdf#G34145 Unicode Caseless Matching]]
  * @see
  *   [[https://www.unicode.org/reports/tr15/#Canon_Compat_Equivalence Canonical Equivalence]]
  * @see
  *   [[https://www.unicode.org/reports/tr15/#Norm_Forms Unicode Normal Forms]]
  */
final case class CanonicalSimpleCaseFoldedString private (override val toString: String)
    extends AnyVal

object CanonicalSimpleCaseFoldedString {

  def apply(value: String): CanonicalSimpleCaseFoldedString =
    new CanonicalSimpleCaseFoldedString(
      Normalizer.normalize(
        if (Normalizer.isNormalized(value, Normalizer.Form.NFD)) {
          CaseFolding.simpleCaseFoldString(value)
        } else {
          CaseFolding.simpleCaseFoldString(Normalizer.normalize(value, Normalizer.Form.NFD))
        },
        Normalizer.Form.NFD
      )
    )

  val empty: CanonicalSimpleCaseFoldedString =
    apply("")

  implicit val hashAndOrderForCanonicalSimpleCaseFoldedString
      : Hash[CanonicalSimpleCaseFoldedString] with Order[CanonicalSimpleCaseFoldedString] =
    new Hash[CanonicalSimpleCaseFoldedString] with Order[CanonicalSimpleCaseFoldedString] {
      override def hash(x: CanonicalSimpleCaseFoldedString): Int =
        x.hashCode

      override def compare(
          x: CanonicalSimpleCaseFoldedString,
          y: CanonicalSimpleCaseFoldedString): Int =
        x.toString.compare(y.toString)
    }

  implicit val orderingForCanonicalSimpleCaseFoldedString
      : Ordering[CanonicalSimpleCaseFoldedString] =
    hashAndOrderForCanonicalSimpleCaseFoldedString.toOrdering

  implicit val showForCanonicalSimpleCaseFoldedString: Show[CanonicalSimpleCaseFoldedString] =
    Show.fromToString

  implicit val lowerBoundForCanonicalSimpleCaseFoldedString
      : LowerBounded[CanonicalSimpleCaseFoldedString] =
    new LowerBounded[CanonicalSimpleCaseFoldedString] {
      override val partialOrder: PartialOrder[CanonicalSimpleCaseFoldedString] =
        hashAndOrderForCanonicalSimpleCaseFoldedString

      override val minBound: CanonicalSimpleCaseFoldedString =
        empty
    }

  implicit val monoidForCanonicalSimpleCaseFoldedString: Monoid[CanonicalSimpleCaseFoldedString] =
    new Monoid[CanonicalSimpleCaseFoldedString] {
      override val empty: CanonicalSimpleCaseFoldedString = CanonicalSimpleCaseFoldedString.empty

      override def combine(
          x: CanonicalSimpleCaseFoldedString,
          y: CanonicalSimpleCaseFoldedString): CanonicalSimpleCaseFoldedString =
        CanonicalSimpleCaseFoldedString(x.toString + y.toString)

      override def combineAll(
          xs: IterableOnce[CanonicalSimpleCaseFoldedString]): CanonicalSimpleCaseFoldedString = {
        val sb: StringBuilder = new StringBuilder
        xs.iterator.foreach(cfs => sb.append(cfs.toString))
        CanonicalSimpleCaseFoldedString(sb.toString)
      }
    }
}
