package com.vedaadata.web

trait CompleterUtil[C <: Cycle] { this: Completer[C] =>
  
  def contextify(link: String)(implicit cycle: C) =
    if (!link.startsWith("/") && !link.startsWith("http://")) s"${cycle.request.getContextPath}/$link"
    else link
  

}