/*
 * Copyright 2020 Typelevel
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel.ci

import org.specs2.mutable.Specification

class TurkeySpec extends Specification {
  "passes the Turkey test" >> {
    assert("i" != "I")
    CIString("i") == CIString("I")
  }
}
