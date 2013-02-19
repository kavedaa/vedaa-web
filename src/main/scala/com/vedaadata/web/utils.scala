package com.vedaadata.web

trait CompleterUtil { this: Completer =>
  
  def contextify(link: String)(implicit cycle: C) =
    if (!link.startsWith("/") && !link.startsWith("http://")) s"${cycle.request.getContextPath}/$link"
    else link
}

class Param(val name: String) {
  def apply(f: this.type => String) = name -> f(this)
  def unapply(params: Params) = Some(params(name))
}

abstract class DefaultParam(val name: String) {
  def default: String
  def apply(f: this.type => String) = name -> f(this)
  def unapply(params: Params) = Some(params get name getOrElse default) 
}