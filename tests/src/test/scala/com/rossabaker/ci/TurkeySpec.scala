package com.rossabaker.ci

import org.specs2.mutable.Specification

class TurkeySpec extends Specification {
  "passes the Turkey test" >> {
    assert("i" != "I")
    CIString("i") == CIString("I")
  }
}
