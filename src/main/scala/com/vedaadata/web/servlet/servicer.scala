package com.vedaadata.web.servlet

import com.vedaadata.web._

abstract class Servicer extends Completer {
  type C = ServletCycle
}

abstract class XmlServicer extends Servicer with XmlCompleter {
  def preamble = Some("""<?xml version="1.0" encoding="UTF-8"?>""")
}

case class Xml(val contentType: String)(xml0: scala.xml.Elem) extends XmlServicer {
  def xml(implicit cycle: C) = xml0
}

case class Redirect(url: String) extends Servicer {
  def complete(implicit cycle: C) = cycle.response.raw.sendRedirect(url)
}

case class ContextualRedirect(url: String) extends Servicer {
  def complete(implicit cycle: C) =
    cycle.response.raw sendRedirect (contextify(url))
}

case class Status(code: Int) extends Servicer {
  def complete(implicit cycle: C) {
    cycle.response.raw setContentType "text/plain"
    cycle.response.raw setStatus code
  }
}

case class Error(code: Int, error: String = "") extends Servicer {
  def complete(implicit cycle: C) {
    cycle.response.raw sendError (code, error)
  }
}

