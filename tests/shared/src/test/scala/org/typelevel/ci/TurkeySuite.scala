/*
 * Copyright 2020 Typelevel
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel.ci

import munit.FunSuite

class TurkeySuite extends FunSuite {
  test("passes the Turkey test") {
    assertEquals(CIString("i"), CIString("I"))
  }
}
