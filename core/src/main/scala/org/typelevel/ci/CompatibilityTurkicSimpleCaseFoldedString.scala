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

import java.text.Normalizer
import scala.annotation.tailrec

final case class CompatibilityTurkicSimpleCaseFoldedString private (override val toString: String)
    extends AnyVal

object CompatibilityTurkicSimpleCaseFoldedString {

  def apply(value: String): CompatibilityTurkicSimpleCaseFoldedString = {
    val nfdNormal: String =
      if (Normalizer.isNormalized(value, Normalizer.Form.NFD)) {
        value
      } else {
        Normalizer.normalize(value, Normalizer.Form.NFD)
      }

    val caseFold0: String =
      CaseFolding.turkicSimpleCaseFoldString(nfdNormal)

    val nfkdNormal0: String =
      Normalizer.normalize(caseFold0, Normalizer.Form.NFKD)

    val caseFold1: String =
      CaseFolding.turkicSimpleCaseFoldString(nfkdNormal0)

    val nfkdNormal1: String =
      Normalizer.normalize(caseFold1, Normalizer.Form.NFKD)

    new CompatibilityTurkicSimpleCaseFoldedString(nfkdNormal1)
  }

  val empty: CompatibilityTurkicSimpleCaseFoldedString =
    apply("")
}
