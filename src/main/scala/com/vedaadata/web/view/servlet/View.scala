package com.vedaadata.web.view.servlet

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.xml.PrettyPrinter
import com.vedaadata.web.io.FileStreamer
import javax.servlet.http.HttpServlet
import java.io.File

abstract class View {

  case class ContextPath(path: String)

  object ContextPath {
    def apply(request: HttpServletRequest): ContextPath =
      ContextPath(request.getContextPath)
  }

  def render(request: HttpServletRequest, response: HttpServletResponse): Unit
  def contextify(link: String)(implicit contextPath: ContextPath) =
    if (!link.startsWith("/")) contextPath.path + "/" + link
    else link
}

abstract class ContentView extends View {
  def contentType: String
}

class ByteArrayView(ba: Array[Byte], val contentType: String) extends ContentView {
  def render(request: HttpServletRequest, response: HttpServletResponse) {
    response setContentType contentType
    val os = response.getOutputStream
    os write ba
    os.flush()
    os.close()
  }
}

class StreamView(val contentType: String, f: java.io.OutputStream => Unit) extends ContentView {
  def render(request: HttpServletRequest, response: HttpServletResponse) {
    response setContentType contentType
    val os = response.getOutputStream
    f(os)
    os.flush()
    os.close()
  }
}

class FileStreamerView(file: File, servlet: HttpServlet) extends View with FileStreamer {

  def this(filePath: String, servlet: HttpServlet) = this(new File(filePath), servlet)

  def this(fileName: String, path: String, servlet: HttpServlet) = this(new File(path, fileName), servlet)

  def contentType(fileName: String) =
    servlet.getServletContext.getMimeType(fileName.toLowerCase) match {
      case null => "application/octet-stream"
      case contentType => contentType
    }

  def render(request: HttpServletRequest, response: HttpServletResponse) {
    response setContentType (contentType(file.getName))
    println(file.getName)
    println(contentType(file.getName))
    streamOriginal(file, response)
  }
}

abstract class StringView extends ContentView {
  def renderString(response: HttpServletResponse, content: String) {
    response setContentType contentType
    response.getWriter print content
  }
}

class TextView(content: String) extends StringView {
  def contentType = "text/plain; charset=utf-8"
  def render(request: HttpServletRequest, response: HttpServletResponse) {
    renderString(response, content)
  }
}

abstract class DocTypeView extends StringView {
  def docType: String
  override def renderString(response: HttpServletResponse, content: String) {
    response setContentType contentType
    response.getWriter println docType
    response.getWriter print content
  }
}

trait BaseXmlView extends StringView {
  def contentType = "text/html; charset=utf-8"
  def prettyPrint = true

  def xml(implicit ctxPath: ContextPath): scala.xml.Elem

  def render(request: HttpServletRequest, response: HttpServletResponse) {
    if (prettyPrint) renderPretty(request, response)
    else renderPlain(request, response)
  }

  private def renderPretty(request: HttpServletRequest, response: HttpServletResponse) {
    val printer = new PrettyPrinter(1024, 2)
    val sb = new StringBuilder
    printer.format(xml(ContextPath(request)), sb)
    renderString(response, sb toString)
  }

  private def renderPlain(request: HttpServletRequest, response: HttpServletResponse) {
    renderString(response, xml(ContextPath(request)) toString)
  }

}

abstract class XmlView extends BaseXmlView {
  override def renderString(response: HttpServletResponse, content: String) {
    val writer = response.getWriter
    writer println """<?xml version="1.0" encoding="UTF-8"?>"""
    writer print content
  }
}

abstract class XhtmlView extends DocTypeView with BaseXmlView {

  def docType = "<!DOCTYPE html>"

  def title: String

  def cssFiles: List[String] = Nil

  def jsFiles: List[String] = Nil

  def body(implicit contextPath: ContextPath): scala.xml.Elem

  def xml(implicit contextPath: ContextPath) =
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title>{ title }</title>
        {
          cssFiles.reverse map { cssFile =>
            <link rel="stylesheet" type="text/css" href={ contextify(cssFile) }/>
          }
        }
        {
          jsFiles.reverse map { jsFile =>
            <script src={ contextify(jsFile) }/>
          }
        }
      </head>
      <body>
        { body }
      </body>
    </html>

}

class RedirectView(url: String) extends View {
  def render(request: HttpServletRequest, response: HttpServletResponse) =
    response.sendRedirect(url)
}

class ContextRedirectView(url: String = "") extends View {
  def render(request: HttpServletRequest, response: HttpServletResponse) =
    response.sendRedirect(contextify(url)(ContextPath(request)))
}

class StatusView(code: Int) extends View {
  def render(request: HttpServletRequest, response: HttpServletResponse) =
    response setStatus code
}

class ErrorView(code: Int, error: String = "") extends View {
  def render(request: HttpServletRequest, response: HttpServletResponse) =
    response sendError (code, error)
}

object View {

  implicit def titleAndBodyToView(titleAndBody: (String, scala.xml.Elem)) =
    new XhtmlView {
      def title = titleAndBody._1
      def body(implicit contextPath: ContextPath) = titleAndBody._2
    }

  implicit def stringToView(s: String) = new TextView(s)

  implicit def elemToView(elem: xml.Elem) = new XhtmlView {
    def title = elem \\ "h1" text
    override def cssFiles = List("style.css")
    def body(implicit ctxPath: ContextPath) = elem
  }

}

