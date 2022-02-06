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

final case class CanonicalFullCaseFoldedString private (override val toString: String)
    extends AnyVal

object CanonicalFullCaseFoldedString {
  def apply(value: String): CanonicalFullCaseFoldedString =
    new CanonicalFullCaseFoldedString(
      Normalizer.normalize(
        if (Normalizer.isNormalized(value, Normalizer.Form.NFD)) {
          CaseFolding.fullCaseFoldString(value)
        } else {
          CaseFolding.fullCaseFoldString(Normalizer.normalize(value, Normalizer.Form.NFD))
        },
        Normalizer.Form.NFD
      )
    )

  val empty: CanonicalFullCaseFoldedString =
    apply("")
}
