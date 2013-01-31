package com.vedaadata.web

import javax.servlet.http.HttpServletRequest

abstract class Completer[C <: Cycle] {
  def complete(implicit cycle: C)  
}
