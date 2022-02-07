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

/** A caseless `String`, normalized using Unicode canonical caseless matching
  * and full case folding. According to the Unicode standard this is the "most
  * correct" method of caseless matching. If you are looking for a
  * caseless/case insensitive `String` and have no other requirements, you
  * should either use this or [[CIString]]. The difference between the two is
  * that [[CIString]] keeps a reference to the original input `String` (before
  * normalization and case folding), and this type does not. If you don't need
  * the original input `String` value, just a caseless version of it, this
  * type will be more efficient in terms of computation and memory for
  * ''most'' applications.
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
  * Where "NFD" is the function which performs "Canonical Decomposition" and
  * "toCasefold" is one, of several, case folding operations. This type uses
  * full case folding, without the special rules for some Turkic languages.
  *
  * Thus, the `String` in this type is the result of applying
  * `NFD(toCasefold(NFD(X)))` to the input `String`, `X`.
  *
  * @see
  *   [[https://www.unicode.org/versions/Unicode14.0.0/ch03.pdf#G34145 Unicode Caseless Matching]]
  * @see [[https://www.unicode.org/reports/tr15/#Canon_Compat_Equivalence Canonical Equivalence]]
  * @see [[https://www.unicode.org/reports/tr15/#Norm_Forms Unicode Normal Forms]]
  */
final case class CanonicalFullCaseFoldedString private (override val toString: String)
    extends AnyVal

object CanonicalFullCaseFoldedString {
  def apply(value: String): CanonicalFullCaseFoldedString =
    new CanonicalFullCaseFoldedString(
      Normalizer.normalize(
        // Note, the first application of NFD prescribed by the Unicode
        // standard is to handle some very rare edge cases which can change
        // the result even after applying the second, outer, NFD application
        // after case folding. These edge cases are so rare that the standard
        // recommends checking to see if the given input string is in normal
        // form first, as that will likely be cheaper than normalizing.
        //
        // However, we always have to normalize _after_ case folding.
        if (Normalizer.isNormalized(value, Normalizer.Form.NFD)) {
          CaseFolding.fullCaseFoldString(value)
        } else {
          CaseFolding.fullCaseFoldString(Normalizer.normalize(value, Normalizer.Form.NFD))
        },
        Normalizer.Form.NFD
      )
    )

  val empty: CanonicalFullCaseFoldedString =
    apply("")

  implicit val hashAndOrderForCanonicalFullCaseFoldedString
      : Hash[CanonicalFullCaseFoldedString] with Order[CanonicalFullCaseFoldedString] =
    new Hash[CanonicalFullCaseFoldedString] with Order[CanonicalFullCaseFoldedString] {
      override def hash(x: CanonicalFullCaseFoldedString): Int =
        x.hashCode

      override def compare(
          x: CanonicalFullCaseFoldedString,
          y: CanonicalFullCaseFoldedString): Int =
        x.toString.compare(y.toString)
    }

  implicit val orderingForCanonicalFullCaseFoldedString: Ordering[CanonicalFullCaseFoldedString] =
    hashAndOrderForCanonicalFullCaseFoldedString.toOrdering

  implicit val showForCanonicalFullCaseFoldedString: Show[CanonicalFullCaseFoldedString] =
    Show.fromToString

  implicit val lowerBoundForCanonicalFullCaseFoldedString
      : LowerBounded[CanonicalFullCaseFoldedString] =
    new LowerBounded[CanonicalFullCaseFoldedString] {
      override val partialOrder: PartialOrder[CanonicalFullCaseFoldedString] =
        hashAndOrderForCanonicalFullCaseFoldedString

      override val minBound: CanonicalFullCaseFoldedString =
        empty
    }

  implicit val monoidForCanonicalFullCaseFoldedString: Monoid[CanonicalFullCaseFoldedString] =
    new Monoid[CanonicalFullCaseFoldedString] {
      override val empty: CanonicalFullCaseFoldedString = CanonicalFullCaseFoldedString.empty

      override def combine(
          x: CanonicalFullCaseFoldedString,
          y: CanonicalFullCaseFoldedString): CanonicalFullCaseFoldedString =
        CanonicalFullCaseFoldedString(x.toString + y.toString)

      override def combineAll(
          xs: IterableOnce[CanonicalFullCaseFoldedString]): CanonicalFullCaseFoldedString = {
        val sb: StringBuilder = new StringBuilder
        xs.iterator.foreach(cfs => sb.append(cfs.toString))
        CanonicalFullCaseFoldedString(sb.toString)
      }
    }
}
