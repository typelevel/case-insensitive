/*
 * Copyright 2020 Typelevel
 *
 * SPDX-License-Identifier: Apache-2.0
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
    val genChar = Gen.oneOf(weirdCharFolds, weirdStringFolds, lowers, uppers, arbitrary[Char])
    Arbitrary(Gen.listOf(genChar).map(cs => CIString(cs.mkString)))
  }

  implicit val cogenForOrgTypelevelCiCIString: Cogen[CIString] =
    Cogen[String].contramap(ci => new String(ci.toString.toArray.map(_.toLower)))
}
