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

import scala.annotation.tailrec

/** These are lookup tables for case folding. There are several different case folding algorithms
  * which can be employed with different trade offs.
  *
  * The definition of case folding from the Unicode specification,
  *
  * {{{
  * Case folding is related to case conversion. However, the main purpose
  * of case folding is to contribute to caseless matching of strings, whereas
  * the main purpose of case conversion is to put strings into a particular
  * cased form.
  * }}}
  *
  * A case folded string is ''not'' a caseless string. The result of case folding a string does not
  * in and of itself give a string which is ready to be compared for a caseless match. There are
  * several types of caseless matching and for many of them one more additional transformations are
  * required.
  *
  * @note
  *   Some case folding, in particular full case folding, can yield more codePoints than the
  *   original value. That is, it can ''increase'' the size of `String` values once folded.
  *
  * @see
  *   [[https://www.unicode.org/versions/Unicode14.0.0/ch05.pdf#G21790 Caseless Matching]]
  * @see
  *   [[https://www.unicode.org/Public/UCD/latest/ucd/CaseFolding.txt Unicode Case Folding Tables]]
  */
private[ci] object CaseFolding {

  // Note to library maintainers: These functions are intentionally written
  // with int based case matching so that they will compile to a fast
  // lookupswitch. Please keep this in mind when making changes.
  //
  // From `javap -v CaseFolding\$.class` on Scala 2.13.
  //
  // {{{
  // 3: lookupswitch  { // 1530
  //              65: 12252
  //              66: 12263
  // }}}

  /** Perform "full" case folding as defined in the Unicode Case folding tables.
    *
    * Full case folded strings can cause the number of code points in the string to change.
    *
    * @see
    *   [[https://www.unicode.org/Public/UCD/latest/ucd/CaseFolding.txt Unicode Case Folding Tables]]
    */
  def fullCaseFoldString(value: String): String = {
    val builder: java.lang.StringBuilder = new java.lang.StringBuilder(value.length * 3)

    @tailrec
    def loop(index: Int): String =
      if (index >= value.length) {
        builder.toString
      } else {
        val codePoint: Int = value.codePointAt(index)
        fullCaseFoldedCodePoints(codePoint).foreach(c => builder.appendCodePoint(c))
        val inc: Int = if (codePoint >= 0x10000) 2 else 1
        loop(index + inc)
      }

    loop(0)
  }

  /** Perform "full" case folding as defined in the Unicode Case folding tables, using the special
    * rules for Turkic languages.
    *
    * Full case folded strings can cause the number of code points in the string to change.
    *
    * @see
    *   [[https://www.unicode.org/Public/UCD/latest/ucd/CaseFolding.txt Unicode Case Folding Tables]]
    */
  def turkicFullCaseFoldString(value: String): String = {
    val builder: java.lang.StringBuilder = new java.lang.StringBuilder(value.length * 3)

    @tailrec
    def loop(index: Int): String =
      if (index >= value.length) {
        builder.toString
      } else {
        val codePoint: Int = value.codePointAt(index)
        turkicFullCaseFoldedCodePoints(codePoint).foreach(c => builder.appendCodePoint(c))
        val inc: Int = if (codePoint >= 0x10000) 2 else 1
        loop(index + inc)
      }

    loop(0)
  }

  /** Perform "simple" case folding as defined in the Unicode Case folding tables.
    *
    * Simple case folded strings will have the same number of code points after folding.
    *
    * @note
    *   Use of simple case folding is formally less correct than full case folding. It is intended
    *   only for circumstances where it a fixed size of the string is required, e.g. you are working
    *   on a fixed size buffer. If that restriction does not apply full case folding shold be
    *   preferred.
    *
    * @see
    *   [[https://www.unicode.org/Public/UCD/latest/ucd/CaseFolding.txt Unicode Case Folding Tables]]
    */
  def simpleCaseFoldString(value: String): String = {
    val builder: java.lang.StringBuilder = new java.lang.StringBuilder(value.length * 3)

    @tailrec
    def loop(index: Int): String =
      if (index >= value.length) {
        builder.toString
      } else {
        val codePoint: Int = value.codePointAt(index)
        builder.appendCodePoint(simpleCaseFoldedCodePoints(codePoint))
        val inc: Int = if (codePoint >= 0x10000) 2 else 1
        loop(index + inc)
      }

    loop(0)
  }

  /** Perform "simple" case folding as defined in the Unicode Case folding tables, using the special
    * rules for Turkic languages.
    *
    * Simple case folded strings will have the same number of code points after folding.
    *
    * @note
    *   Use of simple case folding is formally less correct than full case folding. It is intended
    *   only for circumstances where it a fixed size of the string is required, e.g. you are working
    *   on a fixed size buffer. If that restriction does not apply full case folding shold be
    *   preferred.
    *
    * @see
    *   [[https://www.unicode.org/Public/UCD/latest/ucd/CaseFolding.txt Unicode Case Folding Tables]]
    */
  def turkicSimpleCaseFoldString(value: String): String = {
    val builder: java.lang.StringBuilder = new java.lang.StringBuilder(value.length * 3)

    @tailrec
    def loop(index: Int): String =
      if (index >= value.length) {
        builder.toString
      } else {
        val codePoint: Int = value.codePointAt(index)
        builder.appendCodePoint(turkicSimpleCaseFoldedCodePoints(codePoint))
        val inc: Int = if (codePoint >= 0x10000) 2 else 1
        loop(index + inc)
      }

    loop(0)
  }

  /** This function transforms a Unicode codePoint into it's full case folded variant, with the rule
    * changes which are applicable to ''some'' Turkic languages.
    *
    * For other languages these rules should not be applied.
    */
  def turkicFullCaseFoldedCodePoints(codePoint: Int): Array[Int] =
    codePoint match {
      case 0x0049 => Array(0x0131) // LATIN CAPITAL LETTER I
      case 0x0130 => Array(0x0069) // LATIN CAPITAL LETTER I WITH DOT ABOVE
      case _ =>
        fullCaseFoldedCodePoints(codePoint)
    }

  /** This function transforms a Unicode codePoint into it's simple case folded variant, with the
    * rule changes which are applicable to ''some'' Turkic languages.
    *
    * For other languages these rules should not be applied.
    */
  def turkicSimpleCaseFoldedCodePoints(codePoint: Int): Int =
    codePoint match {
      case 0x0049 => 0x0131 // LATIN CAPITAL LETTER I
      case 0x0130 => 0x0069 // LATIN CAPITAL LETTER I WITH DOT ABOVE
      case _ =>
        simpleCaseFoldedCodePoints(codePoint)
    }

  /** This function transforms a Unicode codePoint into it's full case folded variant using the
    * default rules.
    *
    * It is equivalent to the "C + F" rules from `CaseFolding.txt`.
    */
  def fullCaseFoldedCodePoints(codePoint: Int): Array[Int] =
    codePoint match {
      case 0x00df => Array(0x0073, 0x0073) // LATIN SMALL LETTER SHARP S
      case 0x0130 => Array(0x0069, 0x0307) // LATIN CAPITAL LETTER I WITH DOT ABOVE
      case 0x0149 => Array(0x02bc, 0x006e) // LATIN SMALL LETTER N PRECEDED BY APOSTROPHE
      case 0x01f0 => Array(0x006a, 0x030c) // LATIN SMALL LETTER J WITH CARON
      case 0x0390 =>
        Array(0x03b9, 0x0308, 0x0301) // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND TONOS
      case 0x03b0 =>
        Array(0x03c5, 0x0308, 0x0301) // GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND TONOS
      case 0x0587 => Array(0x0565, 0x0582) // ARMENIAN SMALL LIGATURE ECH YIWN
      case 0x1e96 => Array(0x0068, 0x0331) // LATIN SMALL LETTER H WITH LINE BELOW
      case 0x1e97 => Array(0x0074, 0x0308) // LATIN SMALL LETTER T WITH DIAERESIS
      case 0x1e98 => Array(0x0077, 0x030a) // LATIN SMALL LETTER W WITH RING ABOVE
      case 0x1e99 => Array(0x0079, 0x030a) // LATIN SMALL LETTER Y WITH RING ABOVE
      case 0x1e9a => Array(0x0061, 0x02be) // LATIN SMALL LETTER A WITH RIGHT HALF RING
      case 0x1e9e => Array(0x0073, 0x0073) // LATIN CAPITAL LETTER SHARP S
      case 0x1f50 => Array(0x03c5, 0x0313) // GREEK SMALL LETTER UPSILON WITH PSILI
      case 0x1f52 =>
        Array(0x03c5, 0x0313, 0x0300) // GREEK SMALL LETTER UPSILON WITH PSILI AND VARIA
      case 0x1f54 => Array(0x03c5, 0x0313, 0x0301) // GREEK SMALL LETTER UPSILON WITH PSILI AND OXIA
      case 0x1f56 =>
        Array(0x03c5, 0x0313, 0x0342) // GREEK SMALL LETTER UPSILON WITH PSILI AND PERISPOMENI
      case 0x1f80 => Array(0x1f00, 0x03b9) // GREEK SMALL LETTER ALPHA WITH PSILI AND YPOGEGRAMMENI
      case 0x1f81 => Array(0x1f01, 0x03b9) // GREEK SMALL LETTER ALPHA WITH DASIA AND YPOGEGRAMMENI
      case 0x1f82 =>
        Array(0x1f02, 0x03b9) // GREEK SMALL LETTER ALPHA WITH PSILI AND VARIA AND YPOGEGRAMMENI
      case 0x1f83 =>
        Array(0x1f03, 0x03b9) // GREEK SMALL LETTER ALPHA WITH DASIA AND VARIA AND YPOGEGRAMMENI
      case 0x1f84 =>
        Array(0x1f04, 0x03b9) // GREEK SMALL LETTER ALPHA WITH PSILI AND OXIA AND YPOGEGRAMMENI
      case 0x1f85 =>
        Array(0x1f05, 0x03b9) // GREEK SMALL LETTER ALPHA WITH DASIA AND OXIA AND YPOGEGRAMMENI
      case 0x1f86 =>
        Array(
          0x1f06,
          0x03b9
        ) // GREEK SMALL LETTER ALPHA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
      case 0x1f87 =>
        Array(
          0x1f07,
          0x03b9
        ) // GREEK SMALL LETTER ALPHA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
      case 0x1f88 =>
        Array(0x1f00, 0x03b9) // GREEK CAPITAL LETTER ALPHA WITH PSILI AND PROSGEGRAMMENI
      case 0x1f89 =>
        Array(0x1f01, 0x03b9) // GREEK CAPITAL LETTER ALPHA WITH DASIA AND PROSGEGRAMMENI
      case 0x1f8a =>
        Array(0x1f02, 0x03b9) // GREEK CAPITAL LETTER ALPHA WITH PSILI AND VARIA AND PROSGEGRAMMENI
      case 0x1f8b =>
        Array(0x1f03, 0x03b9) // GREEK CAPITAL LETTER ALPHA WITH DASIA AND VARIA AND PROSGEGRAMMENI
      case 0x1f8c =>
        Array(0x1f04, 0x03b9) // GREEK CAPITAL LETTER ALPHA WITH PSILI AND OXIA AND PROSGEGRAMMENI
      case 0x1f8d =>
        Array(0x1f05, 0x03b9) // GREEK CAPITAL LETTER ALPHA WITH DASIA AND OXIA AND PROSGEGRAMMENI
      case 0x1f8e =>
        Array(
          0x1f06,
          0x03b9
        ) // GREEK CAPITAL LETTER ALPHA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
      case 0x1f8f =>
        Array(
          0x1f07,
          0x03b9
        ) // GREEK CAPITAL LETTER ALPHA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
      case 0x1f90 => Array(0x1f20, 0x03b9) // GREEK SMALL LETTER ETA WITH PSILI AND YPOGEGRAMMENI
      case 0x1f91 => Array(0x1f21, 0x03b9) // GREEK SMALL LETTER ETA WITH DASIA AND YPOGEGRAMMENI
      case 0x1f92 =>
        Array(0x1f22, 0x03b9) // GREEK SMALL LETTER ETA WITH PSILI AND VARIA AND YPOGEGRAMMENI
      case 0x1f93 =>
        Array(0x1f23, 0x03b9) // GREEK SMALL LETTER ETA WITH DASIA AND VARIA AND YPOGEGRAMMENI
      case 0x1f94 =>
        Array(0x1f24, 0x03b9) // GREEK SMALL LETTER ETA WITH PSILI AND OXIA AND YPOGEGRAMMENI
      case 0x1f95 =>
        Array(0x1f25, 0x03b9) // GREEK SMALL LETTER ETA WITH DASIA AND OXIA AND YPOGEGRAMMENI
      case 0x1f96 =>
        Array(0x1f26, 0x03b9) // GREEK SMALL LETTER ETA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
      case 0x1f97 =>
        Array(0x1f27, 0x03b9) // GREEK SMALL LETTER ETA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
      case 0x1f98 => Array(0x1f20, 0x03b9) // GREEK CAPITAL LETTER ETA WITH PSILI AND PROSGEGRAMMENI
      case 0x1f99 => Array(0x1f21, 0x03b9) // GREEK CAPITAL LETTER ETA WITH DASIA AND PROSGEGRAMMENI
      case 0x1f9a =>
        Array(0x1f22, 0x03b9) // GREEK CAPITAL LETTER ETA WITH PSILI AND VARIA AND PROSGEGRAMMENI
      case 0x1f9b =>
        Array(0x1f23, 0x03b9) // GREEK CAPITAL LETTER ETA WITH DASIA AND VARIA AND PROSGEGRAMMENI
      case 0x1f9c =>
        Array(0x1f24, 0x03b9) // GREEK CAPITAL LETTER ETA WITH PSILI AND OXIA AND PROSGEGRAMMENI
      case 0x1f9d =>
        Array(0x1f25, 0x03b9) // GREEK CAPITAL LETTER ETA WITH DASIA AND OXIA AND PROSGEGRAMMENI
      case 0x1f9e =>
        Array(
          0x1f26,
          0x03b9
        ) // GREEK CAPITAL LETTER ETA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
      case 0x1f9f =>
        Array(
          0x1f27,
          0x03b9
        ) // GREEK CAPITAL LETTER ETA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
      case 0x1fa0 => Array(0x1f60, 0x03b9) // GREEK SMALL LETTER OMEGA WITH PSILI AND YPOGEGRAMMENI
      case 0x1fa1 => Array(0x1f61, 0x03b9) // GREEK SMALL LETTER OMEGA WITH DASIA AND YPOGEGRAMMENI
      case 0x1fa2 =>
        Array(0x1f62, 0x03b9) // GREEK SMALL LETTER OMEGA WITH PSILI AND VARIA AND YPOGEGRAMMENI
      case 0x1fa3 =>
        Array(0x1f63, 0x03b9) // GREEK SMALL LETTER OMEGA WITH DASIA AND VARIA AND YPOGEGRAMMENI
      case 0x1fa4 =>
        Array(0x1f64, 0x03b9) // GREEK SMALL LETTER OMEGA WITH PSILI AND OXIA AND YPOGEGRAMMENI
      case 0x1fa5 =>
        Array(0x1f65, 0x03b9) // GREEK SMALL LETTER OMEGA WITH DASIA AND OXIA AND YPOGEGRAMMENI
      case 0x1fa6 =>
        Array(
          0x1f66,
          0x03b9
        ) // GREEK SMALL LETTER OMEGA WITH PSILI AND PERISPOMENI AND YPOGEGRAMMENI
      case 0x1fa7 =>
        Array(
          0x1f67,
          0x03b9
        ) // GREEK SMALL LETTER OMEGA WITH DASIA AND PERISPOMENI AND YPOGEGRAMMENI
      case 0x1fa8 =>
        Array(0x1f60, 0x03b9) // GREEK CAPITAL LETTER OMEGA WITH PSILI AND PROSGEGRAMMENI
      case 0x1fa9 =>
        Array(0x1f61, 0x03b9) // GREEK CAPITAL LETTER OMEGA WITH DASIA AND PROSGEGRAMMENI
      case 0x1faa =>
        Array(0x1f62, 0x03b9) // GREEK CAPITAL LETTER OMEGA WITH PSILI AND VARIA AND PROSGEGRAMMENI
      case 0x1fab =>
        Array(0x1f63, 0x03b9) // GREEK CAPITAL LETTER OMEGA WITH DASIA AND VARIA AND PROSGEGRAMMENI
      case 0x1fac =>
        Array(0x1f64, 0x03b9) // GREEK CAPITAL LETTER OMEGA WITH PSILI AND OXIA AND PROSGEGRAMMENI
      case 0x1fad =>
        Array(0x1f65, 0x03b9) // GREEK CAPITAL LETTER OMEGA WITH DASIA AND OXIA AND PROSGEGRAMMENI
      case 0x1fae =>
        Array(
          0x1f66,
          0x03b9
        ) // GREEK CAPITAL LETTER OMEGA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
      case 0x1faf =>
        Array(
          0x1f67,
          0x03b9
        ) // GREEK CAPITAL LETTER OMEGA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
      case 0x1fb2 => Array(0x1f70, 0x03b9) // GREEK SMALL LETTER ALPHA WITH VARIA AND YPOGEGRAMMENI
      case 0x1fb3 => Array(0x03b1, 0x03b9) // GREEK SMALL LETTER ALPHA WITH YPOGEGRAMMENI
      case 0x1fb4 => Array(0x03ac, 0x03b9) // GREEK SMALL LETTER ALPHA WITH OXIA AND YPOGEGRAMMENI
      case 0x1fb6 => Array(0x03b1, 0x0342) // GREEK SMALL LETTER ALPHA WITH PERISPOMENI
      case 0x1fb7 =>
        Array(0x03b1, 0x0342, 0x03b9) // GREEK SMALL LETTER ALPHA WITH PERISPOMENI AND YPOGEGRAMMENI
      case 0x1fbc => Array(0x03b1, 0x03b9) // GREEK CAPITAL LETTER ALPHA WITH PROSGEGRAMMENI
      case 0x1fc2 => Array(0x1f74, 0x03b9) // GREEK SMALL LETTER ETA WITH VARIA AND YPOGEGRAMMENI
      case 0x1fc3 => Array(0x03b7, 0x03b9) // GREEK SMALL LETTER ETA WITH YPOGEGRAMMENI
      case 0x1fc4 => Array(0x03ae, 0x03b9) // GREEK SMALL LETTER ETA WITH OXIA AND YPOGEGRAMMENI
      case 0x1fc6 => Array(0x03b7, 0x0342) // GREEK SMALL LETTER ETA WITH PERISPOMENI
      case 0x1fc7 =>
        Array(0x03b7, 0x0342, 0x03b9) // GREEK SMALL LETTER ETA WITH PERISPOMENI AND YPOGEGRAMMENI
      case 0x1fcc => Array(0x03b7, 0x03b9) // GREEK CAPITAL LETTER ETA WITH PROSGEGRAMMENI
      case 0x1fd2 =>
        Array(0x03b9, 0x0308, 0x0300) // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND VARIA
      case 0x1fd3 =>
        Array(0x03b9, 0x0308, 0x0301) // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND OXIA
      case 0x1fd6 => Array(0x03b9, 0x0342) // GREEK SMALL LETTER IOTA WITH PERISPOMENI
      case 0x1fd7 =>
        Array(0x03b9, 0x0308, 0x0342) // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND PERISPOMENI
      case 0x1fe2 =>
        Array(0x03c5, 0x0308, 0x0300) // GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND VARIA
      case 0x1fe3 =>
        Array(0x03c5, 0x0308, 0x0301) // GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND OXIA
      case 0x1fe4 => Array(0x03c1, 0x0313) // GREEK SMALL LETTER RHO WITH PSILI
      case 0x1fe6 => Array(0x03c5, 0x0342) // GREEK SMALL LETTER UPSILON WITH PERISPOMENI
      case 0x1fe7 =>
        Array(0x03c5, 0x0308, 0x0342) // GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND PERISPOMENI
      case 0x1ff2 => Array(0x1f7c, 0x03b9) // GREEK SMALL LETTER OMEGA WITH VARIA AND YPOGEGRAMMENI
      case 0x1ff3 => Array(0x03c9, 0x03b9) // GREEK SMALL LETTER OMEGA WITH YPOGEGRAMMENI
      case 0x1ff4 => Array(0x03ce, 0x03b9) // GREEK SMALL LETTER OMEGA WITH OXIA AND YPOGEGRAMMENI
      case 0x1ff6 => Array(0x03c9, 0x0342) // GREEK SMALL LETTER OMEGA WITH PERISPOMENI
      case 0x1ff7 =>
        Array(0x03c9, 0x0342, 0x03b9) // GREEK SMALL LETTER OMEGA WITH PERISPOMENI AND YPOGEGRAMMENI
      case 0x1ffc => Array(0x03c9, 0x03b9) // GREEK CAPITAL LETTER OMEGA WITH PROSGEGRAMMENI
      case 0xfb00 => Array(0x0066, 0x0066) // LATIN SMALL LIGATURE FF
      case 0xfb01 => Array(0x0066, 0x0069) // LATIN SMALL LIGATURE FI
      case 0xfb02 => Array(0x0066, 0x006c) // LATIN SMALL LIGATURE FL
      case 0xfb03 => Array(0x0066, 0x0066, 0x0069) // LATIN SMALL LIGATURE FFI
      case 0xfb04 => Array(0x0066, 0x0066, 0x006c) // LATIN SMALL LIGATURE FFL
      case 0xfb05 => Array(0x0073, 0x0074) // LATIN SMALL LIGATURE LONG S T
      case 0xfb06 => Array(0x0073, 0x0074) // LATIN SMALL LIGATURE ST
      case 0xfb13 => Array(0x0574, 0x0576) // ARMENIAN SMALL LIGATURE MEN NOW
      case 0xfb14 => Array(0x0574, 0x0565) // ARMENIAN SMALL LIGATURE MEN ECH
      case 0xfb15 => Array(0x0574, 0x056b) // ARMENIAN SMALL LIGATURE MEN INI
      case 0xfb16 => Array(0x057e, 0x0576) // ARMENIAN SMALL LIGATURE VEW NOW
      case 0xfb17 => Array(0x0574, 0x056d) // ARMENIAN SMALL LIGATURE MEN XEH
      case _ => // The full rules defer to the common rules
        Array(commonCaseFoldedCodePoints(codePoint))
    }

  /** This function transforms a Unicode codePoint into it's simple case folded variant using the
    * default rules.
    *
    * It is equivalent to the "C + S" rules from `CaseFolding.txt`.
    */
  def simpleCaseFoldedCodePoints(codePoint: Int): Int =
    codePoint match {
      case 0x1e9e => 0x00df // LATIN CAPITAL LETTER SHARP S
      case 0x1f88 => 0x1f80 // GREEK CAPITAL LETTER ALPHA WITH PSILI AND PROSGEGRAMMENI
      case 0x1f89 => 0x1f81 // GREEK CAPITAL LETTER ALPHA WITH DASIA AND PROSGEGRAMMENI
      case 0x1f8a => 0x1f82 // GREEK CAPITAL LETTER ALPHA WITH PSILI AND VARIA AND PROSGEGRAMMENI
      case 0x1f8b => 0x1f83 // GREEK CAPITAL LETTER ALPHA WITH DASIA AND VARIA AND PROSGEGRAMMENI
      case 0x1f8c => 0x1f84 // GREEK CAPITAL LETTER ALPHA WITH PSILI AND OXIA AND PROSGEGRAMMENI
      case 0x1f8d => 0x1f85 // GREEK CAPITAL LETTER ALPHA WITH DASIA AND OXIA AND PROSGEGRAMMENI
      case 0x1f8e =>
        0x1f86 // GREEK CAPITAL LETTER ALPHA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
      case 0x1f8f =>
        0x1f87 // GREEK CAPITAL LETTER ALPHA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
      case 0x1f98 => 0x1f90 // GREEK CAPITAL LETTER ETA WITH PSILI AND PROSGEGRAMMENI
      case 0x1f99 => 0x1f91 // GREEK CAPITAL LETTER ETA WITH DASIA AND PROSGEGRAMMENI
      case 0x1f9a => 0x1f92 // GREEK CAPITAL LETTER ETA WITH PSILI AND VARIA AND PROSGEGRAMMENI
      case 0x1f9b => 0x1f93 // GREEK CAPITAL LETTER ETA WITH DASIA AND VARIA AND PROSGEGRAMMENI
      case 0x1f9c => 0x1f94 // GREEK CAPITAL LETTER ETA WITH PSILI AND OXIA AND PROSGEGRAMMENI
      case 0x1f9d => 0x1f95 // GREEK CAPITAL LETTER ETA WITH DASIA AND OXIA AND PROSGEGRAMMENI
      case 0x1f9e =>
        0x1f96 // GREEK CAPITAL LETTER ETA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
      case 0x1f9f =>
        0x1f97 // GREEK CAPITAL LETTER ETA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
      case 0x1fa8 => 0x1fa0 // GREEK CAPITAL LETTER OMEGA WITH PSILI AND PROSGEGRAMMENI
      case 0x1fa9 => 0x1fa1 // GREEK CAPITAL LETTER OMEGA WITH DASIA AND PROSGEGRAMMENI
      case 0x1faa => 0x1fa2 // GREEK CAPITAL LETTER OMEGA WITH PSILI AND VARIA AND PROSGEGRAMMENI
      case 0x1fab => 0x1fa3 // GREEK CAPITAL LETTER OMEGA WITH DASIA AND VARIA AND PROSGEGRAMMENI
      case 0x1fac => 0x1fa4 // GREEK CAPITAL LETTER OMEGA WITH PSILI AND OXIA AND PROSGEGRAMMENI
      case 0x1fad => 0x1fa5 // GREEK CAPITAL LETTER OMEGA WITH DASIA AND OXIA AND PROSGEGRAMMENI
      case 0x1fae =>
        0x1fa6 // GREEK CAPITAL LETTER OMEGA WITH PSILI AND PERISPOMENI AND PROSGEGRAMMENI
      case 0x1faf =>
        0x1fa7 // GREEK CAPITAL LETTER OMEGA WITH DASIA AND PERISPOMENI AND PROSGEGRAMMENI
      case 0x1fbc => 0x1fb3 // GREEK CAPITAL LETTER ALPHA WITH PROSGEGRAMMENI
      case 0x1fcc => 0x1fc3 // GREEK CAPITAL LETTER ETA WITH PROSGEGRAMMENI
      case 0x1ffc => 0x1ff3 // GREEK CAPITAL LETTER OMEGA WITH PROSGEGRAMMENI
      case _ => commonCaseFoldedCodePoints(codePoint)
    }

  /** This function transforms a Unicode codePoint into it's common case folded form.
    *
    * This lookup can only be validly used in concert with either the simple or full case folding
    * rules (with or without the special cases for some Turkic languages). This is why this function
    * is `private`.
    */
  private def commonCaseFoldedCodePoints(codePoint: Int): Int =
    codePoint match {
      case 0x0041 => 0x0061 // LATIN CAPITAL LETTER A
      case 0x0042 => 0x0062 // LATIN CAPITAL LETTER B
      case 0x0043 => 0x0063 // LATIN CAPITAL LETTER C
      case 0x0044 => 0x0064 // LATIN CAPITAL LETTER D
      case 0x0045 => 0x0065 // LATIN CAPITAL LETTER E
      case 0x0046 => 0x0066 // LATIN CAPITAL LETTER F
      case 0x0047 => 0x0067 // LATIN CAPITAL LETTER G
      case 0x0048 => 0x0068 // LATIN CAPITAL LETTER H
      case 0x0049 => 0x0069 // LATIN CAPITAL LETTER I
      case 0x004a => 0x006a // LATIN CAPITAL LETTER J
      case 0x004b => 0x006b // LATIN CAPITAL LETTER K
      case 0x004c => 0x006c // LATIN CAPITAL LETTER L
      case 0x004d => 0x006d // LATIN CAPITAL LETTER M
      case 0x004e => 0x006e // LATIN CAPITAL LETTER N
      case 0x004f => 0x006f // LATIN CAPITAL LETTER O
      case 0x0050 => 0x0070 // LATIN CAPITAL LETTER P
      case 0x0051 => 0x0071 // LATIN CAPITAL LETTER Q
      case 0x0052 => 0x0072 // LATIN CAPITAL LETTER R
      case 0x0053 => 0x0073 // LATIN CAPITAL LETTER S
      case 0x0054 => 0x0074 // LATIN CAPITAL LETTER T
      case 0x0055 => 0x0075 // LATIN CAPITAL LETTER U
      case 0x0056 => 0x0076 // LATIN CAPITAL LETTER V
      case 0x0057 => 0x0077 // LATIN CAPITAL LETTER W
      case 0x0058 => 0x0078 // LATIN CAPITAL LETTER X
      case 0x0059 => 0x0079 // LATIN CAPITAL LETTER Y
      case 0x005a => 0x007a // LATIN CAPITAL LETTER Z
      case 0x00b5 => 0x03bc // MICRO SIGN
      case 0x00c0 => 0x00e0 // LATIN CAPITAL LETTER A WITH GRAVE
      case 0x00c1 => 0x00e1 // LATIN CAPITAL LETTER A WITH ACUTE
      case 0x00c2 => 0x00e2 // LATIN CAPITAL LETTER A WITH CIRCUMFLEX
      case 0x00c3 => 0x00e3 // LATIN CAPITAL LETTER A WITH TILDE
      case 0x00c4 => 0x00e4 // LATIN CAPITAL LETTER A WITH DIAERESIS
      case 0x00c5 => 0x00e5 // LATIN CAPITAL LETTER A WITH RING ABOVE
      case 0x00c6 => 0x00e6 // LATIN CAPITAL LETTER AE
      case 0x00c7 => 0x00e7 // LATIN CAPITAL LETTER C WITH CEDILLA
      case 0x00c8 => 0x00e8 // LATIN CAPITAL LETTER E WITH GRAVE
      case 0x00c9 => 0x00e9 // LATIN CAPITAL LETTER E WITH ACUTE
      case 0x00ca => 0x00ea // LATIN CAPITAL LETTER E WITH CIRCUMFLEX
      case 0x00cb => 0x00eb // LATIN CAPITAL LETTER E WITH DIAERESIS
      case 0x00cc => 0x00ec // LATIN CAPITAL LETTER I WITH GRAVE
      case 0x00cd => 0x00ed // LATIN CAPITAL LETTER I WITH ACUTE
      case 0x00ce => 0x00ee // LATIN CAPITAL LETTER I WITH CIRCUMFLEX
      case 0x00cf => 0x00ef // LATIN CAPITAL LETTER I WITH DIAERESIS
      case 0x00d0 => 0x00f0 // LATIN CAPITAL LETTER ETH
      case 0x00d1 => 0x00f1 // LATIN CAPITAL LETTER N WITH TILDE
      case 0x00d2 => 0x00f2 // LATIN CAPITAL LETTER O WITH GRAVE
      case 0x00d3 => 0x00f3 // LATIN CAPITAL LETTER O WITH ACUTE
      case 0x00d4 => 0x00f4 // LATIN CAPITAL LETTER O WITH CIRCUMFLEX
      case 0x00d5 => 0x00f5 // LATIN CAPITAL LETTER O WITH TILDE
      case 0x00d6 => 0x00f6 // LATIN CAPITAL LETTER O WITH DIAERESIS
      case 0x00d8 => 0x00f8 // LATIN CAPITAL LETTER O WITH STROKE
      case 0x00d9 => 0x00f9 // LATIN CAPITAL LETTER U WITH GRAVE
      case 0x00da => 0x00fa // LATIN CAPITAL LETTER U WITH ACUTE
      case 0x00db => 0x00fb // LATIN CAPITAL LETTER U WITH CIRCUMFLEX
      case 0x00dc => 0x00fc // LATIN CAPITAL LETTER U WITH DIAERESIS
      case 0x00dd => 0x00fd // LATIN CAPITAL LETTER Y WITH ACUTE
      case 0x00de => 0x00fe // LATIN CAPITAL LETTER THORN
      case 0x0100 => 0x0101 // LATIN CAPITAL LETTER A WITH MACRON
      case 0x0102 => 0x0103 // LATIN CAPITAL LETTER A WITH BREVE
      case 0x0104 => 0x0105 // LATIN CAPITAL LETTER A WITH OGONEK
      case 0x0106 => 0x0107 // LATIN CAPITAL LETTER C WITH ACUTE
      case 0x0108 => 0x0109 // LATIN CAPITAL LETTER C WITH CIRCUMFLEX
      case 0x010a => 0x010b // LATIN CAPITAL LETTER C WITH DOT ABOVE
      case 0x010c => 0x010d // LATIN CAPITAL LETTER C WITH CARON
      case 0x010e => 0x010f // LATIN CAPITAL LETTER D WITH CARON
      case 0x0110 => 0x0111 // LATIN CAPITAL LETTER D WITH STROKE
      case 0x0112 => 0x0113 // LATIN CAPITAL LETTER E WITH MACRON
      case 0x0114 => 0x0115 // LATIN CAPITAL LETTER E WITH BREVE
      case 0x0116 => 0x0117 // LATIN CAPITAL LETTER E WITH DOT ABOVE
      case 0x0118 => 0x0119 // LATIN CAPITAL LETTER E WITH OGONEK
      case 0x011a => 0x011b // LATIN CAPITAL LETTER E WITH CARON
      case 0x011c => 0x011d // LATIN CAPITAL LETTER G WITH CIRCUMFLEX
      case 0x011e => 0x011f // LATIN CAPITAL LETTER G WITH BREVE
      case 0x0120 => 0x0121 // LATIN CAPITAL LETTER G WITH DOT ABOVE
      case 0x0122 => 0x0123 // LATIN CAPITAL LETTER G WITH CEDILLA
      case 0x0124 => 0x0125 // LATIN CAPITAL LETTER H WITH CIRCUMFLEX
      case 0x0126 => 0x0127 // LATIN CAPITAL LETTER H WITH STROKE
      case 0x0128 => 0x0129 // LATIN CAPITAL LETTER I WITH TILDE
      case 0x012a => 0x012b // LATIN CAPITAL LETTER I WITH MACRON
      case 0x012c => 0x012d // LATIN CAPITAL LETTER I WITH BREVE
      case 0x012e => 0x012f // LATIN CAPITAL LETTER I WITH OGONEK
      case 0x0132 => 0x0133 // LATIN CAPITAL LIGATURE IJ
      case 0x0134 => 0x0135 // LATIN CAPITAL LETTER J WITH CIRCUMFLEX
      case 0x0136 => 0x0137 // LATIN CAPITAL LETTER K WITH CEDILLA
      case 0x0139 => 0x013a // LATIN CAPITAL LETTER L WITH ACUTE
      case 0x013b => 0x013c // LATIN CAPITAL LETTER L WITH CEDILLA
      case 0x013d => 0x013e // LATIN CAPITAL LETTER L WITH CARON
      case 0x013f => 0x0140 // LATIN CAPITAL LETTER L WITH MIDDLE DOT
      case 0x0141 => 0x0142 // LATIN CAPITAL LETTER L WITH STROKE
      case 0x0143 => 0x0144 // LATIN CAPITAL LETTER N WITH ACUTE
      case 0x0145 => 0x0146 // LATIN CAPITAL LETTER N WITH CEDILLA
      case 0x0147 => 0x0148 // LATIN CAPITAL LETTER N WITH CARON
      case 0x014a => 0x014b // LATIN CAPITAL LETTER ENG
      case 0x014c => 0x014d // LATIN CAPITAL LETTER O WITH MACRON
      case 0x014e => 0x014f // LATIN CAPITAL LETTER O WITH BREVE
      case 0x0150 => 0x0151 // LATIN CAPITAL LETTER O WITH DOUBLE ACUTE
      case 0x0152 => 0x0153 // LATIN CAPITAL LIGATURE OE
      case 0x0154 => 0x0155 // LATIN CAPITAL LETTER R WITH ACUTE
      case 0x0156 => 0x0157 // LATIN CAPITAL LETTER R WITH CEDILLA
      case 0x0158 => 0x0159 // LATIN CAPITAL LETTER R WITH CARON
      case 0x015a => 0x015b // LATIN CAPITAL LETTER S WITH ACUTE
      case 0x015c => 0x015d // LATIN CAPITAL LETTER S WITH CIRCUMFLEX
      case 0x015e => 0x015f // LATIN CAPITAL LETTER S WITH CEDILLA
      case 0x0160 => 0x0161 // LATIN CAPITAL LETTER S WITH CARON
      case 0x0162 => 0x0163 // LATIN CAPITAL LETTER T WITH CEDILLA
      case 0x0164 => 0x0165 // LATIN CAPITAL LETTER T WITH CARON
      case 0x0166 => 0x0167 // LATIN CAPITAL LETTER T WITH STROKE
      case 0x0168 => 0x0169 // LATIN CAPITAL LETTER U WITH TILDE
      case 0x016a => 0x016b // LATIN CAPITAL LETTER U WITH MACRON
      case 0x016c => 0x016d // LATIN CAPITAL LETTER U WITH BREVE
      case 0x016e => 0x016f // LATIN CAPITAL LETTER U WITH RING ABOVE
      case 0x0170 => 0x0171 // LATIN CAPITAL LETTER U WITH DOUBLE ACUTE
      case 0x0172 => 0x0173 // LATIN CAPITAL LETTER U WITH OGONEK
      case 0x0174 => 0x0175 // LATIN CAPITAL LETTER W WITH CIRCUMFLEX
      case 0x0176 => 0x0177 // LATIN CAPITAL LETTER Y WITH CIRCUMFLEX
      case 0x0178 => 0x00ff // LATIN CAPITAL LETTER Y WITH DIAERESIS
      case 0x0179 => 0x017a // LATIN CAPITAL LETTER Z WITH ACUTE
      case 0x017b => 0x017c // LATIN CAPITAL LETTER Z WITH DOT ABOVE
      case 0x017d => 0x017e // LATIN CAPITAL LETTER Z WITH CARON
      case 0x017f => 0x0073 // LATIN SMALL LETTER LONG S
      case 0x0181 => 0x0253 // LATIN CAPITAL LETTER B WITH HOOK
      case 0x0182 => 0x0183 // LATIN CAPITAL LETTER B WITH TOPBAR
      case 0x0184 => 0x0185 // LATIN CAPITAL LETTER TONE SIX
      case 0x0186 => 0x0254 // LATIN CAPITAL LETTER OPEN O
      case 0x0187 => 0x0188 // LATIN CAPITAL LETTER C WITH HOOK
      case 0x0189 => 0x0256 // LATIN CAPITAL LETTER AFRICAN D
      case 0x018a => 0x0257 // LATIN CAPITAL LETTER D WITH HOOK
      case 0x018b => 0x018c // LATIN CAPITAL LETTER D WITH TOPBAR
      case 0x018e => 0x01dd // LATIN CAPITAL LETTER REVERSED E
      case 0x018f => 0x0259 // LATIN CAPITAL LETTER SCHWA
      case 0x0190 => 0x025b // LATIN CAPITAL LETTER OPEN E
      case 0x0191 => 0x0192 // LATIN CAPITAL LETTER F WITH HOOK
      case 0x0193 => 0x0260 // LATIN CAPITAL LETTER G WITH HOOK
      case 0x0194 => 0x0263 // LATIN CAPITAL LETTER GAMMA
      case 0x0196 => 0x0269 // LATIN CAPITAL LETTER IOTA
      case 0x0197 => 0x0268 // LATIN CAPITAL LETTER I WITH STROKE
      case 0x0198 => 0x0199 // LATIN CAPITAL LETTER K WITH HOOK
      case 0x019c => 0x026f // LATIN CAPITAL LETTER TURNED M
      case 0x019d => 0x0272 // LATIN CAPITAL LETTER N WITH LEFT HOOK
      case 0x019f => 0x0275 // LATIN CAPITAL LETTER O WITH MIDDLE TILDE
      case 0x01a0 => 0x01a1 // LATIN CAPITAL LETTER O WITH HORN
      case 0x01a2 => 0x01a3 // LATIN CAPITAL LETTER OI
      case 0x01a4 => 0x01a5 // LATIN CAPITAL LETTER P WITH HOOK
      case 0x01a6 => 0x0280 // LATIN LETTER YR
      case 0x01a7 => 0x01a8 // LATIN CAPITAL LETTER TONE TWO
      case 0x01a9 => 0x0283 // LATIN CAPITAL LETTER ESH
      case 0x01ac => 0x01ad // LATIN CAPITAL LETTER T WITH HOOK
      case 0x01ae => 0x0288 // LATIN CAPITAL LETTER T WITH RETROFLEX HOOK
      case 0x01af => 0x01b0 // LATIN CAPITAL LETTER U WITH HORN
      case 0x01b1 => 0x028a // LATIN CAPITAL LETTER UPSILON
      case 0x01b2 => 0x028b // LATIN CAPITAL LETTER V WITH HOOK
      case 0x01b3 => 0x01b4 // LATIN CAPITAL LETTER Y WITH HOOK
      case 0x01b5 => 0x01b6 // LATIN CAPITAL LETTER Z WITH STROKE
      case 0x01b7 => 0x0292 // LATIN CAPITAL LETTER EZH
      case 0x01b8 => 0x01b9 // LATIN CAPITAL LETTER EZH REVERSED
      case 0x01bc => 0x01bd // LATIN CAPITAL LETTER TONE FIVE
      case 0x01c4 => 0x01c6 // LATIN CAPITAL LETTER DZ WITH CARON
      case 0x01c5 => 0x01c6 // LATIN CAPITAL LETTER D WITH SMALL LETTER Z WITH CARON
      case 0x01c7 => 0x01c9 // LATIN CAPITAL LETTER LJ
      case 0x01c8 => 0x01c9 // LATIN CAPITAL LETTER L WITH SMALL LETTER J
      case 0x01ca => 0x01cc // LATIN CAPITAL LETTER NJ
      case 0x01cb => 0x01cc // LATIN CAPITAL LETTER N WITH SMALL LETTER J
      case 0x01cd => 0x01ce // LATIN CAPITAL LETTER A WITH CARON
      case 0x01cf => 0x01d0 // LATIN CAPITAL LETTER I WITH CARON
      case 0x01d1 => 0x01d2 // LATIN CAPITAL LETTER O WITH CARON
      case 0x01d3 => 0x01d4 // LATIN CAPITAL LETTER U WITH CARON
      case 0x01d5 => 0x01d6 // LATIN CAPITAL LETTER U WITH DIAERESIS AND MACRON
      case 0x01d7 => 0x01d8 // LATIN CAPITAL LETTER U WITH DIAERESIS AND ACUTE
      case 0x01d9 => 0x01da // LATIN CAPITAL LETTER U WITH DIAERESIS AND CARON
      case 0x01db => 0x01dc // LATIN CAPITAL LETTER U WITH DIAERESIS AND GRAVE
      case 0x01de => 0x01df // LATIN CAPITAL LETTER A WITH DIAERESIS AND MACRON
      case 0x01e0 => 0x01e1 // LATIN CAPITAL LETTER A WITH DOT ABOVE AND MACRON
      case 0x01e2 => 0x01e3 // LATIN CAPITAL LETTER AE WITH MACRON
      case 0x01e4 => 0x01e5 // LATIN CAPITAL LETTER G WITH STROKE
      case 0x01e6 => 0x01e7 // LATIN CAPITAL LETTER G WITH CARON
      case 0x01e8 => 0x01e9 // LATIN CAPITAL LETTER K WITH CARON
      case 0x01ea => 0x01eb // LATIN CAPITAL LETTER O WITH OGONEK
      case 0x01ec => 0x01ed // LATIN CAPITAL LETTER O WITH OGONEK AND MACRON
      case 0x01ee => 0x01ef // LATIN CAPITAL LETTER EZH WITH CARON
      case 0x01f1 => 0x01f3 // LATIN CAPITAL LETTER DZ
      case 0x01f2 => 0x01f3 // LATIN CAPITAL LETTER D WITH SMALL LETTER Z
      case 0x01f4 => 0x01f5 // LATIN CAPITAL LETTER G WITH ACUTE
      case 0x01f6 => 0x0195 // LATIN CAPITAL LETTER HWAIR
      case 0x01f7 => 0x01bf // LATIN CAPITAL LETTER WYNN
      case 0x01f8 => 0x01f9 // LATIN CAPITAL LETTER N WITH GRAVE
      case 0x01fa => 0x01fb // LATIN CAPITAL LETTER A WITH RING ABOVE AND ACUTE
      case 0x01fc => 0x01fd // LATIN CAPITAL LETTER AE WITH ACUTE
      case 0x01fe => 0x01ff // LATIN CAPITAL LETTER O WITH STROKE AND ACUTE
      case 0x0200 => 0x0201 // LATIN CAPITAL LETTER A WITH DOUBLE GRAVE
      case 0x0202 => 0x0203 // LATIN CAPITAL LETTER A WITH INVERTED BREVE
      case 0x0204 => 0x0205 // LATIN CAPITAL LETTER E WITH DOUBLE GRAVE
      case 0x0206 => 0x0207 // LATIN CAPITAL LETTER E WITH INVERTED BREVE
      case 0x0208 => 0x0209 // LATIN CAPITAL LETTER I WITH DOUBLE GRAVE
      case 0x020a => 0x020b // LATIN CAPITAL LETTER I WITH INVERTED BREVE
      case 0x020c => 0x020d // LATIN CAPITAL LETTER O WITH DOUBLE GRAVE
      case 0x020e => 0x020f // LATIN CAPITAL LETTER O WITH INVERTED BREVE
      case 0x0210 => 0x0211 // LATIN CAPITAL LETTER R WITH DOUBLE GRAVE
      case 0x0212 => 0x0213 // LATIN CAPITAL LETTER R WITH INVERTED BREVE
      case 0x0214 => 0x0215 // LATIN CAPITAL LETTER U WITH DOUBLE GRAVE
      case 0x0216 => 0x0217 // LATIN CAPITAL LETTER U WITH INVERTED BREVE
      case 0x0218 => 0x0219 // LATIN CAPITAL LETTER S WITH COMMA BELOW
      case 0x021a => 0x021b // LATIN CAPITAL LETTER T WITH COMMA BELOW
      case 0x021c => 0x021d // LATIN CAPITAL LETTER YOGH
      case 0x021e => 0x021f // LATIN CAPITAL LETTER H WITH CARON
      case 0x0220 => 0x019e // LATIN CAPITAL LETTER N WITH LONG RIGHT LEG
      case 0x0222 => 0x0223 // LATIN CAPITAL LETTER OU
      case 0x0224 => 0x0225 // LATIN CAPITAL LETTER Z WITH HOOK
      case 0x0226 => 0x0227 // LATIN CAPITAL LETTER A WITH DOT ABOVE
      case 0x0228 => 0x0229 // LATIN CAPITAL LETTER E WITH CEDILLA
      case 0x022a => 0x022b // LATIN CAPITAL LETTER O WITH DIAERESIS AND MACRON
      case 0x022c => 0x022d // LATIN CAPITAL LETTER O WITH TILDE AND MACRON
      case 0x022e => 0x022f // LATIN CAPITAL LETTER O WITH DOT ABOVE
      case 0x0230 => 0x0231 // LATIN CAPITAL LETTER O WITH DOT ABOVE AND MACRON
      case 0x0232 => 0x0233 // LATIN CAPITAL LETTER Y WITH MACRON
      case 0x023a => 0x2c65 // LATIN CAPITAL LETTER A WITH STROKE
      case 0x023b => 0x023c // LATIN CAPITAL LETTER C WITH STROKE
      case 0x023d => 0x019a // LATIN CAPITAL LETTER L WITH BAR
      case 0x023e => 0x2c66 // LATIN CAPITAL LETTER T WITH DIAGONAL STROKE
      case 0x0241 => 0x0242 // LATIN CAPITAL LETTER GLOTTAL STOP
      case 0x0243 => 0x0180 // LATIN CAPITAL LETTER B WITH STROKE
      case 0x0244 => 0x0289 // LATIN CAPITAL LETTER U BAR
      case 0x0245 => 0x028c // LATIN CAPITAL LETTER TURNED V
      case 0x0246 => 0x0247 // LATIN CAPITAL LETTER E WITH STROKE
      case 0x0248 => 0x0249 // LATIN CAPITAL LETTER J WITH STROKE
      case 0x024a => 0x024b // LATIN CAPITAL LETTER SMALL Q WITH HOOK TAIL
      case 0x024c => 0x024d // LATIN CAPITAL LETTER R WITH STROKE
      case 0x024e => 0x024f // LATIN CAPITAL LETTER Y WITH STROKE
      case 0x0345 => 0x03b9 // COMBINING GREEK YPOGEGRAMMENI
      case 0x0370 => 0x0371 // GREEK CAPITAL LETTER HETA
      case 0x0372 => 0x0373 // GREEK CAPITAL LETTER ARCHAIC SAMPI
      case 0x0376 => 0x0377 // GREEK CAPITAL LETTER PAMPHYLIAN DIGAMMA
      case 0x037f => 0x03f3 // GREEK CAPITAL LETTER YOT
      case 0x0386 => 0x03ac // GREEK CAPITAL LETTER ALPHA WITH TONOS
      case 0x0388 => 0x03ad // GREEK CAPITAL LETTER EPSILON WITH TONOS
      case 0x0389 => 0x03ae // GREEK CAPITAL LETTER ETA WITH TONOS
      case 0x038a => 0x03af // GREEK CAPITAL LETTER IOTA WITH TONOS
      case 0x038c => 0x03cc // GREEK CAPITAL LETTER OMICRON WITH TONOS
      case 0x038e => 0x03cd // GREEK CAPITAL LETTER UPSILON WITH TONOS
      case 0x038f => 0x03ce // GREEK CAPITAL LETTER OMEGA WITH TONOS
      case 0x0391 => 0x03b1 // GREEK CAPITAL LETTER ALPHA
      case 0x0392 => 0x03b2 // GREEK CAPITAL LETTER BETA
      case 0x0393 => 0x03b3 // GREEK CAPITAL LETTER GAMMA
      case 0x0394 => 0x03b4 // GREEK CAPITAL LETTER DELTA
      case 0x0395 => 0x03b5 // GREEK CAPITAL LETTER EPSILON
      case 0x0396 => 0x03b6 // GREEK CAPITAL LETTER ZETA
      case 0x0397 => 0x03b7 // GREEK CAPITAL LETTER ETA
      case 0x0398 => 0x03b8 // GREEK CAPITAL LETTER THETA
      case 0x0399 => 0x03b9 // GREEK CAPITAL LETTER IOTA
      case 0x039a => 0x03ba // GREEK CAPITAL LETTER KAPPA
      case 0x039b => 0x03bb // GREEK CAPITAL LETTER LAMDA
      case 0x039c => 0x03bc // GREEK CAPITAL LETTER MU
      case 0x039d => 0x03bd // GREEK CAPITAL LETTER NU
      case 0x039e => 0x03be // GREEK CAPITAL LETTER XI
      case 0x039f => 0x03bf // GREEK CAPITAL LETTER OMICRON
      case 0x03a0 => 0x03c0 // GREEK CAPITAL LETTER PI
      case 0x03a1 => 0x03c1 // GREEK CAPITAL LETTER RHO
      case 0x03a3 => 0x03c3 // GREEK CAPITAL LETTER SIGMA
      case 0x03a4 => 0x03c4 // GREEK CAPITAL LETTER TAU
      case 0x03a5 => 0x03c5 // GREEK CAPITAL LETTER UPSILON
      case 0x03a6 => 0x03c6 // GREEK CAPITAL LETTER PHI
      case 0x03a7 => 0x03c7 // GREEK CAPITAL LETTER CHI
      case 0x03a8 => 0x03c8 // GREEK CAPITAL LETTER PSI
      case 0x03a9 => 0x03c9 // GREEK CAPITAL LETTER OMEGA
      case 0x03aa => 0x03ca // GREEK CAPITAL LETTER IOTA WITH DIALYTIKA
      case 0x03ab => 0x03cb // GREEK CAPITAL LETTER UPSILON WITH DIALYTIKA
      case 0x03c2 => 0x03c3 // GREEK SMALL LETTER FINAL SIGMA
      case 0x03cf => 0x03d7 // GREEK CAPITAL KAI SYMBOL
      case 0x03d0 => 0x03b2 // GREEK BETA SYMBOL
      case 0x03d1 => 0x03b8 // GREEK THETA SYMBOL
      case 0x03d5 => 0x03c6 // GREEK PHI SYMBOL
      case 0x03d6 => 0x03c0 // GREEK PI SYMBOL
      case 0x03d8 => 0x03d9 // GREEK LETTER ARCHAIC KOPPA
      case 0x03da => 0x03db // GREEK LETTER STIGMA
      case 0x03dc => 0x03dd // GREEK LETTER DIGAMMA
      case 0x03de => 0x03df // GREEK LETTER KOPPA
      case 0x03e0 => 0x03e1 // GREEK LETTER SAMPI
      case 0x03e2 => 0x03e3 // COPTIC CAPITAL LETTER SHEI
      case 0x03e4 => 0x03e5 // COPTIC CAPITAL LETTER FEI
      case 0x03e6 => 0x03e7 // COPTIC CAPITAL LETTER KHEI
      case 0x03e8 => 0x03e9 // COPTIC CAPITAL LETTER HORI
      case 0x03ea => 0x03eb // COPTIC CAPITAL LETTER GANGIA
      case 0x03ec => 0x03ed // COPTIC CAPITAL LETTER SHIMA
      case 0x03ee => 0x03ef // COPTIC CAPITAL LETTER DEI
      case 0x03f0 => 0x03ba // GREEK KAPPA SYMBOL
      case 0x03f1 => 0x03c1 // GREEK RHO SYMBOL
      case 0x03f4 => 0x03b8 // GREEK CAPITAL THETA SYMBOL
      case 0x03f5 => 0x03b5 // GREEK LUNATE EPSILON SYMBOL
      case 0x03f7 => 0x03f8 // GREEK CAPITAL LETTER SHO
      case 0x03f9 => 0x03f2 // GREEK CAPITAL LUNATE SIGMA SYMBOL
      case 0x03fa => 0x03fb // GREEK CAPITAL LETTER SAN
      case 0x03fd => 0x037b // GREEK CAPITAL REVERSED LUNATE SIGMA SYMBOL
      case 0x03fe => 0x037c // GREEK CAPITAL DOTTED LUNATE SIGMA SYMBOL
      case 0x03ff => 0x037d // GREEK CAPITAL REVERSED DOTTED LUNATE SIGMA SYMBOL
      case 0x0400 => 0x0450 // CYRILLIC CAPITAL LETTER IE WITH GRAVE
      case 0x0401 => 0x0451 // CYRILLIC CAPITAL LETTER IO
      case 0x0402 => 0x0452 // CYRILLIC CAPITAL LETTER DJE
      case 0x0403 => 0x0453 // CYRILLIC CAPITAL LETTER GJE
      case 0x0404 => 0x0454 // CYRILLIC CAPITAL LETTER UKRAINIAN IE
      case 0x0405 => 0x0455 // CYRILLIC CAPITAL LETTER DZE
      case 0x0406 => 0x0456 // CYRILLIC CAPITAL LETTER BYELORUSSIAN-UKRAINIAN I
      case 0x0407 => 0x0457 // CYRILLIC CAPITAL LETTER YI
      case 0x0408 => 0x0458 // CYRILLIC CAPITAL LETTER JE
      case 0x0409 => 0x0459 // CYRILLIC CAPITAL LETTER LJE
      case 0x040a => 0x045a // CYRILLIC CAPITAL LETTER NJE
      case 0x040b => 0x045b // CYRILLIC CAPITAL LETTER TSHE
      case 0x040c => 0x045c // CYRILLIC CAPITAL LETTER KJE
      case 0x040d => 0x045d // CYRILLIC CAPITAL LETTER I WITH GRAVE
      case 0x040e => 0x045e // CYRILLIC CAPITAL LETTER SHORT U
      case 0x040f => 0x045f // CYRILLIC CAPITAL LETTER DZHE
      case 0x0410 => 0x0430 // CYRILLIC CAPITAL LETTER A
      case 0x0411 => 0x0431 // CYRILLIC CAPITAL LETTER BE
      case 0x0412 => 0x0432 // CYRILLIC CAPITAL LETTER VE
      case 0x0413 => 0x0433 // CYRILLIC CAPITAL LETTER GHE
      case 0x0414 => 0x0434 // CYRILLIC CAPITAL LETTER DE
      case 0x0415 => 0x0435 // CYRILLIC CAPITAL LETTER IE
      case 0x0416 => 0x0436 // CYRILLIC CAPITAL LETTER ZHE
      case 0x0417 => 0x0437 // CYRILLIC CAPITAL LETTER ZE
      case 0x0418 => 0x0438 // CYRILLIC CAPITAL LETTER I
      case 0x0419 => 0x0439 // CYRILLIC CAPITAL LETTER SHORT I
      case 0x041a => 0x043a // CYRILLIC CAPITAL LETTER KA
      case 0x041b => 0x043b // CYRILLIC CAPITAL LETTER EL
      case 0x041c => 0x043c // CYRILLIC CAPITAL LETTER EM
      case 0x041d => 0x043d // CYRILLIC CAPITAL LETTER EN
      case 0x041e => 0x043e // CYRILLIC CAPITAL LETTER O
      case 0x041f => 0x043f // CYRILLIC CAPITAL LETTER PE
      case 0x0420 => 0x0440 // CYRILLIC CAPITAL LETTER ER
      case 0x0421 => 0x0441 // CYRILLIC CAPITAL LETTER ES
      case 0x0422 => 0x0442 // CYRILLIC CAPITAL LETTER TE
      case 0x0423 => 0x0443 // CYRILLIC CAPITAL LETTER U
      case 0x0424 => 0x0444 // CYRILLIC CAPITAL LETTER EF
      case 0x0425 => 0x0445 // CYRILLIC CAPITAL LETTER HA
      case 0x0426 => 0x0446 // CYRILLIC CAPITAL LETTER TSE
      case 0x0427 => 0x0447 // CYRILLIC CAPITAL LETTER CHE
      case 0x0428 => 0x0448 // CYRILLIC CAPITAL LETTER SHA
      case 0x0429 => 0x0449 // CYRILLIC CAPITAL LETTER SHCHA
      case 0x042a => 0x044a // CYRILLIC CAPITAL LETTER HARD SIGN
      case 0x042b => 0x044b // CYRILLIC CAPITAL LETTER YERU
      case 0x042c => 0x044c // CYRILLIC CAPITAL LETTER SOFT SIGN
      case 0x042d => 0x044d // CYRILLIC CAPITAL LETTER E
      case 0x042e => 0x044e // CYRILLIC CAPITAL LETTER YU
      case 0x042f => 0x044f // CYRILLIC CAPITAL LETTER YA
      case 0x0460 => 0x0461 // CYRILLIC CAPITAL LETTER OMEGA
      case 0x0462 => 0x0463 // CYRILLIC CAPITAL LETTER YAT
      case 0x0464 => 0x0465 // CYRILLIC CAPITAL LETTER IOTIFIED E
      case 0x0466 => 0x0467 // CYRILLIC CAPITAL LETTER LITTLE YUS
      case 0x0468 => 0x0469 // CYRILLIC CAPITAL LETTER IOTIFIED LITTLE YUS
      case 0x046a => 0x046b // CYRILLIC CAPITAL LETTER BIG YUS
      case 0x046c => 0x046d // CYRILLIC CAPITAL LETTER IOTIFIED BIG YUS
      case 0x046e => 0x046f // CYRILLIC CAPITAL LETTER KSI
      case 0x0470 => 0x0471 // CYRILLIC CAPITAL LETTER PSI
      case 0x0472 => 0x0473 // CYRILLIC CAPITAL LETTER FITA
      case 0x0474 => 0x0475 // CYRILLIC CAPITAL LETTER IZHITSA
      case 0x0476 => 0x0477 // CYRILLIC CAPITAL LETTER IZHITSA WITH DOUBLE GRAVE ACCENT
      case 0x0478 => 0x0479 // CYRILLIC CAPITAL LETTER UK
      case 0x047a => 0x047b // CYRILLIC CAPITAL LETTER ROUND OMEGA
      case 0x047c => 0x047d // CYRILLIC CAPITAL LETTER OMEGA WITH TITLO
      case 0x047e => 0x047f // CYRILLIC CAPITAL LETTER OT
      case 0x0480 => 0x0481 // CYRILLIC CAPITAL LETTER KOPPA
      case 0x048a => 0x048b // CYRILLIC CAPITAL LETTER SHORT I WITH TAIL
      case 0x048c => 0x048d // CYRILLIC CAPITAL LETTER SEMISOFT SIGN
      case 0x048e => 0x048f // CYRILLIC CAPITAL LETTER ER WITH TICK
      case 0x0490 => 0x0491 // CYRILLIC CAPITAL LETTER GHE WITH UPTURN
      case 0x0492 => 0x0493 // CYRILLIC CAPITAL LETTER GHE WITH STROKE
      case 0x0494 => 0x0495 // CYRILLIC CAPITAL LETTER GHE WITH MIDDLE HOOK
      case 0x0496 => 0x0497 // CYRILLIC CAPITAL LETTER ZHE WITH DESCENDER
      case 0x0498 => 0x0499 // CYRILLIC CAPITAL LETTER ZE WITH DESCENDER
      case 0x049a => 0x049b // CYRILLIC CAPITAL LETTER KA WITH DESCENDER
      case 0x049c => 0x049d // CYRILLIC CAPITAL LETTER KA WITH VERTICAL STROKE
      case 0x049e => 0x049f // CYRILLIC CAPITAL LETTER KA WITH STROKE
      case 0x04a0 => 0x04a1 // CYRILLIC CAPITAL LETTER BASHKIR KA
      case 0x04a2 => 0x04a3 // CYRILLIC CAPITAL LETTER EN WITH DESCENDER
      case 0x04a4 => 0x04a5 // CYRILLIC CAPITAL LIGATURE EN GHE
      case 0x04a6 => 0x04a7 // CYRILLIC CAPITAL LETTER PE WITH MIDDLE HOOK
      case 0x04a8 => 0x04a9 // CYRILLIC CAPITAL LETTER ABKHASIAN HA
      case 0x04aa => 0x04ab // CYRILLIC CAPITAL LETTER ES WITH DESCENDER
      case 0x04ac => 0x04ad // CYRILLIC CAPITAL LETTER TE WITH DESCENDER
      case 0x04ae => 0x04af // CYRILLIC CAPITAL LETTER STRAIGHT U
      case 0x04b0 => 0x04b1 // CYRILLIC CAPITAL LETTER STRAIGHT U WITH STROKE
      case 0x04b2 => 0x04b3 // CYRILLIC CAPITAL LETTER HA WITH DESCENDER
      case 0x04b4 => 0x04b5 // CYRILLIC CAPITAL LIGATURE TE TSE
      case 0x04b6 => 0x04b7 // CYRILLIC CAPITAL LETTER CHE WITH DESCENDER
      case 0x04b8 => 0x04b9 // CYRILLIC CAPITAL LETTER CHE WITH VERTICAL STROKE
      case 0x04ba => 0x04bb // CYRILLIC CAPITAL LETTER SHHA
      case 0x04bc => 0x04bd // CYRILLIC CAPITAL LETTER ABKHASIAN CHE
      case 0x04be => 0x04bf // CYRILLIC CAPITAL LETTER ABKHASIAN CHE WITH DESCENDER
      case 0x04c0 => 0x04cf // CYRILLIC LETTER PALOCHKA
      case 0x04c1 => 0x04c2 // CYRILLIC CAPITAL LETTER ZHE WITH BREVE
      case 0x04c3 => 0x04c4 // CYRILLIC CAPITAL LETTER KA WITH HOOK
      case 0x04c5 => 0x04c6 // CYRILLIC CAPITAL LETTER EL WITH TAIL
      case 0x04c7 => 0x04c8 // CYRILLIC CAPITAL LETTER EN WITH HOOK
      case 0x04c9 => 0x04ca // CYRILLIC CAPITAL LETTER EN WITH TAIL
      case 0x04cb => 0x04cc // CYRILLIC CAPITAL LETTER KHAKASSIAN CHE
      case 0x04cd => 0x04ce // CYRILLIC CAPITAL LETTER EM WITH TAIL
      case 0x04d0 => 0x04d1 // CYRILLIC CAPITAL LETTER A WITH BREVE
      case 0x04d2 => 0x04d3 // CYRILLIC CAPITAL LETTER A WITH DIAERESIS
      case 0x04d4 => 0x04d5 // CYRILLIC CAPITAL LIGATURE A IE
      case 0x04d6 => 0x04d7 // CYRILLIC CAPITAL LETTER IE WITH BREVE
      case 0x04d8 => 0x04d9 // CYRILLIC CAPITAL LETTER SCHWA
      case 0x04da => 0x04db // CYRILLIC CAPITAL LETTER SCHWA WITH DIAERESIS
      case 0x04dc => 0x04dd // CYRILLIC CAPITAL LETTER ZHE WITH DIAERESIS
      case 0x04de => 0x04df // CYRILLIC CAPITAL LETTER ZE WITH DIAERESIS
      case 0x04e0 => 0x04e1 // CYRILLIC CAPITAL LETTER ABKHASIAN DZE
      case 0x04e2 => 0x04e3 // CYRILLIC CAPITAL LETTER I WITH MACRON
      case 0x04e4 => 0x04e5 // CYRILLIC CAPITAL LETTER I WITH DIAERESIS
      case 0x04e6 => 0x04e7 // CYRILLIC CAPITAL LETTER O WITH DIAERESIS
      case 0x04e8 => 0x04e9 // CYRILLIC CAPITAL LETTER BARRED O
      case 0x04ea => 0x04eb // CYRILLIC CAPITAL LETTER BARRED O WITH DIAERESIS
      case 0x04ec => 0x04ed // CYRILLIC CAPITAL LETTER E WITH DIAERESIS
      case 0x04ee => 0x04ef // CYRILLIC CAPITAL LETTER U WITH MACRON
      case 0x04f0 => 0x04f1 // CYRILLIC CAPITAL LETTER U WITH DIAERESIS
      case 0x04f2 => 0x04f3 // CYRILLIC CAPITAL LETTER U WITH DOUBLE ACUTE
      case 0x04f4 => 0x04f5 // CYRILLIC CAPITAL LETTER CHE WITH DIAERESIS
      case 0x04f6 => 0x04f7 // CYRILLIC CAPITAL LETTER GHE WITH DESCENDER
      case 0x04f8 => 0x04f9 // CYRILLIC CAPITAL LETTER YERU WITH DIAERESIS
      case 0x04fa => 0x04fb // CYRILLIC CAPITAL LETTER GHE WITH STROKE AND HOOK
      case 0x04fc => 0x04fd // CYRILLIC CAPITAL LETTER HA WITH HOOK
      case 0x04fe => 0x04ff // CYRILLIC CAPITAL LETTER HA WITH STROKE
      case 0x0500 => 0x0501 // CYRILLIC CAPITAL LETTER KOMI DE
      case 0x0502 => 0x0503 // CYRILLIC CAPITAL LETTER KOMI DJE
      case 0x0504 => 0x0505 // CYRILLIC CAPITAL LETTER KOMI ZJE
      case 0x0506 => 0x0507 // CYRILLIC CAPITAL LETTER KOMI DZJE
      case 0x0508 => 0x0509 // CYRILLIC CAPITAL LETTER KOMI LJE
      case 0x050a => 0x050b // CYRILLIC CAPITAL LETTER KOMI NJE
      case 0x050c => 0x050d // CYRILLIC CAPITAL LETTER KOMI SJE
      case 0x050e => 0x050f // CYRILLIC CAPITAL LETTER KOMI TJE
      case 0x0510 => 0x0511 // CYRILLIC CAPITAL LETTER REVERSED ZE
      case 0x0512 => 0x0513 // CYRILLIC CAPITAL LETTER EL WITH HOOK
      case 0x0514 => 0x0515 // CYRILLIC CAPITAL LETTER LHA
      case 0x0516 => 0x0517 // CYRILLIC CAPITAL LETTER RHA
      case 0x0518 => 0x0519 // CYRILLIC CAPITAL LETTER YAE
      case 0x051a => 0x051b // CYRILLIC CAPITAL LETTER QA
      case 0x051c => 0x051d // CYRILLIC CAPITAL LETTER WE
      case 0x051e => 0x051f // CYRILLIC CAPITAL LETTER ALEUT KA
      case 0x0520 => 0x0521 // CYRILLIC CAPITAL LETTER EL WITH MIDDLE HOOK
      case 0x0522 => 0x0523 // CYRILLIC CAPITAL LETTER EN WITH MIDDLE HOOK
      case 0x0524 => 0x0525 // CYRILLIC CAPITAL LETTER PE WITH DESCENDER
      case 0x0526 => 0x0527 // CYRILLIC CAPITAL LETTER SHHA WITH DESCENDER
      case 0x0528 => 0x0529 // CYRILLIC CAPITAL LETTER EN WITH LEFT HOOK
      case 0x052a => 0x052b // CYRILLIC CAPITAL LETTER DZZHE
      case 0x052c => 0x052d // CYRILLIC CAPITAL LETTER DCHE
      case 0x052e => 0x052f // CYRILLIC CAPITAL LETTER EL WITH DESCENDER
      case 0x0531 => 0x0561 // ARMENIAN CAPITAL LETTER AYB
      case 0x0532 => 0x0562 // ARMENIAN CAPITAL LETTER BEN
      case 0x0533 => 0x0563 // ARMENIAN CAPITAL LETTER GIM
      case 0x0534 => 0x0564 // ARMENIAN CAPITAL LETTER DA
      case 0x0535 => 0x0565 // ARMENIAN CAPITAL LETTER ECH
      case 0x0536 => 0x0566 // ARMENIAN CAPITAL LETTER ZA
      case 0x0537 => 0x0567 // ARMENIAN CAPITAL LETTER EH
      case 0x0538 => 0x0568 // ARMENIAN CAPITAL LETTER ET
      case 0x0539 => 0x0569 // ARMENIAN CAPITAL LETTER TO
      case 0x053a => 0x056a // ARMENIAN CAPITAL LETTER ZHE
      case 0x053b => 0x056b // ARMENIAN CAPITAL LETTER INI
      case 0x053c => 0x056c // ARMENIAN CAPITAL LETTER LIWN
      case 0x053d => 0x056d // ARMENIAN CAPITAL LETTER XEH
      case 0x053e => 0x056e // ARMENIAN CAPITAL LETTER CA
      case 0x053f => 0x056f // ARMENIAN CAPITAL LETTER KEN
      case 0x0540 => 0x0570 // ARMENIAN CAPITAL LETTER HO
      case 0x0541 => 0x0571 // ARMENIAN CAPITAL LETTER JA
      case 0x0542 => 0x0572 // ARMENIAN CAPITAL LETTER GHAD
      case 0x0543 => 0x0573 // ARMENIAN CAPITAL LETTER CHEH
      case 0x0544 => 0x0574 // ARMENIAN CAPITAL LETTER MEN
      case 0x0545 => 0x0575 // ARMENIAN CAPITAL LETTER YI
      case 0x0546 => 0x0576 // ARMENIAN CAPITAL LETTER NOW
      case 0x0547 => 0x0577 // ARMENIAN CAPITAL LETTER SHA
      case 0x0548 => 0x0578 // ARMENIAN CAPITAL LETTER VO
      case 0x0549 => 0x0579 // ARMENIAN CAPITAL LETTER CHA
      case 0x054a => 0x057a // ARMENIAN CAPITAL LETTER PEH
      case 0x054b => 0x057b // ARMENIAN CAPITAL LETTER JHEH
      case 0x054c => 0x057c // ARMENIAN CAPITAL LETTER RA
      case 0x054d => 0x057d // ARMENIAN CAPITAL LETTER SEH
      case 0x054e => 0x057e // ARMENIAN CAPITAL LETTER VEW
      case 0x054f => 0x057f // ARMENIAN CAPITAL LETTER TIWN
      case 0x0550 => 0x0580 // ARMENIAN CAPITAL LETTER REH
      case 0x0551 => 0x0581 // ARMENIAN CAPITAL LETTER CO
      case 0x0552 => 0x0582 // ARMENIAN CAPITAL LETTER YIWN
      case 0x0553 => 0x0583 // ARMENIAN CAPITAL LETTER PIWR
      case 0x0554 => 0x0584 // ARMENIAN CAPITAL LETTER KEH
      case 0x0555 => 0x0585 // ARMENIAN CAPITAL LETTER OH
      case 0x0556 => 0x0586 // ARMENIAN CAPITAL LETTER FEH
      case 0x10a0 => 0x2d00 // GEORGIAN CAPITAL LETTER AN
      case 0x10a1 => 0x2d01 // GEORGIAN CAPITAL LETTER BAN
      case 0x10a2 => 0x2d02 // GEORGIAN CAPITAL LETTER GAN
      case 0x10a3 => 0x2d03 // GEORGIAN CAPITAL LETTER DON
      case 0x10a4 => 0x2d04 // GEORGIAN CAPITAL LETTER EN
      case 0x10a5 => 0x2d05 // GEORGIAN CAPITAL LETTER VIN
      case 0x10a6 => 0x2d06 // GEORGIAN CAPITAL LETTER ZEN
      case 0x10a7 => 0x2d07 // GEORGIAN CAPITAL LETTER TAN
      case 0x10a8 => 0x2d08 // GEORGIAN CAPITAL LETTER IN
      case 0x10a9 => 0x2d09 // GEORGIAN CAPITAL LETTER KAN
      case 0x10aa => 0x2d0a // GEORGIAN CAPITAL LETTER LAS
      case 0x10ab => 0x2d0b // GEORGIAN CAPITAL LETTER MAN
      case 0x10ac => 0x2d0c // GEORGIAN CAPITAL LETTER NAR
      case 0x10ad => 0x2d0d // GEORGIAN CAPITAL LETTER ON
      case 0x10ae => 0x2d0e // GEORGIAN CAPITAL LETTER PAR
      case 0x10af => 0x2d0f // GEORGIAN CAPITAL LETTER ZHAR
      case 0x10b0 => 0x2d10 // GEORGIAN CAPITAL LETTER RAE
      case 0x10b1 => 0x2d11 // GEORGIAN CAPITAL LETTER SAN
      case 0x10b2 => 0x2d12 // GEORGIAN CAPITAL LETTER TAR
      case 0x10b3 => 0x2d13 // GEORGIAN CAPITAL LETTER UN
      case 0x10b4 => 0x2d14 // GEORGIAN CAPITAL LETTER PHAR
      case 0x10b5 => 0x2d15 // GEORGIAN CAPITAL LETTER KHAR
      case 0x10b6 => 0x2d16 // GEORGIAN CAPITAL LETTER GHAN
      case 0x10b7 => 0x2d17 // GEORGIAN CAPITAL LETTER QAR
      case 0x10b8 => 0x2d18 // GEORGIAN CAPITAL LETTER SHIN
      case 0x10b9 => 0x2d19 // GEORGIAN CAPITAL LETTER CHIN
      case 0x10ba => 0x2d1a // GEORGIAN CAPITAL LETTER CAN
      case 0x10bb => 0x2d1b // GEORGIAN CAPITAL LETTER JIL
      case 0x10bc => 0x2d1c // GEORGIAN CAPITAL LETTER CIL
      case 0x10bd => 0x2d1d // GEORGIAN CAPITAL LETTER CHAR
      case 0x10be => 0x2d1e // GEORGIAN CAPITAL LETTER XAN
      case 0x10bf => 0x2d1f // GEORGIAN CAPITAL LETTER JHAN
      case 0x10c0 => 0x2d20 // GEORGIAN CAPITAL LETTER HAE
      case 0x10c1 => 0x2d21 // GEORGIAN CAPITAL LETTER HE
      case 0x10c2 => 0x2d22 // GEORGIAN CAPITAL LETTER HIE
      case 0x10c3 => 0x2d23 // GEORGIAN CAPITAL LETTER WE
      case 0x10c4 => 0x2d24 // GEORGIAN CAPITAL LETTER HAR
      case 0x10c5 => 0x2d25 // GEORGIAN CAPITAL LETTER HOE
      case 0x10c7 => 0x2d27 // GEORGIAN CAPITAL LETTER YN
      case 0x10cd => 0x2d2d // GEORGIAN CAPITAL LETTER AEN
      case 0x13f8 => 0x13f0 // CHEROKEE SMALL LETTER YE
      case 0x13f9 => 0x13f1 // CHEROKEE SMALL LETTER YI
      case 0x13fa => 0x13f2 // CHEROKEE SMALL LETTER YO
      case 0x13fb => 0x13f3 // CHEROKEE SMALL LETTER YU
      case 0x13fc => 0x13f4 // CHEROKEE SMALL LETTER YV
      case 0x13fd => 0x13f5 // CHEROKEE SMALL LETTER MV
      case 0x1c80 => 0x0432 // CYRILLIC SMALL LETTER ROUNDED VE
      case 0x1c81 => 0x0434 // CYRILLIC SMALL LETTER LONG-LEGGED DE
      case 0x1c82 => 0x043e // CYRILLIC SMALL LETTER NARROW O
      case 0x1c83 => 0x0441 // CYRILLIC SMALL LETTER WIDE ES
      case 0x1c84 => 0x0442 // CYRILLIC SMALL LETTER TALL TE
      case 0x1c85 => 0x0442 // CYRILLIC SMALL LETTER THREE-LEGGED TE
      case 0x1c86 => 0x044a // CYRILLIC SMALL LETTER TALL HARD SIGN
      case 0x1c87 => 0x0463 // CYRILLIC SMALL LETTER TALL YAT
      case 0x1c88 => 0xa64b // CYRILLIC SMALL LETTER UNBLENDED UK
      case 0x1c90 => 0x10d0 // GEORGIAN MTAVRULI CAPITAL LETTER AN
      case 0x1c91 => 0x10d1 // GEORGIAN MTAVRULI CAPITAL LETTER BAN
      case 0x1c92 => 0x10d2 // GEORGIAN MTAVRULI CAPITAL LETTER GAN
      case 0x1c93 => 0x10d3 // GEORGIAN MTAVRULI CAPITAL LETTER DON
      case 0x1c94 => 0x10d4 // GEORGIAN MTAVRULI CAPITAL LETTER EN
      case 0x1c95 => 0x10d5 // GEORGIAN MTAVRULI CAPITAL LETTER VIN
      case 0x1c96 => 0x10d6 // GEORGIAN MTAVRULI CAPITAL LETTER ZEN
      case 0x1c97 => 0x10d7 // GEORGIAN MTAVRULI CAPITAL LETTER TAN
      case 0x1c98 => 0x10d8 // GEORGIAN MTAVRULI CAPITAL LETTER IN
      case 0x1c99 => 0x10d9 // GEORGIAN MTAVRULI CAPITAL LETTER KAN
      case 0x1c9a => 0x10da // GEORGIAN MTAVRULI CAPITAL LETTER LAS
      case 0x1c9b => 0x10db // GEORGIAN MTAVRULI CAPITAL LETTER MAN
      case 0x1c9c => 0x10dc // GEORGIAN MTAVRULI CAPITAL LETTER NAR
      case 0x1c9d => 0x10dd // GEORGIAN MTAVRULI CAPITAL LETTER ON
      case 0x1c9e => 0x10de // GEORGIAN MTAVRULI CAPITAL LETTER PAR
      case 0x1c9f => 0x10df // GEORGIAN MTAVRULI CAPITAL LETTER ZHAR
      case 0x1ca0 => 0x10e0 // GEORGIAN MTAVRULI CAPITAL LETTER RAE
      case 0x1ca1 => 0x10e1 // GEORGIAN MTAVRULI CAPITAL LETTER SAN
      case 0x1ca2 => 0x10e2 // GEORGIAN MTAVRULI CAPITAL LETTER TAR
      case 0x1ca3 => 0x10e3 // GEORGIAN MTAVRULI CAPITAL LETTER UN
      case 0x1ca4 => 0x10e4 // GEORGIAN MTAVRULI CAPITAL LETTER PHAR
      case 0x1ca5 => 0x10e5 // GEORGIAN MTAVRULI CAPITAL LETTER KHAR
      case 0x1ca6 => 0x10e6 // GEORGIAN MTAVRULI CAPITAL LETTER GHAN
      case 0x1ca7 => 0x10e7 // GEORGIAN MTAVRULI CAPITAL LETTER QAR
      case 0x1ca8 => 0x10e8 // GEORGIAN MTAVRULI CAPITAL LETTER SHIN
      case 0x1ca9 => 0x10e9 // GEORGIAN MTAVRULI CAPITAL LETTER CHIN
      case 0x1caa => 0x10ea // GEORGIAN MTAVRULI CAPITAL LETTER CAN
      case 0x1cab => 0x10eb // GEORGIAN MTAVRULI CAPITAL LETTER JIL
      case 0x1cac => 0x10ec // GEORGIAN MTAVRULI CAPITAL LETTER CIL
      case 0x1cad => 0x10ed // GEORGIAN MTAVRULI CAPITAL LETTER CHAR
      case 0x1cae => 0x10ee // GEORGIAN MTAVRULI CAPITAL LETTER XAN
      case 0x1caf => 0x10ef // GEORGIAN MTAVRULI CAPITAL LETTER JHAN
      case 0x1cb0 => 0x10f0 // GEORGIAN MTAVRULI CAPITAL LETTER HAE
      case 0x1cb1 => 0x10f1 // GEORGIAN MTAVRULI CAPITAL LETTER HE
      case 0x1cb2 => 0x10f2 // GEORGIAN MTAVRULI CAPITAL LETTER HIE
      case 0x1cb3 => 0x10f3 // GEORGIAN MTAVRULI CAPITAL LETTER WE
      case 0x1cb4 => 0x10f4 // GEORGIAN MTAVRULI CAPITAL LETTER HAR
      case 0x1cb5 => 0x10f5 // GEORGIAN MTAVRULI CAPITAL LETTER HOE
      case 0x1cb6 => 0x10f6 // GEORGIAN MTAVRULI CAPITAL LETTER FI
      case 0x1cb7 => 0x10f7 // GEORGIAN MTAVRULI CAPITAL LETTER YN
      case 0x1cb8 => 0x10f8 // GEORGIAN MTAVRULI CAPITAL LETTER ELIFI
      case 0x1cb9 => 0x10f9 // GEORGIAN MTAVRULI CAPITAL LETTER TURNED GAN
      case 0x1cba => 0x10fa // GEORGIAN MTAVRULI CAPITAL LETTER AIN
      case 0x1cbd => 0x10fd // GEORGIAN MTAVRULI CAPITAL LETTER AEN
      case 0x1cbe => 0x10fe // GEORGIAN MTAVRULI CAPITAL LETTER HARD SIGN
      case 0x1cbf => 0x10ff // GEORGIAN MTAVRULI CAPITAL LETTER LABIAL SIGN
      case 0x1e00 => 0x1e01 // LATIN CAPITAL LETTER A WITH RING BELOW
      case 0x1e02 => 0x1e03 // LATIN CAPITAL LETTER B WITH DOT ABOVE
      case 0x1e04 => 0x1e05 // LATIN CAPITAL LETTER B WITH DOT BELOW
      case 0x1e06 => 0x1e07 // LATIN CAPITAL LETTER B WITH LINE BELOW
      case 0x1e08 => 0x1e09 // LATIN CAPITAL LETTER C WITH CEDILLA AND ACUTE
      case 0x1e0a => 0x1e0b // LATIN CAPITAL LETTER D WITH DOT ABOVE
      case 0x1e0c => 0x1e0d // LATIN CAPITAL LETTER D WITH DOT BELOW
      case 0x1e0e => 0x1e0f // LATIN CAPITAL LETTER D WITH LINE BELOW
      case 0x1e10 => 0x1e11 // LATIN CAPITAL LETTER D WITH CEDILLA
      case 0x1e12 => 0x1e13 // LATIN CAPITAL LETTER D WITH CIRCUMFLEX BELOW
      case 0x1e14 => 0x1e15 // LATIN CAPITAL LETTER E WITH MACRON AND GRAVE
      case 0x1e16 => 0x1e17 // LATIN CAPITAL LETTER E WITH MACRON AND ACUTE
      case 0x1e18 => 0x1e19 // LATIN CAPITAL LETTER E WITH CIRCUMFLEX BELOW
      case 0x1e1a => 0x1e1b // LATIN CAPITAL LETTER E WITH TILDE BELOW
      case 0x1e1c => 0x1e1d // LATIN CAPITAL LETTER E WITH CEDILLA AND BREVE
      case 0x1e1e => 0x1e1f // LATIN CAPITAL LETTER F WITH DOT ABOVE
      case 0x1e20 => 0x1e21 // LATIN CAPITAL LETTER G WITH MACRON
      case 0x1e22 => 0x1e23 // LATIN CAPITAL LETTER H WITH DOT ABOVE
      case 0x1e24 => 0x1e25 // LATIN CAPITAL LETTER H WITH DOT BELOW
      case 0x1e26 => 0x1e27 // LATIN CAPITAL LETTER H WITH DIAERESIS
      case 0x1e28 => 0x1e29 // LATIN CAPITAL LETTER H WITH CEDILLA
      case 0x1e2a => 0x1e2b // LATIN CAPITAL LETTER H WITH BREVE BELOW
      case 0x1e2c => 0x1e2d // LATIN CAPITAL LETTER I WITH TILDE BELOW
      case 0x1e2e => 0x1e2f // LATIN CAPITAL LETTER I WITH DIAERESIS AND ACUTE
      case 0x1e30 => 0x1e31 // LATIN CAPITAL LETTER K WITH ACUTE
      case 0x1e32 => 0x1e33 // LATIN CAPITAL LETTER K WITH DOT BELOW
      case 0x1e34 => 0x1e35 // LATIN CAPITAL LETTER K WITH LINE BELOW
      case 0x1e36 => 0x1e37 // LATIN CAPITAL LETTER L WITH DOT BELOW
      case 0x1e38 => 0x1e39 // LATIN CAPITAL LETTER L WITH DOT BELOW AND MACRON
      case 0x1e3a => 0x1e3b // LATIN CAPITAL LETTER L WITH LINE BELOW
      case 0x1e3c => 0x1e3d // LATIN CAPITAL LETTER L WITH CIRCUMFLEX BELOW
      case 0x1e3e => 0x1e3f // LATIN CAPITAL LETTER M WITH ACUTE
      case 0x1e40 => 0x1e41 // LATIN CAPITAL LETTER M WITH DOT ABOVE
      case 0x1e42 => 0x1e43 // LATIN CAPITAL LETTER M WITH DOT BELOW
      case 0x1e44 => 0x1e45 // LATIN CAPITAL LETTER N WITH DOT ABOVE
      case 0x1e46 => 0x1e47 // LATIN CAPITAL LETTER N WITH DOT BELOW
      case 0x1e48 => 0x1e49 // LATIN CAPITAL LETTER N WITH LINE BELOW
      case 0x1e4a => 0x1e4b // LATIN CAPITAL LETTER N WITH CIRCUMFLEX BELOW
      case 0x1e4c => 0x1e4d // LATIN CAPITAL LETTER O WITH TILDE AND ACUTE
      case 0x1e4e => 0x1e4f // LATIN CAPITAL LETTER O WITH TILDE AND DIAERESIS
      case 0x1e50 => 0x1e51 // LATIN CAPITAL LETTER O WITH MACRON AND GRAVE
      case 0x1e52 => 0x1e53 // LATIN CAPITAL LETTER O WITH MACRON AND ACUTE
      case 0x1e54 => 0x1e55 // LATIN CAPITAL LETTER P WITH ACUTE
      case 0x1e56 => 0x1e57 // LATIN CAPITAL LETTER P WITH DOT ABOVE
      case 0x1e58 => 0x1e59 // LATIN CAPITAL LETTER R WITH DOT ABOVE
      case 0x1e5a => 0x1e5b // LATIN CAPITAL LETTER R WITH DOT BELOW
      case 0x1e5c => 0x1e5d // LATIN CAPITAL LETTER R WITH DOT BELOW AND MACRON
      case 0x1e5e => 0x1e5f // LATIN CAPITAL LETTER R WITH LINE BELOW
      case 0x1e60 => 0x1e61 // LATIN CAPITAL LETTER S WITH DOT ABOVE
      case 0x1e62 => 0x1e63 // LATIN CAPITAL LETTER S WITH DOT BELOW
      case 0x1e64 => 0x1e65 // LATIN CAPITAL LETTER S WITH ACUTE AND DOT ABOVE
      case 0x1e66 => 0x1e67 // LATIN CAPITAL LETTER S WITH CARON AND DOT ABOVE
      case 0x1e68 => 0x1e69 // LATIN CAPITAL LETTER S WITH DOT BELOW AND DOT ABOVE
      case 0x1e6a => 0x1e6b // LATIN CAPITAL LETTER T WITH DOT ABOVE
      case 0x1e6c => 0x1e6d // LATIN CAPITAL LETTER T WITH DOT BELOW
      case 0x1e6e => 0x1e6f // LATIN CAPITAL LETTER T WITH LINE BELOW
      case 0x1e70 => 0x1e71 // LATIN CAPITAL LETTER T WITH CIRCUMFLEX BELOW
      case 0x1e72 => 0x1e73 // LATIN CAPITAL LETTER U WITH DIAERESIS BELOW
      case 0x1e74 => 0x1e75 // LATIN CAPITAL LETTER U WITH TILDE BELOW
      case 0x1e76 => 0x1e77 // LATIN CAPITAL LETTER U WITH CIRCUMFLEX BELOW
      case 0x1e78 => 0x1e79 // LATIN CAPITAL LETTER U WITH TILDE AND ACUTE
      case 0x1e7a => 0x1e7b // LATIN CAPITAL LETTER U WITH MACRON AND DIAERESIS
      case 0x1e7c => 0x1e7d // LATIN CAPITAL LETTER V WITH TILDE
      case 0x1e7e => 0x1e7f // LATIN CAPITAL LETTER V WITH DOT BELOW
      case 0x1e80 => 0x1e81 // LATIN CAPITAL LETTER W WITH GRAVE
      case 0x1e82 => 0x1e83 // LATIN CAPITAL LETTER W WITH ACUTE
      case 0x1e84 => 0x1e85 // LATIN CAPITAL LETTER W WITH DIAERESIS
      case 0x1e86 => 0x1e87 // LATIN CAPITAL LETTER W WITH DOT ABOVE
      case 0x1e88 => 0x1e89 // LATIN CAPITAL LETTER W WITH DOT BELOW
      case 0x1e8a => 0x1e8b // LATIN CAPITAL LETTER X WITH DOT ABOVE
      case 0x1e8c => 0x1e8d // LATIN CAPITAL LETTER X WITH DIAERESIS
      case 0x1e8e => 0x1e8f // LATIN CAPITAL LETTER Y WITH DOT ABOVE
      case 0x1e90 => 0x1e91 // LATIN CAPITAL LETTER Z WITH CIRCUMFLEX
      case 0x1e92 => 0x1e93 // LATIN CAPITAL LETTER Z WITH DOT BELOW
      case 0x1e94 => 0x1e95 // LATIN CAPITAL LETTER Z WITH LINE BELOW
      case 0x1e9b => 0x1e61 // LATIN SMALL LETTER LONG S WITH DOT ABOVE
      case 0x1ea0 => 0x1ea1 // LATIN CAPITAL LETTER A WITH DOT BELOW
      case 0x1ea2 => 0x1ea3 // LATIN CAPITAL LETTER A WITH HOOK ABOVE
      case 0x1ea4 => 0x1ea5 // LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND ACUTE
      case 0x1ea6 => 0x1ea7 // LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND GRAVE
      case 0x1ea8 => 0x1ea9 // LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND HOOK ABOVE
      case 0x1eaa => 0x1eab // LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND TILDE
      case 0x1eac => 0x1ead // LATIN CAPITAL LETTER A WITH CIRCUMFLEX AND DOT BELOW
      case 0x1eae => 0x1eaf // LATIN CAPITAL LETTER A WITH BREVE AND ACUTE
      case 0x1eb0 => 0x1eb1 // LATIN CAPITAL LETTER A WITH BREVE AND GRAVE
      case 0x1eb2 => 0x1eb3 // LATIN CAPITAL LETTER A WITH BREVE AND HOOK ABOVE
      case 0x1eb4 => 0x1eb5 // LATIN CAPITAL LETTER A WITH BREVE AND TILDE
      case 0x1eb6 => 0x1eb7 // LATIN CAPITAL LETTER A WITH BREVE AND DOT BELOW
      case 0x1eb8 => 0x1eb9 // LATIN CAPITAL LETTER E WITH DOT BELOW
      case 0x1eba => 0x1ebb // LATIN CAPITAL LETTER E WITH HOOK ABOVE
      case 0x1ebc => 0x1ebd // LATIN CAPITAL LETTER E WITH TILDE
      case 0x1ebe => 0x1ebf // LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND ACUTE
      case 0x1ec0 => 0x1ec1 // LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND GRAVE
      case 0x1ec2 => 0x1ec3 // LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND HOOK ABOVE
      case 0x1ec4 => 0x1ec5 // LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND TILDE
      case 0x1ec6 => 0x1ec7 // LATIN CAPITAL LETTER E WITH CIRCUMFLEX AND DOT BELOW
      case 0x1ec8 => 0x1ec9 // LATIN CAPITAL LETTER I WITH HOOK ABOVE
      case 0x1eca => 0x1ecb // LATIN CAPITAL LETTER I WITH DOT BELOW
      case 0x1ecc => 0x1ecd // LATIN CAPITAL LETTER O WITH DOT BELOW
      case 0x1ece => 0x1ecf // LATIN CAPITAL LETTER O WITH HOOK ABOVE
      case 0x1ed0 => 0x1ed1 // LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND ACUTE
      case 0x1ed2 => 0x1ed3 // LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND GRAVE
      case 0x1ed4 => 0x1ed5 // LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND HOOK ABOVE
      case 0x1ed6 => 0x1ed7 // LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND TILDE
      case 0x1ed8 => 0x1ed9 // LATIN CAPITAL LETTER O WITH CIRCUMFLEX AND DOT BELOW
      case 0x1eda => 0x1edb // LATIN CAPITAL LETTER O WITH HORN AND ACUTE
      case 0x1edc => 0x1edd // LATIN CAPITAL LETTER O WITH HORN AND GRAVE
      case 0x1ede => 0x1edf // LATIN CAPITAL LETTER O WITH HORN AND HOOK ABOVE
      case 0x1ee0 => 0x1ee1 // LATIN CAPITAL LETTER O WITH HORN AND TILDE
      case 0x1ee2 => 0x1ee3 // LATIN CAPITAL LETTER O WITH HORN AND DOT BELOW
      case 0x1ee4 => 0x1ee5 // LATIN CAPITAL LETTER U WITH DOT BELOW
      case 0x1ee6 => 0x1ee7 // LATIN CAPITAL LETTER U WITH HOOK ABOVE
      case 0x1ee8 => 0x1ee9 // LATIN CAPITAL LETTER U WITH HORN AND ACUTE
      case 0x1eea => 0x1eeb // LATIN CAPITAL LETTER U WITH HORN AND GRAVE
      case 0x1eec => 0x1eed // LATIN CAPITAL LETTER U WITH HORN AND HOOK ABOVE
      case 0x1eee => 0x1eef // LATIN CAPITAL LETTER U WITH HORN AND TILDE
      case 0x1ef0 => 0x1ef1 // LATIN CAPITAL LETTER U WITH HORN AND DOT BELOW
      case 0x1ef2 => 0x1ef3 // LATIN CAPITAL LETTER Y WITH GRAVE
      case 0x1ef4 => 0x1ef5 // LATIN CAPITAL LETTER Y WITH DOT BELOW
      case 0x1ef6 => 0x1ef7 // LATIN CAPITAL LETTER Y WITH HOOK ABOVE
      case 0x1ef8 => 0x1ef9 // LATIN CAPITAL LETTER Y WITH TILDE
      case 0x1efa => 0x1efb // LATIN CAPITAL LETTER MIDDLE-WELSH LL
      case 0x1efc => 0x1efd // LATIN CAPITAL LETTER MIDDLE-WELSH V
      case 0x1efe => 0x1eff // LATIN CAPITAL LETTER Y WITH LOOP
      case 0x1f08 => 0x1f00 // GREEK CAPITAL LETTER ALPHA WITH PSILI
      case 0x1f09 => 0x1f01 // GREEK CAPITAL LETTER ALPHA WITH DASIA
      case 0x1f0a => 0x1f02 // GREEK CAPITAL LETTER ALPHA WITH PSILI AND VARIA
      case 0x1f0b => 0x1f03 // GREEK CAPITAL LETTER ALPHA WITH DASIA AND VARIA
      case 0x1f0c => 0x1f04 // GREEK CAPITAL LETTER ALPHA WITH PSILI AND OXIA
      case 0x1f0d => 0x1f05 // GREEK CAPITAL LETTER ALPHA WITH DASIA AND OXIA
      case 0x1f0e => 0x1f06 // GREEK CAPITAL LETTER ALPHA WITH PSILI AND PERISPOMENI
      case 0x1f0f => 0x1f07 // GREEK CAPITAL LETTER ALPHA WITH DASIA AND PERISPOMENI
      case 0x1f18 => 0x1f10 // GREEK CAPITAL LETTER EPSILON WITH PSILI
      case 0x1f19 => 0x1f11 // GREEK CAPITAL LETTER EPSILON WITH DASIA
      case 0x1f1a => 0x1f12 // GREEK CAPITAL LETTER EPSILON WITH PSILI AND VARIA
      case 0x1f1b => 0x1f13 // GREEK CAPITAL LETTER EPSILON WITH DASIA AND VARIA
      case 0x1f1c => 0x1f14 // GREEK CAPITAL LETTER EPSILON WITH PSILI AND OXIA
      case 0x1f1d => 0x1f15 // GREEK CAPITAL LETTER EPSILON WITH DASIA AND OXIA
      case 0x1f28 => 0x1f20 // GREEK CAPITAL LETTER ETA WITH PSILI
      case 0x1f29 => 0x1f21 // GREEK CAPITAL LETTER ETA WITH DASIA
      case 0x1f2a => 0x1f22 // GREEK CAPITAL LETTER ETA WITH PSILI AND VARIA
      case 0x1f2b => 0x1f23 // GREEK CAPITAL LETTER ETA WITH DASIA AND VARIA
      case 0x1f2c => 0x1f24 // GREEK CAPITAL LETTER ETA WITH PSILI AND OXIA
      case 0x1f2d => 0x1f25 // GREEK CAPITAL LETTER ETA WITH DASIA AND OXIA
      case 0x1f2e => 0x1f26 // GREEK CAPITAL LETTER ETA WITH PSILI AND PERISPOMENI
      case 0x1f2f => 0x1f27 // GREEK CAPITAL LETTER ETA WITH DASIA AND PERISPOMENI
      case 0x1f38 => 0x1f30 // GREEK CAPITAL LETTER IOTA WITH PSILI
      case 0x1f39 => 0x1f31 // GREEK CAPITAL LETTER IOTA WITH DASIA
      case 0x1f3a => 0x1f32 // GREEK CAPITAL LETTER IOTA WITH PSILI AND VARIA
      case 0x1f3b => 0x1f33 // GREEK CAPITAL LETTER IOTA WITH DASIA AND VARIA
      case 0x1f3c => 0x1f34 // GREEK CAPITAL LETTER IOTA WITH PSILI AND OXIA
      case 0x1f3d => 0x1f35 // GREEK CAPITAL LETTER IOTA WITH DASIA AND OXIA
      case 0x1f3e => 0x1f36 // GREEK CAPITAL LETTER IOTA WITH PSILI AND PERISPOMENI
      case 0x1f3f => 0x1f37 // GREEK CAPITAL LETTER IOTA WITH DASIA AND PERISPOMENI
      case 0x1f48 => 0x1f40 // GREEK CAPITAL LETTER OMICRON WITH PSILI
      case 0x1f49 => 0x1f41 // GREEK CAPITAL LETTER OMICRON WITH DASIA
      case 0x1f4a => 0x1f42 // GREEK CAPITAL LETTER OMICRON WITH PSILI AND VARIA
      case 0x1f4b => 0x1f43 // GREEK CAPITAL LETTER OMICRON WITH DASIA AND VARIA
      case 0x1f4c => 0x1f44 // GREEK CAPITAL LETTER OMICRON WITH PSILI AND OXIA
      case 0x1f4d => 0x1f45 // GREEK CAPITAL LETTER OMICRON WITH DASIA AND OXIA
      case 0x1f59 => 0x1f51 // GREEK CAPITAL LETTER UPSILON WITH DASIA
      case 0x1f5b => 0x1f53 // GREEK CAPITAL LETTER UPSILON WITH DASIA AND VARIA
      case 0x1f5d => 0x1f55 // GREEK CAPITAL LETTER UPSILON WITH DASIA AND OXIA
      case 0x1f5f => 0x1f57 // GREEK CAPITAL LETTER UPSILON WITH DASIA AND PERISPOMENI
      case 0x1f68 => 0x1f60 // GREEK CAPITAL LETTER OMEGA WITH PSILI
      case 0x1f69 => 0x1f61 // GREEK CAPITAL LETTER OMEGA WITH DASIA
      case 0x1f6a => 0x1f62 // GREEK CAPITAL LETTER OMEGA WITH PSILI AND VARIA
      case 0x1f6b => 0x1f63 // GREEK CAPITAL LETTER OMEGA WITH DASIA AND VARIA
      case 0x1f6c => 0x1f64 // GREEK CAPITAL LETTER OMEGA WITH PSILI AND OXIA
      case 0x1f6d => 0x1f65 // GREEK CAPITAL LETTER OMEGA WITH DASIA AND OXIA
      case 0x1f6e => 0x1f66 // GREEK CAPITAL LETTER OMEGA WITH PSILI AND PERISPOMENI
      case 0x1f6f => 0x1f67 // GREEK CAPITAL LETTER OMEGA WITH DASIA AND PERISPOMENI
      case 0x1fb8 => 0x1fb0 // GREEK CAPITAL LETTER ALPHA WITH VRACHY
      case 0x1fb9 => 0x1fb1 // GREEK CAPITAL LETTER ALPHA WITH MACRON
      case 0x1fba => 0x1f70 // GREEK CAPITAL LETTER ALPHA WITH VARIA
      case 0x1fbb => 0x1f71 // GREEK CAPITAL LETTER ALPHA WITH OXIA
      case 0x1fbe => 0x03b9 // GREEK PROSGEGRAMMENI
      case 0x1fc8 => 0x1f72 // GREEK CAPITAL LETTER EPSILON WITH VARIA
      case 0x1fc9 => 0x1f73 // GREEK CAPITAL LETTER EPSILON WITH OXIA
      case 0x1fca => 0x1f74 // GREEK CAPITAL LETTER ETA WITH VARIA
      case 0x1fcb => 0x1f75 // GREEK CAPITAL LETTER ETA WITH OXIA
      case 0x1fd8 => 0x1fd0 // GREEK CAPITAL LETTER IOTA WITH VRACHY
      case 0x1fd9 => 0x1fd1 // GREEK CAPITAL LETTER IOTA WITH MACRON
      case 0x1fda => 0x1f76 // GREEK CAPITAL LETTER IOTA WITH VARIA
      case 0x1fdb => 0x1f77 // GREEK CAPITAL LETTER IOTA WITH OXIA
      case 0x1fe8 => 0x1fe0 // GREEK CAPITAL LETTER UPSILON WITH VRACHY
      case 0x1fe9 => 0x1fe1 // GREEK CAPITAL LETTER UPSILON WITH MACRON
      case 0x1fea => 0x1f7a // GREEK CAPITAL LETTER UPSILON WITH VARIA
      case 0x1feb => 0x1f7b // GREEK CAPITAL LETTER UPSILON WITH OXIA
      case 0x1fec => 0x1fe5 // GREEK CAPITAL LETTER RHO WITH DASIA
      case 0x1ff8 => 0x1f78 // GREEK CAPITAL LETTER OMICRON WITH VARIA
      case 0x1ff9 => 0x1f79 // GREEK CAPITAL LETTER OMICRON WITH OXIA
      case 0x1ffa => 0x1f7c // GREEK CAPITAL LETTER OMEGA WITH VARIA
      case 0x1ffb => 0x1f7d // GREEK CAPITAL LETTER OMEGA WITH OXIA
      case 0x2126 => 0x03c9 // OHM SIGN
      case 0x212a => 0x006b // KELVIN SIGN
      case 0x212b => 0x00e5 // ANGSTROM SIGN
      case 0x2132 => 0x214e // TURNED CAPITAL F
      case 0x2160 => 0x2170 // ROMAN NUMERAL ONE
      case 0x2161 => 0x2171 // ROMAN NUMERAL TWO
      case 0x2162 => 0x2172 // ROMAN NUMERAL THREE
      case 0x2163 => 0x2173 // ROMAN NUMERAL FOUR
      case 0x2164 => 0x2174 // ROMAN NUMERAL FIVE
      case 0x2165 => 0x2175 // ROMAN NUMERAL SIX
      case 0x2166 => 0x2176 // ROMAN NUMERAL SEVEN
      case 0x2167 => 0x2177 // ROMAN NUMERAL EIGHT
      case 0x2168 => 0x2178 // ROMAN NUMERAL NINE
      case 0x2169 => 0x2179 // ROMAN NUMERAL TEN
      case 0x216a => 0x217a // ROMAN NUMERAL ELEVEN
      case 0x216b => 0x217b // ROMAN NUMERAL TWELVE
      case 0x216c => 0x217c // ROMAN NUMERAL FIFTY
      case 0x216d => 0x217d // ROMAN NUMERAL ONE HUNDRED
      case 0x216e => 0x217e // ROMAN NUMERAL FIVE HUNDRED
      case 0x216f => 0x217f // ROMAN NUMERAL ONE THOUSAND
      case 0x2183 => 0x2184 // ROMAN NUMERAL REVERSED ONE HUNDRED
      case 0x24b6 => 0x24d0 // CIRCLED LATIN CAPITAL LETTER A
      case 0x24b7 => 0x24d1 // CIRCLED LATIN CAPITAL LETTER B
      case 0x24b8 => 0x24d2 // CIRCLED LATIN CAPITAL LETTER C
      case 0x24b9 => 0x24d3 // CIRCLED LATIN CAPITAL LETTER D
      case 0x24ba => 0x24d4 // CIRCLED LATIN CAPITAL LETTER E
      case 0x24bb => 0x24d5 // CIRCLED LATIN CAPITAL LETTER F
      case 0x24bc => 0x24d6 // CIRCLED LATIN CAPITAL LETTER G
      case 0x24bd => 0x24d7 // CIRCLED LATIN CAPITAL LETTER H
      case 0x24be => 0x24d8 // CIRCLED LATIN CAPITAL LETTER I
      case 0x24bf => 0x24d9 // CIRCLED LATIN CAPITAL LETTER J
      case 0x24c0 => 0x24da // CIRCLED LATIN CAPITAL LETTER K
      case 0x24c1 => 0x24db // CIRCLED LATIN CAPITAL LETTER L
      case 0x24c2 => 0x24dc // CIRCLED LATIN CAPITAL LETTER M
      case 0x24c3 => 0x24dd // CIRCLED LATIN CAPITAL LETTER N
      case 0x24c4 => 0x24de // CIRCLED LATIN CAPITAL LETTER O
      case 0x24c5 => 0x24df // CIRCLED LATIN CAPITAL LETTER P
      case 0x24c6 => 0x24e0 // CIRCLED LATIN CAPITAL LETTER Q
      case 0x24c7 => 0x24e1 // CIRCLED LATIN CAPITAL LETTER R
      case 0x24c8 => 0x24e2 // CIRCLED LATIN CAPITAL LETTER S
      case 0x24c9 => 0x24e3 // CIRCLED LATIN CAPITAL LETTER T
      case 0x24ca => 0x24e4 // CIRCLED LATIN CAPITAL LETTER U
      case 0x24cb => 0x24e5 // CIRCLED LATIN CAPITAL LETTER V
      case 0x24cc => 0x24e6 // CIRCLED LATIN CAPITAL LETTER W
      case 0x24cd => 0x24e7 // CIRCLED LATIN CAPITAL LETTER X
      case 0x24ce => 0x24e8 // CIRCLED LATIN CAPITAL LETTER Y
      case 0x24cf => 0x24e9 // CIRCLED LATIN CAPITAL LETTER Z
      case 0x2c00 => 0x2c30 // GLAGOLITIC CAPITAL LETTER AZU
      case 0x2c01 => 0x2c31 // GLAGOLITIC CAPITAL LETTER BUKY
      case 0x2c02 => 0x2c32 // GLAGOLITIC CAPITAL LETTER VEDE
      case 0x2c03 => 0x2c33 // GLAGOLITIC CAPITAL LETTER GLAGOLI
      case 0x2c04 => 0x2c34 // GLAGOLITIC CAPITAL LETTER DOBRO
      case 0x2c05 => 0x2c35 // GLAGOLITIC CAPITAL LETTER YESTU
      case 0x2c06 => 0x2c36 // GLAGOLITIC CAPITAL LETTER ZHIVETE
      case 0x2c07 => 0x2c37 // GLAGOLITIC CAPITAL LETTER DZELO
      case 0x2c08 => 0x2c38 // GLAGOLITIC CAPITAL LETTER ZEMLJA
      case 0x2c09 => 0x2c39 // GLAGOLITIC CAPITAL LETTER IZHE
      case 0x2c0a => 0x2c3a // GLAGOLITIC CAPITAL LETTER INITIAL IZHE
      case 0x2c0b => 0x2c3b // GLAGOLITIC CAPITAL LETTER I
      case 0x2c0c => 0x2c3c // GLAGOLITIC CAPITAL LETTER DJERVI
      case 0x2c0d => 0x2c3d // GLAGOLITIC CAPITAL LETTER KAKO
      case 0x2c0e => 0x2c3e // GLAGOLITIC CAPITAL LETTER LJUDIJE
      case 0x2c0f => 0x2c3f // GLAGOLITIC CAPITAL LETTER MYSLITE
      case 0x2c10 => 0x2c40 // GLAGOLITIC CAPITAL LETTER NASHI
      case 0x2c11 => 0x2c41 // GLAGOLITIC CAPITAL LETTER ONU
      case 0x2c12 => 0x2c42 // GLAGOLITIC CAPITAL LETTER POKOJI
      case 0x2c13 => 0x2c43 // GLAGOLITIC CAPITAL LETTER RITSI
      case 0x2c14 => 0x2c44 // GLAGOLITIC CAPITAL LETTER SLOVO
      case 0x2c15 => 0x2c45 // GLAGOLITIC CAPITAL LETTER TVRIDO
      case 0x2c16 => 0x2c46 // GLAGOLITIC CAPITAL LETTER UKU
      case 0x2c17 => 0x2c47 // GLAGOLITIC CAPITAL LETTER FRITU
      case 0x2c18 => 0x2c48 // GLAGOLITIC CAPITAL LETTER HERU
      case 0x2c19 => 0x2c49 // GLAGOLITIC CAPITAL LETTER OTU
      case 0x2c1a => 0x2c4a // GLAGOLITIC CAPITAL LETTER PE
      case 0x2c1b => 0x2c4b // GLAGOLITIC CAPITAL LETTER SHTA
      case 0x2c1c => 0x2c4c // GLAGOLITIC CAPITAL LETTER TSI
      case 0x2c1d => 0x2c4d // GLAGOLITIC CAPITAL LETTER CHRIVI
      case 0x2c1e => 0x2c4e // GLAGOLITIC CAPITAL LETTER SHA
      case 0x2c1f => 0x2c4f // GLAGOLITIC CAPITAL LETTER YERU
      case 0x2c20 => 0x2c50 // GLAGOLITIC CAPITAL LETTER YERI
      case 0x2c21 => 0x2c51 // GLAGOLITIC CAPITAL LETTER YATI
      case 0x2c22 => 0x2c52 // GLAGOLITIC CAPITAL LETTER SPIDERY HA
      case 0x2c23 => 0x2c53 // GLAGOLITIC CAPITAL LETTER YU
      case 0x2c24 => 0x2c54 // GLAGOLITIC CAPITAL LETTER SMALL YUS
      case 0x2c25 => 0x2c55 // GLAGOLITIC CAPITAL LETTER SMALL YUS WITH TAIL
      case 0x2c26 => 0x2c56 // GLAGOLITIC CAPITAL LETTER YO
      case 0x2c27 => 0x2c57 // GLAGOLITIC CAPITAL LETTER IOTATED SMALL YUS
      case 0x2c28 => 0x2c58 // GLAGOLITIC CAPITAL LETTER BIG YUS
      case 0x2c29 => 0x2c59 // GLAGOLITIC CAPITAL LETTER IOTATED BIG YUS
      case 0x2c2a => 0x2c5a // GLAGOLITIC CAPITAL LETTER FITA
      case 0x2c2b => 0x2c5b // GLAGOLITIC CAPITAL LETTER IZHITSA
      case 0x2c2c => 0x2c5c // GLAGOLITIC CAPITAL LETTER SHTAPIC
      case 0x2c2d => 0x2c5d // GLAGOLITIC CAPITAL LETTER TROKUTASTI A
      case 0x2c2e => 0x2c5e // GLAGOLITIC CAPITAL LETTER LATINATE MYSLITE
      case 0x2c2f => 0x2c5f // GLAGOLITIC CAPITAL LETTER CAUDATE CHRIVI
      case 0x2c60 => 0x2c61 // LATIN CAPITAL LETTER L WITH DOUBLE BAR
      case 0x2c62 => 0x026b // LATIN CAPITAL LETTER L WITH MIDDLE TILDE
      case 0x2c63 => 0x1d7d // LATIN CAPITAL LETTER P WITH STROKE
      case 0x2c64 => 0x027d // LATIN CAPITAL LETTER R WITH TAIL
      case 0x2c67 => 0x2c68 // LATIN CAPITAL LETTER H WITH DESCENDER
      case 0x2c69 => 0x2c6a // LATIN CAPITAL LETTER K WITH DESCENDER
      case 0x2c6b => 0x2c6c // LATIN CAPITAL LETTER Z WITH DESCENDER
      case 0x2c6d => 0x0251 // LATIN CAPITAL LETTER ALPHA
      case 0x2c6e => 0x0271 // LATIN CAPITAL LETTER M WITH HOOK
      case 0x2c6f => 0x0250 // LATIN CAPITAL LETTER TURNED A
      case 0x2c70 => 0x0252 // LATIN CAPITAL LETTER TURNED ALPHA
      case 0x2c72 => 0x2c73 // LATIN CAPITAL LETTER W WITH HOOK
      case 0x2c75 => 0x2c76 // LATIN CAPITAL LETTER HALF H
      case 0x2c7e => 0x023f // LATIN CAPITAL LETTER S WITH SWASH TAIL
      case 0x2c7f => 0x0240 // LATIN CAPITAL LETTER Z WITH SWASH TAIL
      case 0x2c80 => 0x2c81 // COPTIC CAPITAL LETTER ALFA
      case 0x2c82 => 0x2c83 // COPTIC CAPITAL LETTER VIDA
      case 0x2c84 => 0x2c85 // COPTIC CAPITAL LETTER GAMMA
      case 0x2c86 => 0x2c87 // COPTIC CAPITAL LETTER DALDA
      case 0x2c88 => 0x2c89 // COPTIC CAPITAL LETTER EIE
      case 0x2c8a => 0x2c8b // COPTIC CAPITAL LETTER SOU
      case 0x2c8c => 0x2c8d // COPTIC CAPITAL LETTER ZATA
      case 0x2c8e => 0x2c8f // COPTIC CAPITAL LETTER HATE
      case 0x2c90 => 0x2c91 // COPTIC CAPITAL LETTER THETHE
      case 0x2c92 => 0x2c93 // COPTIC CAPITAL LETTER IAUDA
      case 0x2c94 => 0x2c95 // COPTIC CAPITAL LETTER KAPA
      case 0x2c96 => 0x2c97 // COPTIC CAPITAL LETTER LAULA
      case 0x2c98 => 0x2c99 // COPTIC CAPITAL LETTER MI
      case 0x2c9a => 0x2c9b // COPTIC CAPITAL LETTER NI
      case 0x2c9c => 0x2c9d // COPTIC CAPITAL LETTER KSI
      case 0x2c9e => 0x2c9f // COPTIC CAPITAL LETTER O
      case 0x2ca0 => 0x2ca1 // COPTIC CAPITAL LETTER PI
      case 0x2ca2 => 0x2ca3 // COPTIC CAPITAL LETTER RO
      case 0x2ca4 => 0x2ca5 // COPTIC CAPITAL LETTER SIMA
      case 0x2ca6 => 0x2ca7 // COPTIC CAPITAL LETTER TAU
      case 0x2ca8 => 0x2ca9 // COPTIC CAPITAL LETTER UA
      case 0x2caa => 0x2cab // COPTIC CAPITAL LETTER FI
      case 0x2cac => 0x2cad // COPTIC CAPITAL LETTER KHI
      case 0x2cae => 0x2caf // COPTIC CAPITAL LETTER PSI
      case 0x2cb0 => 0x2cb1 // COPTIC CAPITAL LETTER OOU
      case 0x2cb2 => 0x2cb3 // COPTIC CAPITAL LETTER DIALECT-P ALEF
      case 0x2cb4 => 0x2cb5 // COPTIC CAPITAL LETTER OLD COPTIC AIN
      case 0x2cb6 => 0x2cb7 // COPTIC CAPITAL LETTER CRYPTOGRAMMIC EIE
      case 0x2cb8 => 0x2cb9 // COPTIC CAPITAL LETTER DIALECT-P KAPA
      case 0x2cba => 0x2cbb // COPTIC CAPITAL LETTER DIALECT-P NI
      case 0x2cbc => 0x2cbd // COPTIC CAPITAL LETTER CRYPTOGRAMMIC NI
      case 0x2cbe => 0x2cbf // COPTIC CAPITAL LETTER OLD COPTIC OOU
      case 0x2cc0 => 0x2cc1 // COPTIC CAPITAL LETTER SAMPI
      case 0x2cc2 => 0x2cc3 // COPTIC CAPITAL LETTER CROSSED SHEI
      case 0x2cc4 => 0x2cc5 // COPTIC CAPITAL LETTER OLD COPTIC SHEI
      case 0x2cc6 => 0x2cc7 // COPTIC CAPITAL LETTER OLD COPTIC ESH
      case 0x2cc8 => 0x2cc9 // COPTIC CAPITAL LETTER AKHMIMIC KHEI
      case 0x2cca => 0x2ccb // COPTIC CAPITAL LETTER DIALECT-P HORI
      case 0x2ccc => 0x2ccd // COPTIC CAPITAL LETTER OLD COPTIC HORI
      case 0x2cce => 0x2ccf // COPTIC CAPITAL LETTER OLD COPTIC HA
      case 0x2cd0 => 0x2cd1 // COPTIC CAPITAL LETTER L-SHAPED HA
      case 0x2cd2 => 0x2cd3 // COPTIC CAPITAL LETTER OLD COPTIC HEI
      case 0x2cd4 => 0x2cd5 // COPTIC CAPITAL LETTER OLD COPTIC HAT
      case 0x2cd6 => 0x2cd7 // COPTIC CAPITAL LETTER OLD COPTIC GANGIA
      case 0x2cd8 => 0x2cd9 // COPTIC CAPITAL LETTER OLD COPTIC DJA
      case 0x2cda => 0x2cdb // COPTIC CAPITAL LETTER OLD COPTIC SHIMA
      case 0x2cdc => 0x2cdd // COPTIC CAPITAL LETTER OLD NUBIAN SHIMA
      case 0x2cde => 0x2cdf // COPTIC CAPITAL LETTER OLD NUBIAN NGI
      case 0x2ce0 => 0x2ce1 // COPTIC CAPITAL LETTER OLD NUBIAN NYI
      case 0x2ce2 => 0x2ce3 // COPTIC CAPITAL LETTER OLD NUBIAN WAU
      case 0x2ceb => 0x2cec // COPTIC CAPITAL LETTER CRYPTOGRAMMIC SHEI
      case 0x2ced => 0x2cee // COPTIC CAPITAL LETTER CRYPTOGRAMMIC GANGIA
      case 0x2cf2 => 0x2cf3 // COPTIC CAPITAL LETTER BOHAIRIC KHEI
      case 0xa640 => 0xa641 // CYRILLIC CAPITAL LETTER ZEMLYA
      case 0xa642 => 0xa643 // CYRILLIC CAPITAL LETTER DZELO
      case 0xa644 => 0xa645 // CYRILLIC CAPITAL LETTER REVERSED DZE
      case 0xa646 => 0xa647 // CYRILLIC CAPITAL LETTER IOTA
      case 0xa648 => 0xa649 // CYRILLIC CAPITAL LETTER DJERV
      case 0xa64a => 0xa64b // CYRILLIC CAPITAL LETTER MONOGRAPH UK
      case 0xa64c => 0xa64d // CYRILLIC CAPITAL LETTER BROAD OMEGA
      case 0xa64e => 0xa64f // CYRILLIC CAPITAL LETTER NEUTRAL YER
      case 0xa650 => 0xa651 // CYRILLIC CAPITAL LETTER YERU WITH BACK YER
      case 0xa652 => 0xa653 // CYRILLIC CAPITAL LETTER IOTIFIED YAT
      case 0xa654 => 0xa655 // CYRILLIC CAPITAL LETTER REVERSED YU
      case 0xa656 => 0xa657 // CYRILLIC CAPITAL LETTER IOTIFIED A
      case 0xa658 => 0xa659 // CYRILLIC CAPITAL LETTER CLOSED LITTLE YUS
      case 0xa65a => 0xa65b // CYRILLIC CAPITAL LETTER BLENDED YUS
      case 0xa65c => 0xa65d // CYRILLIC CAPITAL LETTER IOTIFIED CLOSED LITTLE YUS
      case 0xa65e => 0xa65f // CYRILLIC CAPITAL LETTER YN
      case 0xa660 => 0xa661 // CYRILLIC CAPITAL LETTER REVERSED TSE
      case 0xa662 => 0xa663 // CYRILLIC CAPITAL LETTER SOFT DE
      case 0xa664 => 0xa665 // CYRILLIC CAPITAL LETTER SOFT EL
      case 0xa666 => 0xa667 // CYRILLIC CAPITAL LETTER SOFT EM
      case 0xa668 => 0xa669 // CYRILLIC CAPITAL LETTER MONOCULAR O
      case 0xa66a => 0xa66b // CYRILLIC CAPITAL LETTER BINOCULAR O
      case 0xa66c => 0xa66d // CYRILLIC CAPITAL LETTER DOUBLE MONOCULAR O
      case 0xa680 => 0xa681 // CYRILLIC CAPITAL LETTER DWE
      case 0xa682 => 0xa683 // CYRILLIC CAPITAL LETTER DZWE
      case 0xa684 => 0xa685 // CYRILLIC CAPITAL LETTER ZHWE
      case 0xa686 => 0xa687 // CYRILLIC CAPITAL LETTER CCHE
      case 0xa688 => 0xa689 // CYRILLIC CAPITAL LETTER DZZE
      case 0xa68a => 0xa68b // CYRILLIC CAPITAL LETTER TE WITH MIDDLE HOOK
      case 0xa68c => 0xa68d // CYRILLIC CAPITAL LETTER TWE
      case 0xa68e => 0xa68f // CYRILLIC CAPITAL LETTER TSWE
      case 0xa690 => 0xa691 // CYRILLIC CAPITAL LETTER TSSE
      case 0xa692 => 0xa693 // CYRILLIC CAPITAL LETTER TCHE
      case 0xa694 => 0xa695 // CYRILLIC CAPITAL LETTER HWE
      case 0xa696 => 0xa697 // CYRILLIC CAPITAL LETTER SHWE
      case 0xa698 => 0xa699 // CYRILLIC CAPITAL LETTER DOUBLE O
      case 0xa69a => 0xa69b // CYRILLIC CAPITAL LETTER CROSSED O
      case 0xa722 => 0xa723 // LATIN CAPITAL LETTER EGYPTOLOGICAL ALEF
      case 0xa724 => 0xa725 // LATIN CAPITAL LETTER EGYPTOLOGICAL AIN
      case 0xa726 => 0xa727 // LATIN CAPITAL LETTER HENG
      case 0xa728 => 0xa729 // LATIN CAPITAL LETTER TZ
      case 0xa72a => 0xa72b // LATIN CAPITAL LETTER TRESILLO
      case 0xa72c => 0xa72d // LATIN CAPITAL LETTER CUATRILLO
      case 0xa72e => 0xa72f // LATIN CAPITAL LETTER CUATRILLO WITH COMMA
      case 0xa732 => 0xa733 // LATIN CAPITAL LETTER AA
      case 0xa734 => 0xa735 // LATIN CAPITAL LETTER AO
      case 0xa736 => 0xa737 // LATIN CAPITAL LETTER AU
      case 0xa738 => 0xa739 // LATIN CAPITAL LETTER AV
      case 0xa73a => 0xa73b // LATIN CAPITAL LETTER AV WITH HORIZONTAL BAR
      case 0xa73c => 0xa73d // LATIN CAPITAL LETTER AY
      case 0xa73e => 0xa73f // LATIN CAPITAL LETTER REVERSED C WITH DOT
      case 0xa740 => 0xa741 // LATIN CAPITAL LETTER K WITH STROKE
      case 0xa742 => 0xa743 // LATIN CAPITAL LETTER K WITH DIAGONAL STROKE
      case 0xa744 => 0xa745 // LATIN CAPITAL LETTER K WITH STROKE AND DIAGONAL STROKE
      case 0xa746 => 0xa747 // LATIN CAPITAL LETTER BROKEN L
      case 0xa748 => 0xa749 // LATIN CAPITAL LETTER L WITH HIGH STROKE
      case 0xa74a => 0xa74b // LATIN CAPITAL LETTER O WITH LONG STROKE OVERLAY
      case 0xa74c => 0xa74d // LATIN CAPITAL LETTER O WITH LOOP
      case 0xa74e => 0xa74f // LATIN CAPITAL LETTER OO
      case 0xa750 => 0xa751 // LATIN CAPITAL LETTER P WITH STROKE THROUGH DESCENDER
      case 0xa752 => 0xa753 // LATIN CAPITAL LETTER P WITH FLOURISH
      case 0xa754 => 0xa755 // LATIN CAPITAL LETTER P WITH SQUIRREL TAIL
      case 0xa756 => 0xa757 // LATIN CAPITAL LETTER Q WITH STROKE THROUGH DESCENDER
      case 0xa758 => 0xa759 // LATIN CAPITAL LETTER Q WITH DIAGONAL STROKE
      case 0xa75a => 0xa75b // LATIN CAPITAL LETTER R ROTUNDA
      case 0xa75c => 0xa75d // LATIN CAPITAL LETTER RUM ROTUNDA
      case 0xa75e => 0xa75f // LATIN CAPITAL LETTER V WITH DIAGONAL STROKE
      case 0xa760 => 0xa761 // LATIN CAPITAL LETTER VY
      case 0xa762 => 0xa763 // LATIN CAPITAL LETTER VISIGOTHIC Z
      case 0xa764 => 0xa765 // LATIN CAPITAL LETTER THORN WITH STROKE
      case 0xa766 => 0xa767 // LATIN CAPITAL LETTER THORN WITH STROKE THROUGH DESCENDER
      case 0xa768 => 0xa769 // LATIN CAPITAL LETTER VEND
      case 0xa76a => 0xa76b // LATIN CAPITAL LETTER ET
      case 0xa76c => 0xa76d // LATIN CAPITAL LETTER IS
      case 0xa76e => 0xa76f // LATIN CAPITAL LETTER CON
      case 0xa779 => 0xa77a // LATIN CAPITAL LETTER INSULAR D
      case 0xa77b => 0xa77c // LATIN CAPITAL LETTER INSULAR F
      case 0xa77d => 0x1d79 // LATIN CAPITAL LETTER INSULAR G
      case 0xa77e => 0xa77f // LATIN CAPITAL LETTER TURNED INSULAR G
      case 0xa780 => 0xa781 // LATIN CAPITAL LETTER TURNED L
      case 0xa782 => 0xa783 // LATIN CAPITAL LETTER INSULAR R
      case 0xa784 => 0xa785 // LATIN CAPITAL LETTER INSULAR S
      case 0xa786 => 0xa787 // LATIN CAPITAL LETTER INSULAR T
      case 0xa78b => 0xa78c // LATIN CAPITAL LETTER SALTILLO
      case 0xa78d => 0x0265 // LATIN CAPITAL LETTER TURNED H
      case 0xa790 => 0xa791 // LATIN CAPITAL LETTER N WITH DESCENDER
      case 0xa792 => 0xa793 // LATIN CAPITAL LETTER C WITH BAR
      case 0xa796 => 0xa797 // LATIN CAPITAL LETTER B WITH FLOURISH
      case 0xa798 => 0xa799 // LATIN CAPITAL LETTER F WITH STROKE
      case 0xa79a => 0xa79b // LATIN CAPITAL LETTER VOLAPUK AE
      case 0xa79c => 0xa79d // LATIN CAPITAL LETTER VOLAPUK OE
      case 0xa79e => 0xa79f // LATIN CAPITAL LETTER VOLAPUK UE
      case 0xa7a0 => 0xa7a1 // LATIN CAPITAL LETTER G WITH OBLIQUE STROKE
      case 0xa7a2 => 0xa7a3 // LATIN CAPITAL LETTER K WITH OBLIQUE STROKE
      case 0xa7a4 => 0xa7a5 // LATIN CAPITAL LETTER N WITH OBLIQUE STROKE
      case 0xa7a6 => 0xa7a7 // LATIN CAPITAL LETTER R WITH OBLIQUE STROKE
      case 0xa7a8 => 0xa7a9 // LATIN CAPITAL LETTER S WITH OBLIQUE STROKE
      case 0xa7aa => 0x0266 // LATIN CAPITAL LETTER H WITH HOOK
      case 0xa7ab => 0x025c // LATIN CAPITAL LETTER REVERSED OPEN E
      case 0xa7ac => 0x0261 // LATIN CAPITAL LETTER SCRIPT G
      case 0xa7ad => 0x026c // LATIN CAPITAL LETTER L WITH BELT
      case 0xa7ae => 0x026a // LATIN CAPITAL LETTER SMALL CAPITAL I
      case 0xa7b0 => 0x029e // LATIN CAPITAL LETTER TURNED K
      case 0xa7b1 => 0x0287 // LATIN CAPITAL LETTER TURNED T
      case 0xa7b2 => 0x029d // LATIN CAPITAL LETTER J WITH CROSSED-TAIL
      case 0xa7b3 => 0xab53 // LATIN CAPITAL LETTER CHI
      case 0xa7b4 => 0xa7b5 // LATIN CAPITAL LETTER BETA
      case 0xa7b6 => 0xa7b7 // LATIN CAPITAL LETTER OMEGA
      case 0xa7b8 => 0xa7b9 // LATIN CAPITAL LETTER U WITH STROKE
      case 0xa7ba => 0xa7bb // LATIN CAPITAL LETTER GLOTTAL A
      case 0xa7bc => 0xa7bd // LATIN CAPITAL LETTER GLOTTAL I
      case 0xa7be => 0xa7bf // LATIN CAPITAL LETTER GLOTTAL U
      case 0xa7c0 => 0xa7c1 // LATIN CAPITAL LETTER OLD POLISH O
      case 0xa7c2 => 0xa7c3 // LATIN CAPITAL LETTER ANGLICANA W
      case 0xa7c4 => 0xa794 // LATIN CAPITAL LETTER C WITH PALATAL HOOK
      case 0xa7c5 => 0x0282 // LATIN CAPITAL LETTER S WITH HOOK
      case 0xa7c6 => 0x1d8e // LATIN CAPITAL LETTER Z WITH PALATAL HOOK
      case 0xa7c7 => 0xa7c8 // LATIN CAPITAL LETTER D WITH SHORT STROKE OVERLAY
      case 0xa7c9 => 0xa7ca // LATIN CAPITAL LETTER S WITH SHORT STROKE OVERLAY
      case 0xa7d0 => 0xa7d1 // LATIN CAPITAL LETTER CLOSED INSULAR G
      case 0xa7d6 => 0xa7d7 // LATIN CAPITAL LETTER MIDDLE SCOTS S
      case 0xa7d8 => 0xa7d9 // LATIN CAPITAL LETTER SIGMOID S
      case 0xa7f5 => 0xa7f6 // LATIN CAPITAL LETTER REVERSED HALF H
      case 0xab70 => 0x13a0 // CHEROKEE SMALL LETTER A
      case 0xab71 => 0x13a1 // CHEROKEE SMALL LETTER E
      case 0xab72 => 0x13a2 // CHEROKEE SMALL LETTER I
      case 0xab73 => 0x13a3 // CHEROKEE SMALL LETTER O
      case 0xab74 => 0x13a4 // CHEROKEE SMALL LETTER U
      case 0xab75 => 0x13a5 // CHEROKEE SMALL LETTER V
      case 0xab76 => 0x13a6 // CHEROKEE SMALL LETTER GA
      case 0xab77 => 0x13a7 // CHEROKEE SMALL LETTER KA
      case 0xab78 => 0x13a8 // CHEROKEE SMALL LETTER GE
      case 0xab79 => 0x13a9 // CHEROKEE SMALL LETTER GI
      case 0xab7a => 0x13aa // CHEROKEE SMALL LETTER GO
      case 0xab7b => 0x13ab // CHEROKEE SMALL LETTER GU
      case 0xab7c => 0x13ac // CHEROKEE SMALL LETTER GV
      case 0xab7d => 0x13ad // CHEROKEE SMALL LETTER HA
      case 0xab7e => 0x13ae // CHEROKEE SMALL LETTER HE
      case 0xab7f => 0x13af // CHEROKEE SMALL LETTER HI
      case 0xab80 => 0x13b0 // CHEROKEE SMALL LETTER HO
      case 0xab81 => 0x13b1 // CHEROKEE SMALL LETTER HU
      case 0xab82 => 0x13b2 // CHEROKEE SMALL LETTER HV
      case 0xab83 => 0x13b3 // CHEROKEE SMALL LETTER LA
      case 0xab84 => 0x13b4 // CHEROKEE SMALL LETTER LE
      case 0xab85 => 0x13b5 // CHEROKEE SMALL LETTER LI
      case 0xab86 => 0x13b6 // CHEROKEE SMALL LETTER LO
      case 0xab87 => 0x13b7 // CHEROKEE SMALL LETTER LU
      case 0xab88 => 0x13b8 // CHEROKEE SMALL LETTER LV
      case 0xab89 => 0x13b9 // CHEROKEE SMALL LETTER MA
      case 0xab8a => 0x13ba // CHEROKEE SMALL LETTER ME
      case 0xab8b => 0x13bb // CHEROKEE SMALL LETTER MI
      case 0xab8c => 0x13bc // CHEROKEE SMALL LETTER MO
      case 0xab8d => 0x13bd // CHEROKEE SMALL LETTER MU
      case 0xab8e => 0x13be // CHEROKEE SMALL LETTER NA
      case 0xab8f => 0x13bf // CHEROKEE SMALL LETTER HNA
      case 0xab90 => 0x13c0 // CHEROKEE SMALL LETTER NAH
      case 0xab91 => 0x13c1 // CHEROKEE SMALL LETTER NE
      case 0xab92 => 0x13c2 // CHEROKEE SMALL LETTER NI
      case 0xab93 => 0x13c3 // CHEROKEE SMALL LETTER NO
      case 0xab94 => 0x13c4 // CHEROKEE SMALL LETTER NU
      case 0xab95 => 0x13c5 // CHEROKEE SMALL LETTER NV
      case 0xab96 => 0x13c6 // CHEROKEE SMALL LETTER QUA
      case 0xab97 => 0x13c7 // CHEROKEE SMALL LETTER QUE
      case 0xab98 => 0x13c8 // CHEROKEE SMALL LETTER QUI
      case 0xab99 => 0x13c9 // CHEROKEE SMALL LETTER QUO
      case 0xab9a => 0x13ca // CHEROKEE SMALL LETTER QUU
      case 0xab9b => 0x13cb // CHEROKEE SMALL LETTER QUV
      case 0xab9c => 0x13cc // CHEROKEE SMALL LETTER SA
      case 0xab9d => 0x13cd // CHEROKEE SMALL LETTER S
      case 0xab9e => 0x13ce // CHEROKEE SMALL LETTER SE
      case 0xab9f => 0x13cf // CHEROKEE SMALL LETTER SI
      case 0xaba0 => 0x13d0 // CHEROKEE SMALL LETTER SO
      case 0xaba1 => 0x13d1 // CHEROKEE SMALL LETTER SU
      case 0xaba2 => 0x13d2 // CHEROKEE SMALL LETTER SV
      case 0xaba3 => 0x13d3 // CHEROKEE SMALL LETTER DA
      case 0xaba4 => 0x13d4 // CHEROKEE SMALL LETTER TA
      case 0xaba5 => 0x13d5 // CHEROKEE SMALL LETTER DE
      case 0xaba6 => 0x13d6 // CHEROKEE SMALL LETTER TE
      case 0xaba7 => 0x13d7 // CHEROKEE SMALL LETTER DI
      case 0xaba8 => 0x13d8 // CHEROKEE SMALL LETTER TI
      case 0xaba9 => 0x13d9 // CHEROKEE SMALL LETTER DO
      case 0xabaa => 0x13da // CHEROKEE SMALL LETTER DU
      case 0xabab => 0x13db // CHEROKEE SMALL LETTER DV
      case 0xabac => 0x13dc // CHEROKEE SMALL LETTER DLA
      case 0xabad => 0x13dd // CHEROKEE SMALL LETTER TLA
      case 0xabae => 0x13de // CHEROKEE SMALL LETTER TLE
      case 0xabaf => 0x13df // CHEROKEE SMALL LETTER TLI
      case 0xabb0 => 0x13e0 // CHEROKEE SMALL LETTER TLO
      case 0xabb1 => 0x13e1 // CHEROKEE SMALL LETTER TLU
      case 0xabb2 => 0x13e2 // CHEROKEE SMALL LETTER TLV
      case 0xabb3 => 0x13e3 // CHEROKEE SMALL LETTER TSA
      case 0xabb4 => 0x13e4 // CHEROKEE SMALL LETTER TSE
      case 0xabb5 => 0x13e5 // CHEROKEE SMALL LETTER TSI
      case 0xabb6 => 0x13e6 // CHEROKEE SMALL LETTER TSO
      case 0xabb7 => 0x13e7 // CHEROKEE SMALL LETTER TSU
      case 0xabb8 => 0x13e8 // CHEROKEE SMALL LETTER TSV
      case 0xabb9 => 0x13e9 // CHEROKEE SMALL LETTER WA
      case 0xabba => 0x13ea // CHEROKEE SMALL LETTER WE
      case 0xabbb => 0x13eb // CHEROKEE SMALL LETTER WI
      case 0xabbc => 0x13ec // CHEROKEE SMALL LETTER WO
      case 0xabbd => 0x13ed // CHEROKEE SMALL LETTER WU
      case 0xabbe => 0x13ee // CHEROKEE SMALL LETTER WV
      case 0xabbf => 0x13ef // CHEROKEE SMALL LETTER YA
      case 0xff21 => 0xff41 // FULLWIDTH LATIN CAPITAL LETTER A
      case 0xff22 => 0xff42 // FULLWIDTH LATIN CAPITAL LETTER B
      case 0xff23 => 0xff43 // FULLWIDTH LATIN CAPITAL LETTER C
      case 0xff24 => 0xff44 // FULLWIDTH LATIN CAPITAL LETTER D
      case 0xff25 => 0xff45 // FULLWIDTH LATIN CAPITAL LETTER E
      case 0xff26 => 0xff46 // FULLWIDTH LATIN CAPITAL LETTER F
      case 0xff27 => 0xff47 // FULLWIDTH LATIN CAPITAL LETTER G
      case 0xff28 => 0xff48 // FULLWIDTH LATIN CAPITAL LETTER H
      case 0xff29 => 0xff49 // FULLWIDTH LATIN CAPITAL LETTER I
      case 0xff2a => 0xff4a // FULLWIDTH LATIN CAPITAL LETTER J
      case 0xff2b => 0xff4b // FULLWIDTH LATIN CAPITAL LETTER K
      case 0xff2c => 0xff4c // FULLWIDTH LATIN CAPITAL LETTER L
      case 0xff2d => 0xff4d // FULLWIDTH LATIN CAPITAL LETTER M
      case 0xff2e => 0xff4e // FULLWIDTH LATIN CAPITAL LETTER N
      case 0xff2f => 0xff4f // FULLWIDTH LATIN CAPITAL LETTER O
      case 0xff30 => 0xff50 // FULLWIDTH LATIN CAPITAL LETTER P
      case 0xff31 => 0xff51 // FULLWIDTH LATIN CAPITAL LETTER Q
      case 0xff32 => 0xff52 // FULLWIDTH LATIN CAPITAL LETTER R
      case 0xff33 => 0xff53 // FULLWIDTH LATIN CAPITAL LETTER S
      case 0xff34 => 0xff54 // FULLWIDTH LATIN CAPITAL LETTER T
      case 0xff35 => 0xff55 // FULLWIDTH LATIN CAPITAL LETTER U
      case 0xff36 => 0xff56 // FULLWIDTH LATIN CAPITAL LETTER V
      case 0xff37 => 0xff57 // FULLWIDTH LATIN CAPITAL LETTER W
      case 0xff38 => 0xff58 // FULLWIDTH LATIN CAPITAL LETTER X
      case 0xff39 => 0xff59 // FULLWIDTH LATIN CAPITAL LETTER Y
      case 0xff3a => 0xff5a // FULLWIDTH LATIN CAPITAL LETTER Z
      case 0x10400 => 0x10428 // DESERET CAPITAL LETTER LONG I
      case 0x10401 => 0x10429 // DESERET CAPITAL LETTER LONG E
      case 0x10402 => 0x1042a // DESERET CAPITAL LETTER LONG A
      case 0x10403 => 0x1042b // DESERET CAPITAL LETTER LONG AH
      case 0x10404 => 0x1042c // DESERET CAPITAL LETTER LONG O
      case 0x10405 => 0x1042d // DESERET CAPITAL LETTER LONG OO
      case 0x10406 => 0x1042e // DESERET CAPITAL LETTER SHORT I
      case 0x10407 => 0x1042f // DESERET CAPITAL LETTER SHORT E
      case 0x10408 => 0x10430 // DESERET CAPITAL LETTER SHORT A
      case 0x10409 => 0x10431 // DESERET CAPITAL LETTER SHORT AH
      case 0x1040a => 0x10432 // DESERET CAPITAL LETTER SHORT O
      case 0x1040b => 0x10433 // DESERET CAPITAL LETTER SHORT OO
      case 0x1040c => 0x10434 // DESERET CAPITAL LETTER AY
      case 0x1040d => 0x10435 // DESERET CAPITAL LETTER OW
      case 0x1040e => 0x10436 // DESERET CAPITAL LETTER WU
      case 0x1040f => 0x10437 // DESERET CAPITAL LETTER YEE
      case 0x10410 => 0x10438 // DESERET CAPITAL LETTER H
      case 0x10411 => 0x10439 // DESERET CAPITAL LETTER PEE
      case 0x10412 => 0x1043a // DESERET CAPITAL LETTER BEE
      case 0x10413 => 0x1043b // DESERET CAPITAL LETTER TEE
      case 0x10414 => 0x1043c // DESERET CAPITAL LETTER DEE
      case 0x10415 => 0x1043d // DESERET CAPITAL LETTER CHEE
      case 0x10416 => 0x1043e // DESERET CAPITAL LETTER JEE
      case 0x10417 => 0x1043f // DESERET CAPITAL LETTER KAY
      case 0x10418 => 0x10440 // DESERET CAPITAL LETTER GAY
      case 0x10419 => 0x10441 // DESERET CAPITAL LETTER EF
      case 0x1041a => 0x10442 // DESERET CAPITAL LETTER VEE
      case 0x1041b => 0x10443 // DESERET CAPITAL LETTER ETH
      case 0x1041c => 0x10444 // DESERET CAPITAL LETTER THEE
      case 0x1041d => 0x10445 // DESERET CAPITAL LETTER ES
      case 0x1041e => 0x10446 // DESERET CAPITAL LETTER ZEE
      case 0x1041f => 0x10447 // DESERET CAPITAL LETTER ESH
      case 0x10420 => 0x10448 // DESERET CAPITAL LETTER ZHEE
      case 0x10421 => 0x10449 // DESERET CAPITAL LETTER ER
      case 0x10422 => 0x1044a // DESERET CAPITAL LETTER EL
      case 0x10423 => 0x1044b // DESERET CAPITAL LETTER EM
      case 0x10424 => 0x1044c // DESERET CAPITAL LETTER EN
      case 0x10425 => 0x1044d // DESERET CAPITAL LETTER ENG
      case 0x10426 => 0x1044e // DESERET CAPITAL LETTER OI
      case 0x10427 => 0x1044f // DESERET CAPITAL LETTER EW
      case 0x104b0 => 0x104d8 // OSAGE CAPITAL LETTER A
      case 0x104b1 => 0x104d9 // OSAGE CAPITAL LETTER AI
      case 0x104b2 => 0x104da // OSAGE CAPITAL LETTER AIN
      case 0x104b3 => 0x104db // OSAGE CAPITAL LETTER AH
      case 0x104b4 => 0x104dc // OSAGE CAPITAL LETTER BRA
      case 0x104b5 => 0x104dd // OSAGE CAPITAL LETTER CHA
      case 0x104b6 => 0x104de // OSAGE CAPITAL LETTER EHCHA
      case 0x104b7 => 0x104df // OSAGE CAPITAL LETTER E
      case 0x104b8 => 0x104e0 // OSAGE CAPITAL LETTER EIN
      case 0x104b9 => 0x104e1 // OSAGE CAPITAL LETTER HA
      case 0x104ba => 0x104e2 // OSAGE CAPITAL LETTER HYA
      case 0x104bb => 0x104e3 // OSAGE CAPITAL LETTER I
      case 0x104bc => 0x104e4 // OSAGE CAPITAL LETTER KA
      case 0x104bd => 0x104e5 // OSAGE CAPITAL LETTER EHKA
      case 0x104be => 0x104e6 // OSAGE CAPITAL LETTER KYA
      case 0x104bf => 0x104e7 // OSAGE CAPITAL LETTER LA
      case 0x104c0 => 0x104e8 // OSAGE CAPITAL LETTER MA
      case 0x104c1 => 0x104e9 // OSAGE CAPITAL LETTER NA
      case 0x104c2 => 0x104ea // OSAGE CAPITAL LETTER O
      case 0x104c3 => 0x104eb // OSAGE CAPITAL LETTER OIN
      case 0x104c4 => 0x104ec // OSAGE CAPITAL LETTER PA
      case 0x104c5 => 0x104ed // OSAGE CAPITAL LETTER EHPA
      case 0x104c6 => 0x104ee // OSAGE CAPITAL LETTER SA
      case 0x104c7 => 0x104ef // OSAGE CAPITAL LETTER SHA
      case 0x104c8 => 0x104f0 // OSAGE CAPITAL LETTER TA
      case 0x104c9 => 0x104f1 // OSAGE CAPITAL LETTER EHTA
      case 0x104ca => 0x104f2 // OSAGE CAPITAL LETTER TSA
      case 0x104cb => 0x104f3 // OSAGE CAPITAL LETTER EHTSA
      case 0x104cc => 0x104f4 // OSAGE CAPITAL LETTER TSHA
      case 0x104cd => 0x104f5 // OSAGE CAPITAL LETTER DHA
      case 0x104ce => 0x104f6 // OSAGE CAPITAL LETTER U
      case 0x104cf => 0x104f7 // OSAGE CAPITAL LETTER WA
      case 0x104d0 => 0x104f8 // OSAGE CAPITAL LETTER KHA
      case 0x104d1 => 0x104f9 // OSAGE CAPITAL LETTER GHA
      case 0x104d2 => 0x104fa // OSAGE CAPITAL LETTER ZA
      case 0x104d3 => 0x104fb // OSAGE CAPITAL LETTER ZHA
      case 0x10570 => 0x10597 // VITHKUQI CAPITAL LETTER A
      case 0x10571 => 0x10598 // VITHKUQI CAPITAL LETTER BBE
      case 0x10572 => 0x10599 // VITHKUQI CAPITAL LETTER BE
      case 0x10573 => 0x1059a // VITHKUQI CAPITAL LETTER CE
      case 0x10574 => 0x1059b // VITHKUQI CAPITAL LETTER CHE
      case 0x10575 => 0x1059c // VITHKUQI CAPITAL LETTER DE
      case 0x10576 => 0x1059d // VITHKUQI CAPITAL LETTER DHE
      case 0x10577 => 0x1059e // VITHKUQI CAPITAL LETTER EI
      case 0x10578 => 0x1059f // VITHKUQI CAPITAL LETTER E
      case 0x10579 => 0x105a0 // VITHKUQI CAPITAL LETTER FE
      case 0x1057a => 0x105a1 // VITHKUQI CAPITAL LETTER GA
      case 0x1057c => 0x105a3 // VITHKUQI CAPITAL LETTER HA
      case 0x1057d => 0x105a4 // VITHKUQI CAPITAL LETTER HHA
      case 0x1057e => 0x105a5 // VITHKUQI CAPITAL LETTER I
      case 0x1057f => 0x105a6 // VITHKUQI CAPITAL LETTER IJE
      case 0x10580 => 0x105a7 // VITHKUQI CAPITAL LETTER JE
      case 0x10581 => 0x105a8 // VITHKUQI CAPITAL LETTER KA
      case 0x10582 => 0x105a9 // VITHKUQI CAPITAL LETTER LA
      case 0x10583 => 0x105aa // VITHKUQI CAPITAL LETTER LLA
      case 0x10584 => 0x105ab // VITHKUQI CAPITAL LETTER ME
      case 0x10585 => 0x105ac // VITHKUQI CAPITAL LETTER NE
      case 0x10586 => 0x105ad // VITHKUQI CAPITAL LETTER NJE
      case 0x10587 => 0x105ae // VITHKUQI CAPITAL LETTER O
      case 0x10588 => 0x105af // VITHKUQI CAPITAL LETTER PE
      case 0x10589 => 0x105b0 // VITHKUQI CAPITAL LETTER QA
      case 0x1058a => 0x105b1 // VITHKUQI CAPITAL LETTER RE
      case 0x1058c => 0x105b3 // VITHKUQI CAPITAL LETTER SE
      case 0x1058d => 0x105b4 // VITHKUQI CAPITAL LETTER SHE
      case 0x1058e => 0x105b5 // VITHKUQI CAPITAL LETTER TE
      case 0x1058f => 0x105b6 // VITHKUQI CAPITAL LETTER THE
      case 0x10590 => 0x105b7 // VITHKUQI CAPITAL LETTER U
      case 0x10591 => 0x105b8 // VITHKUQI CAPITAL LETTER VE
      case 0x10592 => 0x105b9 // VITHKUQI CAPITAL LETTER XE
      case 0x10594 => 0x105bb // VITHKUQI CAPITAL LETTER Y
      case 0x10595 => 0x105bc // VITHKUQI CAPITAL LETTER ZE
      case 0x10c80 => 0x10cc0 // OLD HUNGARIAN CAPITAL LETTER A
      case 0x10c81 => 0x10cc1 // OLD HUNGARIAN CAPITAL LETTER AA
      case 0x10c82 => 0x10cc2 // OLD HUNGARIAN CAPITAL LETTER EB
      case 0x10c83 => 0x10cc3 // OLD HUNGARIAN CAPITAL LETTER AMB
      case 0x10c84 => 0x10cc4 // OLD HUNGARIAN CAPITAL LETTER EC
      case 0x10c85 => 0x10cc5 // OLD HUNGARIAN CAPITAL LETTER ENC
      case 0x10c86 => 0x10cc6 // OLD HUNGARIAN CAPITAL LETTER ECS
      case 0x10c87 => 0x10cc7 // OLD HUNGARIAN CAPITAL LETTER ED
      case 0x10c88 => 0x10cc8 // OLD HUNGARIAN CAPITAL LETTER AND
      case 0x10c89 => 0x10cc9 // OLD HUNGARIAN CAPITAL LETTER E
      case 0x10c8a => 0x10cca // OLD HUNGARIAN CAPITAL LETTER CLOSE E
      case 0x10c8b => 0x10ccb // OLD HUNGARIAN CAPITAL LETTER EE
      case 0x10c8c => 0x10ccc // OLD HUNGARIAN CAPITAL LETTER EF
      case 0x10c8d => 0x10ccd // OLD HUNGARIAN CAPITAL LETTER EG
      case 0x10c8e => 0x10cce // OLD HUNGARIAN CAPITAL LETTER EGY
      case 0x10c8f => 0x10ccf // OLD HUNGARIAN CAPITAL LETTER EH
      case 0x10c90 => 0x10cd0 // OLD HUNGARIAN CAPITAL LETTER I
      case 0x10c91 => 0x10cd1 // OLD HUNGARIAN CAPITAL LETTER II
      case 0x10c92 => 0x10cd2 // OLD HUNGARIAN CAPITAL LETTER EJ
      case 0x10c93 => 0x10cd3 // OLD HUNGARIAN CAPITAL LETTER EK
      case 0x10c94 => 0x10cd4 // OLD HUNGARIAN CAPITAL LETTER AK
      case 0x10c95 => 0x10cd5 // OLD HUNGARIAN CAPITAL LETTER UNK
      case 0x10c96 => 0x10cd6 // OLD HUNGARIAN CAPITAL LETTER EL
      case 0x10c97 => 0x10cd7 // OLD HUNGARIAN CAPITAL LETTER ELY
      case 0x10c98 => 0x10cd8 // OLD HUNGARIAN CAPITAL LETTER EM
      case 0x10c99 => 0x10cd9 // OLD HUNGARIAN CAPITAL LETTER EN
      case 0x10c9a => 0x10cda // OLD HUNGARIAN CAPITAL LETTER ENY
      case 0x10c9b => 0x10cdb // OLD HUNGARIAN CAPITAL LETTER O
      case 0x10c9c => 0x10cdc // OLD HUNGARIAN CAPITAL LETTER OO
      case 0x10c9d => 0x10cdd // OLD HUNGARIAN CAPITAL LETTER NIKOLSBURG OE
      case 0x10c9e => 0x10cde // OLD HUNGARIAN CAPITAL LETTER RUDIMENTA OE
      case 0x10c9f => 0x10cdf // OLD HUNGARIAN CAPITAL LETTER OEE
      case 0x10ca0 => 0x10ce0 // OLD HUNGARIAN CAPITAL LETTER EP
      case 0x10ca1 => 0x10ce1 // OLD HUNGARIAN CAPITAL LETTER EMP
      case 0x10ca2 => 0x10ce2 // OLD HUNGARIAN CAPITAL LETTER ER
      case 0x10ca3 => 0x10ce3 // OLD HUNGARIAN CAPITAL LETTER SHORT ER
      case 0x10ca4 => 0x10ce4 // OLD HUNGARIAN CAPITAL LETTER ES
      case 0x10ca5 => 0x10ce5 // OLD HUNGARIAN CAPITAL LETTER ESZ
      case 0x10ca6 => 0x10ce6 // OLD HUNGARIAN CAPITAL LETTER ET
      case 0x10ca7 => 0x10ce7 // OLD HUNGARIAN CAPITAL LETTER ENT
      case 0x10ca8 => 0x10ce8 // OLD HUNGARIAN CAPITAL LETTER ETY
      case 0x10ca9 => 0x10ce9 // OLD HUNGARIAN CAPITAL LETTER ECH
      case 0x10caa => 0x10cea // OLD HUNGARIAN CAPITAL LETTER U
      case 0x10cab => 0x10ceb // OLD HUNGARIAN CAPITAL LETTER UU
      case 0x10cac => 0x10cec // OLD HUNGARIAN CAPITAL LETTER NIKOLSBURG UE
      case 0x10cad => 0x10ced // OLD HUNGARIAN CAPITAL LETTER RUDIMENTA UE
      case 0x10cae => 0x10cee // OLD HUNGARIAN CAPITAL LETTER EV
      case 0x10caf => 0x10cef // OLD HUNGARIAN CAPITAL LETTER EZ
      case 0x10cb0 => 0x10cf0 // OLD HUNGARIAN CAPITAL LETTER EZS
      case 0x10cb1 => 0x10cf1 // OLD HUNGARIAN CAPITAL LETTER ENT-SHAPED SIGN
      case 0x10cb2 => 0x10cf2 // OLD HUNGARIAN CAPITAL LETTER US
      case 0x118a0 => 0x118c0 // WARANG CITI CAPITAL LETTER NGAA
      case 0x118a1 => 0x118c1 // WARANG CITI CAPITAL LETTER A
      case 0x118a2 => 0x118c2 // WARANG CITI CAPITAL LETTER WI
      case 0x118a3 => 0x118c3 // WARANG CITI CAPITAL LETTER YU
      case 0x118a4 => 0x118c4 // WARANG CITI CAPITAL LETTER YA
      case 0x118a5 => 0x118c5 // WARANG CITI CAPITAL LETTER YO
      case 0x118a6 => 0x118c6 // WARANG CITI CAPITAL LETTER II
      case 0x118a7 => 0x118c7 // WARANG CITI CAPITAL LETTER UU
      case 0x118a8 => 0x118c8 // WARANG CITI CAPITAL LETTER E
      case 0x118a9 => 0x118c9 // WARANG CITI CAPITAL LETTER O
      case 0x118aa => 0x118ca // WARANG CITI CAPITAL LETTER ANG
      case 0x118ab => 0x118cb // WARANG CITI CAPITAL LETTER GA
      case 0x118ac => 0x118cc // WARANG CITI CAPITAL LETTER KO
      case 0x118ad => 0x118cd // WARANG CITI CAPITAL LETTER ENY
      case 0x118ae => 0x118ce // WARANG CITI CAPITAL LETTER YUJ
      case 0x118af => 0x118cf // WARANG CITI CAPITAL LETTER UC
      case 0x118b0 => 0x118d0 // WARANG CITI CAPITAL LETTER ENN
      case 0x118b1 => 0x118d1 // WARANG CITI CAPITAL LETTER ODD
      case 0x118b2 => 0x118d2 // WARANG CITI CAPITAL LETTER TTE
      case 0x118b3 => 0x118d3 // WARANG CITI CAPITAL LETTER NUNG
      case 0x118b4 => 0x118d4 // WARANG CITI CAPITAL LETTER DA
      case 0x118b5 => 0x118d5 // WARANG CITI CAPITAL LETTER AT
      case 0x118b6 => 0x118d6 // WARANG CITI CAPITAL LETTER AM
      case 0x118b7 => 0x118d7 // WARANG CITI CAPITAL LETTER BU
      case 0x118b8 => 0x118d8 // WARANG CITI CAPITAL LETTER PU
      case 0x118b9 => 0x118d9 // WARANG CITI CAPITAL LETTER HIYO
      case 0x118ba => 0x118da // WARANG CITI CAPITAL LETTER HOLO
      case 0x118bb => 0x118db // WARANG CITI CAPITAL LETTER HORR
      case 0x118bc => 0x118dc // WARANG CITI CAPITAL LETTER HAR
      case 0x118bd => 0x118dd // WARANG CITI CAPITAL LETTER SSUU
      case 0x118be => 0x118de // WARANG CITI CAPITAL LETTER SII
      case 0x118bf => 0x118df // WARANG CITI CAPITAL LETTER VIYO
      case 0x16e40 => 0x16e60 // MEDEFAIDRIN CAPITAL LETTER M
      case 0x16e41 => 0x16e61 // MEDEFAIDRIN CAPITAL LETTER S
      case 0x16e42 => 0x16e62 // MEDEFAIDRIN CAPITAL LETTER V
      case 0x16e43 => 0x16e63 // MEDEFAIDRIN CAPITAL LETTER W
      case 0x16e44 => 0x16e64 // MEDEFAIDRIN CAPITAL LETTER ATIU
      case 0x16e45 => 0x16e65 // MEDEFAIDRIN CAPITAL LETTER Z
      case 0x16e46 => 0x16e66 // MEDEFAIDRIN CAPITAL LETTER KP
      case 0x16e47 => 0x16e67 // MEDEFAIDRIN CAPITAL LETTER P
      case 0x16e48 => 0x16e68 // MEDEFAIDRIN CAPITAL LETTER T
      case 0x16e49 => 0x16e69 // MEDEFAIDRIN CAPITAL LETTER G
      case 0x16e4a => 0x16e6a // MEDEFAIDRIN CAPITAL LETTER F
      case 0x16e4b => 0x16e6b // MEDEFAIDRIN CAPITAL LETTER I
      case 0x16e4c => 0x16e6c // MEDEFAIDRIN CAPITAL LETTER K
      case 0x16e4d => 0x16e6d // MEDEFAIDRIN CAPITAL LETTER A
      case 0x16e4e => 0x16e6e // MEDEFAIDRIN CAPITAL LETTER J
      case 0x16e4f => 0x16e6f // MEDEFAIDRIN CAPITAL LETTER E
      case 0x16e50 => 0x16e70 // MEDEFAIDRIN CAPITAL LETTER B
      case 0x16e51 => 0x16e71 // MEDEFAIDRIN CAPITAL LETTER C
      case 0x16e52 => 0x16e72 // MEDEFAIDRIN CAPITAL LETTER U
      case 0x16e53 => 0x16e73 // MEDEFAIDRIN CAPITAL LETTER YU
      case 0x16e54 => 0x16e74 // MEDEFAIDRIN CAPITAL LETTER L
      case 0x16e55 => 0x16e75 // MEDEFAIDRIN CAPITAL LETTER Q
      case 0x16e56 => 0x16e76 // MEDEFAIDRIN CAPITAL LETTER HP
      case 0x16e57 => 0x16e77 // MEDEFAIDRIN CAPITAL LETTER NY
      case 0x16e58 => 0x16e78 // MEDEFAIDRIN CAPITAL LETTER X
      case 0x16e59 => 0x16e79 // MEDEFAIDRIN CAPITAL LETTER D
      case 0x16e5a => 0x16e7a // MEDEFAIDRIN CAPITAL LETTER OE
      case 0x16e5b => 0x16e7b // MEDEFAIDRIN CAPITAL LETTER N
      case 0x16e5c => 0x16e7c // MEDEFAIDRIN CAPITAL LETTER R
      case 0x16e5d => 0x16e7d // MEDEFAIDRIN CAPITAL LETTER O
      case 0x16e5e => 0x16e7e // MEDEFAIDRIN CAPITAL LETTER AI
      case 0x16e5f => 0x16e7f // MEDEFAIDRIN CAPITAL LETTER Y
      case 0x1e900 => 0x1e922 // ADLAM CAPITAL LETTER ALIF
      case 0x1e901 => 0x1e923 // ADLAM CAPITAL LETTER DAALI
      case 0x1e902 => 0x1e924 // ADLAM CAPITAL LETTER LAAM
      case 0x1e903 => 0x1e925 // ADLAM CAPITAL LETTER MIIM
      case 0x1e904 => 0x1e926 // ADLAM CAPITAL LETTER BA
      case 0x1e905 => 0x1e927 // ADLAM CAPITAL LETTER SINNYIIYHE
      case 0x1e906 => 0x1e928 // ADLAM CAPITAL LETTER PE
      case 0x1e907 => 0x1e929 // ADLAM CAPITAL LETTER BHE
      case 0x1e908 => 0x1e92a // ADLAM CAPITAL LETTER RA
      case 0x1e909 => 0x1e92b // ADLAM CAPITAL LETTER E
      case 0x1e90a => 0x1e92c // ADLAM CAPITAL LETTER FA
      case 0x1e90b => 0x1e92d // ADLAM CAPITAL LETTER I
      case 0x1e90c => 0x1e92e // ADLAM CAPITAL LETTER O
      case 0x1e90d => 0x1e92f // ADLAM CAPITAL LETTER DHA
      case 0x1e90e => 0x1e930 // ADLAM CAPITAL LETTER YHE
      case 0x1e90f => 0x1e931 // ADLAM CAPITAL LETTER WAW
      case 0x1e910 => 0x1e932 // ADLAM CAPITAL LETTER NUN
      case 0x1e911 => 0x1e933 // ADLAM CAPITAL LETTER KAF
      case 0x1e912 => 0x1e934 // ADLAM CAPITAL LETTER YA
      case 0x1e913 => 0x1e935 // ADLAM CAPITAL LETTER U
      case 0x1e914 => 0x1e936 // ADLAM CAPITAL LETTER JIIM
      case 0x1e915 => 0x1e937 // ADLAM CAPITAL LETTER CHI
      case 0x1e916 => 0x1e938 // ADLAM CAPITAL LETTER HA
      case 0x1e917 => 0x1e939 // ADLAM CAPITAL LETTER QAAF
      case 0x1e918 => 0x1e93a // ADLAM CAPITAL LETTER GA
      case 0x1e919 => 0x1e93b // ADLAM CAPITAL LETTER NYA
      case 0x1e91a => 0x1e93c // ADLAM CAPITAL LETTER TU
      case 0x1e91b => 0x1e93d // ADLAM CAPITAL LETTER NHA
      case 0x1e91c => 0x1e93e // ADLAM CAPITAL LETTER VA
      case 0x1e91d => 0x1e93f // ADLAM CAPITAL LETTER KHA
      case 0x1e91e => 0x1e940 // ADLAM CAPITAL LETTER GBE
      case 0x1e91f => 0x1e941 // ADLAM CAPITAL LETTER ZAL
      case 0x1e920 => 0x1e942 // ADLAM CAPITAL LETTER KPO
      case 0x1e921 => 0x1e943 // ADLAM CAPITAL LETTER SHA
      case _ => codePoint // All others map to themselves
    }
}
