/*
 * Copyright 2020 Typelevel
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel

package object ci {
  final implicit class CIStringSyntax(private val sc: StringContext) extends AnyVal {

    /** Provides a `ci` interpolator, similar to the `s` interpolator. */
    def ci(args: Any*): CIString = CIString(sc.s(args: _*))
  }
}
