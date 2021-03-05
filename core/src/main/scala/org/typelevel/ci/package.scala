/*
 * Copyright 2020 Typelevel
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel

package object ci {
  final implicit class CIStringSyntax(sc: StringContext) {

    /** Provides a `ci` interpolator, similar to the `s` interpolator. */
    def ci(args: Any*): CIString = CIString(sc.s(args: _*))

    object ci {

      /** A globbing CIString matcher, similar to the `s` matcher. */
      def unapplySeq(ci: CIString): Option[Seq[CIString]] = glob(sc.parts, ci)
    }
  }

  // Adapted from scala.StringContext.glob.
  // Originally inspired by https://research.swtch.com/glob
  private def glob(patternChunks: Seq[String], input: CIString): Option[Seq[CIString]] = {
    var patternIndex = 0
    var inputIndex = 0
    var nextPatternIndex = 0
    var nextInputIndex = 0

    val numWildcards = patternChunks.length - 1
    val matchStarts = Array.fill(numWildcards)(-1)
    val matchEnds = Array.fill(numWildcards)(-1)

    val nameLength = input.length
    // The final pattern is as long as all the chunks, separated by 1-character
    // glob-wildcard placeholders
    val patternLength = {
      var n = numWildcards
      for (chunk <- patternChunks)
        n += chunk.length
      n
    }

    // Convert the input pattern chunks into a single sequence of shorts; each
    // non-negative short represents a character, while -1 represents a glob wildcard
    val pattern = {
      val arr = new Array[Short](patternLength)
      var i = 0
      var first = true
      for (chunk <- patternChunks) {
        if (first) first = false
        else {
          arr(i) = -1
          i += 1
        }
        for (c <- chunk) {
          arr(i) = c.toShort
          i += 1
        }
      }
      arr
    }

    // Lookup table for each character in the pattern to check whether or not
    // it refers to a glob wildcard; a non-negative integer indicates which
    // glob wildcard it represents, while -1 means it doesn't represent any
    val matchIndices = {
      val arr = Array.fill(patternLength + 1)(-1)
      var i = 0
      var j = 0
      for (chunk <- patternChunks)
        if (j < numWildcards) {
          i += chunk.length
          arr(i) = j
          i += 1
          j += 1
        }
      arr
    }

    while (patternIndex < patternLength || inputIndex < nameLength) {
      matchIndices(patternIndex) match {
        case -1 => // do nothing
        case n =>
          matchStarts(n) = matchStarts(n) match {
            case -1 => inputIndex
            case s => math.min(s, inputIndex)
          }
          matchEnds(n) = matchEnds(n) match {
            case -1 => inputIndex
            case s => math.max(s, inputIndex)
          }
      }

      val continue = if (patternIndex < patternLength) {
        val c = pattern(patternIndex)
        c match {
          case -1 => // zero-or-more-character wildcard
            // Try to match at nx. If that doesn't work out, restart at nx+1 next.
            nextPatternIndex = patternIndex
            nextInputIndex = inputIndex + 1
            patternIndex += 1
            true
          case _ => // ordinary character
            if (inputIndex < nameLength && eqCi(input.toString(inputIndex), c.toChar)) {
              patternIndex += 1
              inputIndex += 1
              true
            } else {
              false
            }
        }
      } else false

      // Mismatch. Maybe restart.
      if (!continue) {
        if (0 < nextInputIndex && nextInputIndex <= nameLength) {
          patternIndex = nextPatternIndex
          inputIndex = nextInputIndex
        } else {
          return None
        }
      }
    }

    // Matched all of pattern to all of name. Success.
    Some(
      compat.unsafeWrapArray(
        Array.tabulate(patternChunks.length - 1)(n =>
          CIString(input.toString.slice(matchStarts(n), matchEnds(n))))
      ))
  }

  private def eqCi(a: Char, b: Char) = {
    val a0 = a.toUpper
    val b0 = b.toUpper
    (a0 == b0) || (a0.toLower == b0.toLower)
  }
}
