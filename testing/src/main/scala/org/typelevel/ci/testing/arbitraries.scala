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
package testing

import java.util.Locale
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Cogen, Gen, Shrink}
import scala.annotation.nowarn
import scala.annotation.tailrec
import scala.collection.immutable.BitSet

object arbitraries {
  implicit val arbitraryForOrgTypelevelCiCIString: Arbitrary[CIString] = {
    val chars = (Character.MIN_VALUE until Character.MAX_VALUE).map(_.toChar)
    // These characters have odd properties when folding
    val weirdCharFolds =
      chars.filter(c => c.toUpper.toLower != c.toLower || c.toLower.toUpper != c.toUpper)
    val weirdStringFolds = chars.filter(c =>
      c.toString.toLowerCase(Locale.ROOT) != c.toLower.toString || c.toString.toUpperCase(
        Locale.ROOT) != c.toUpper.toString)
    // Also focus on characters that are cased at all
    val lowers = chars.filter(_.isLower)
    val uppers = chars.filter(_.isUpper)
    val genChar = Gen.oneOf(weirdCharFolds, weirdStringFolds, lowers, uppers, arbitrary[Char])

    val surrogatePairStrings: Gen[String] =
      // Any Unicode codepoint >= 0x10000 is represented on the JVM by a
      // surrogate pair of two character values.
      Gen.choose(0x10000, 0x10ffff).map(codePoint => new String(Array(codePoint), 0, 1))

    val titleCaseStrings: Gen[String] = {
      @tailrec
      def loop(acc: BitSet, codePoint: Int): BitSet =
        if (codePoint > 0x10ffff) {
          acc
        } else {
          if (Character.isTitleCase(codePoint)) {
            loop(acc + codePoint, codePoint + 1)
          } else {
            loop(acc, codePoint + 1)
          }
        }

      Gen.oneOf(loop(BitSet.empty, 0)).map(codePoint => new String(Array(codePoint), 0, 1))
    }

    Arbitrary(
      Gen.oneOf(
        Gen.listOf(genChar).map(cs => CIString(cs.mkString)),
        arbitrary[String].map(CIString.apply),
        surrogatePairStrings.map(CIString.apply),
        titleCaseStrings.map(CIString.apply)
      )
    )
  }

  implicit val shrinkForCIString: Shrink[CIString] = {
    val stringShrink: Shrink[String] = implicitly[Shrink[String]]
    Shrink(x => stringShrink.shrink(x.toString).map(CIString.apply))
  }

  implicit val cogenForOrgTypelevelCiCIString: Cogen[CIString] =
    Cogen[String].contramap(ci => new String(ci.toString.toArray.map(_.toLower)))
}
