package com.vedaadata.web

import javax.servlet.http.HttpServletRequest
import scala.xml.PrettyPrinter

abstract class Completer extends CompleterUtil {
  type C <: Cycle
  def complete(implicit cycle: C)
}

trait TextCompleter extends Completer {
  val text: String
  def complete(implicit cycle: C) {
    cycle.response setContentType "text/plain; charset=utf-8"
    cycle.response.getWriter print text
  }
}

trait XmlCompleter extends Completer {

  def preamble: Option[String]
  def contentType: String

  def xml(implicit cycle: C): scala.xml.Elem

  def prettyPrint = true

  def complete(implicit cycle: C) {
    if (prettyPrint) renderPretty
    else renderPlain
  }

  private def renderPretty(implicit cycle: C) {
    val printer = new PrettyPrinter(1024, 2)
    val sb = new StringBuilder
    printer format (xml, sb)
    renderString(sb.toString)
  }

  private def renderPlain(implicit cycle: C) {
    renderString(xml.toString)
  }

  private def renderString(content: String)(implicit cycle: C) {
    cycle.response setContentType contentType
    preamble map (cycle.response.getWriter println _)
    cycle.response.getWriter print content
  }
}
