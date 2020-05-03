package com.rossabaker.ci

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Cogen

object arbitraries {
  implicit val arbitraryForComRossabakerCIString: Arbitrary[CIString] =
    Arbitrary(arbitrary[String].map(CIString(_)))

  implicit val cogenForComRossabakerCIString: Cogen[CIString] =
    Cogen[String].contramap(ci => new String(ci.toString.toArray.map(_.toLower)))
}
