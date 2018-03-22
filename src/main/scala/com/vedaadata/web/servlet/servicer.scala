package com.vedaadata.web.servlet

import com.vedaadata.web._
import scala.xml.PrettyPrinter
import javax.servlet.http._

/**
 * Base class for all servicers. All `case` expressions in a `route` block in a RouterServlet
 * must evaluate to an instance of this class, either explicitly or implicitly.
 * There is one abstract method to implement, `service`.
 * You'll most likely not use this class directly; one of its subclasses will usually suffice.
 */
abstract class Servicer {

  def service(implicit c: ServletCycle)

  def contextify(link: String)(implicit c: ServletCycle) =
    if (!link.startsWith("/") && !link.startsWith("http://")) contextPath(link)
    else link

  def contextPath(link: String)(implicit c: ServletCycle) =
    s"${c.request.getContextPath}/$link"

  def servletPath(link: String)(implicit c: ServletCycle) =
    s"${c.request.getServletPath}/$link"

  def contextServletPath(link: String)(implicit c: ServletCycle) =
    s"${c.request.getContextPath}${c.request.getServletPath}/$link"
}

/**
 * Servicer for returning a simple text response.
 */
class TextServicer(text: String, mimeType: String = "text/plain") extends Servicer {
  def service(implicit c: ServletCycle) {
    c.response setContentType (mimeType + "; charset=utf-8")
    c.response.getWriter print text
  }
}

/**
 * Base servicer for XML and XHTML.
 */
abstract class XmlServicer extends Servicer {

  def preamble: Option[String]
  def contentType: String

  def xml(implicit c: ServletCycle): scala.xml.Elem

  def prettyPrint = true

  def service(implicit c: ServletCycle) {
    if (prettyPrint) renderPretty
    else renderPlain
  }

  private def renderPretty(implicit c: ServletCycle) {
    val printer = new PrettyPrinter(1024, 2)
    val sb = new StringBuilder
    printer format (xml, sb)
    renderString(sb.toString)
  }

  private def renderPlain(implicit c: ServletCycle) {
    renderString(xml.toString)
  }

  private def renderString(content: String)(implicit c: ServletCycle) {
    c.response setContentType contentType
    preamble map (c.response.getWriter println _)
    c.response.getWriter print content
  }

}

abstract class SimpleXml extends XmlServicer {
  def preamble = Some("""<?xml version="1.0" encoding="UTF-8"?>""")
}

/**
 * Convenient XML factories.
 */
object Xml {

  def apply(preamble0: String, contentType0: String)(xml0: scala.xml.Elem): XmlServicer =
    new XmlServicer {
      def preamble = Some(preamble0)
      def contentType = contentType0
      def xml(implicit c: ServletCycle) = xml0
    }

  def apply(contentType0: String)(xml0: scala.xml.Elem): XmlServicer =
    apply("""<?xml version="1.0" encoding="UTF-8"?>""", contentType0)(xml0)

  def apply(xml0: scala.xml.Elem): XmlServicer =
    apply("application/xml")(xml0)
}

/**
 * Convenient XHTML factories.
 */
object Xhtml {

  def apply(contentType0: String)(xml0: scala.xml.Elem): XmlServicer =
    Xml("<!DOCTYPE html>", contentType0)(xml0)

  def apply(xml0: scala.xml.Elem): XmlServicer =
    apply("text/html; charset=utf-8")(xml0)
}

/**
 * Convenience XHTML servicer with a predefined <head> element and methods for
 * including CSS and Javascript files.
 * The `title` and `body` methods must be implemented.
 */
abstract class SimpleXhtml extends XmlServicer {

  def contentType = "text/html; charset=utf-8"

  def preamble = Some("<!DOCTYPE html>")

  def title: String

  def metas: List[(String, String)] = Nil
  def links: List[(String, String, String)] = Nil

  def cssFiles: List[String] = Nil
  def cssLinks: List[String] = Nil

  def jsFiles: List[String] = Nil
  def jsLinks: List[String] = Nil

  def body(implicit c: ServletCycle): scala.xml.Elem

  def xml(implicit c: ServletCycle) =
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>{ title }</title>
        {
          metas map { case (name, content) =>
            <meta name={ name } content={ content } />
          }
        }
        {
          links map { case (rel, href, tpe) =>
            <link rel={ rel } href={ href } type={ tpe } />
          }
        }
        {
          cssLinks.reverse map { cssLink =>
            <link rel="stylesheet" type="text/css" href={ cssLink }/>
          }
        }
        {
          cssFiles.reverse map { cssFile =>
            <link rel="stylesheet" type="text/css" href={ contextify(cssFile) }/>
          }
        }
        {
          jsLinks.reverse map { jsLink =>
            <script src={ jsLink }></script>
          }
        }
        {
          jsFiles.reverse map { jsFile =>
            <script src={ contextify(jsFile) }></script>
          }
        }
      </head>
      <body>
        { body }
      </body>
    </html>
}

/**
 * Convenient SimpleXhtml factories.
 */
object SimpleXhtml {

  def apply(title0: String)(body0: scala.xml.Elem): SimpleXhtml =
    new SimpleXhtml {
      def title = title0
      def body(implicit c: ServletCycle) = body0
    }
}

case class BinaryServicer(contentType: String)(ba: Array[Byte]) extends Servicer {
  def service(implicit c: ServletCycle) {
    c.response setContentType contentType
    val os = c.response.getOutputStream
    os write ba
    os flush ()
    os close ()
  }
}

case class StreamServicer[U](contentType: String)(f: java.io.OutputStream => U) extends Servicer {
  def service(implicit c: ServletCycle) {
    c.response reset ()
    c.response setHeader ("Cache-Control", "private") // IE8 workaround    
    c.response setContentType contentType
    val os = c.response.getOutputStream
    f(os)
    os flush ()
    os close ()
  }
}

case class Redirect(url: String) extends Servicer {
  def service(implicit c: ServletCycle) = c.response sendRedirect url
}

case class ContextRedirect(url: String) extends Servicer {
  def service(implicit c: ServletCycle) =
    c.response sendRedirect contextPath(url)
}

case class ContextServletRedirect(url: String) extends Servicer {
  def service(implicit c: ServletCycle) =
    c.response sendRedirect contextServletPath(url)
}

case class Status(code: Int) extends Servicer {
  def service(implicit c: ServletCycle) {
    c.response setContentType "text/plain"
    c.response setStatus code
  }
}

case class Error(code: Int, error: String = "") extends Servicer {
  def service(implicit c: ServletCycle) {
    c.response sendError (code, error)
  }
}

case class Unauthorized(realm: String) extends Servicer {
  def service(implicit c: ServletCycle) {
    c.response setHeader ("WWW-Authenticate", "BASIC realm=\"" + realm + "\"")
    c.response sendError HttpServletResponse.SC_UNAUTHORIZED
  }
}

object Servicer {
  implicit def textServicer(s: String) = new TextServicer(s)
  implicit def xmlServicer(xml: scala.xml.Elem) = Xml(xml)
}