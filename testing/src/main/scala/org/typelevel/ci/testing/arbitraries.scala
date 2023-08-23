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
import org.scalacheck.{Arbitrary, Cogen, Gen}
import org.scalacheck.Arbitrary.arbitrary

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
    val genChar = Gen.oneOf(
      Gen.oneOf(weirdCharFolds),
      Gen.oneOf(weirdStringFolds),
      Gen.oneOf(lowers),
      Gen.oneOf(uppers),
      arbitrary[Char]
    )
    Arbitrary(Gen.listOf(genChar).map(cs => CIString(cs.mkString)))
  }

  implicit val cogenForOrgTypelevelCiCIString: Cogen[CIString] =
    Cogen[String].contramap(ci => new String(ci.toString.toArray.map(_.toLower)))
}
