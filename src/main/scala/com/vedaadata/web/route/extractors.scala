package com.vedaadata.web.route

trait CommonExtractors {
  
  object & {
    def unapply[T](x: T) = Some(x, x)
  }

  object int {
    def unapply(s: String) = try { Some(s.toInt) } catch { case _ => None }
  }
    
}