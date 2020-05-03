package com.rossabaker.ci

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

object arbitraries {
  implicit val arbitraryForComRossabakerCIString: Arbitrary[CIString] =
    Arbitrary(arbitrary[String].map(CIString(_)))
}
