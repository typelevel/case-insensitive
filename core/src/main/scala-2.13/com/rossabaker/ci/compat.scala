package com.rossabaker.ci

import scala.annotation.{Annotation, StaticAnnotation}

private[ci] object compat {
  class suppressUnusedImportWarningForCompat extends Annotation with StaticAnnotation
}
