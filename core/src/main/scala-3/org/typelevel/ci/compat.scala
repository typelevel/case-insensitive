/*
 * Copyright 2020 Typelevel
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.typelevel.ci

import scala.annotation.{Annotation, StaticAnnotation}

private[ci] object compat {
  class suppressUnusedImportWarningForCompat extends Annotation with StaticAnnotation
}
