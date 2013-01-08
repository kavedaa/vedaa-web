package com.vedaadata.web.route

import scala.util.Try

trait CommonExtractors {
  
  object & {
    def unapply[T](x: T) = Some(x, x)
  }

  object int {
    def unapply(s: String) = Try(s.toInt).toOption
  }
    
}