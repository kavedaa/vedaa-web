package com.vedaadata.web.view.servlet

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.xml.PrettyPrinter
import com.vedaadata.web.io.FileStreamer
import javax.servlet.http.HttpServlet
import java.io.File
import com.vedaadata.web.view.ViewUtil
import com.vedaadata.web.route.servlet.RenderContext

abstract class View extends ViewUtil {

  def render(implicit ctx: RenderContext): Unit
  
  def contextify(link: String)(implicit ctx: RenderContext) =
    if (!link.startsWith("/") && !link.startsWith("http://")) ctx.request.getContextPath + "/" + link
    else link
}

abstract class ContentView extends View {
  def contentType: String
}

class ByteArrayView(ba: Array[Byte], val contentType: String) extends ContentView {
  def render(implicit ctx: RenderContext) {
    ctx.response setContentType contentType
    val os = ctx.response.getOutputStream
    os write ba
    os.flush()
    os.close()
  }
}

class StreamView(val contentType: String, f: java.io.OutputStream => Unit) extends ContentView {
  def render(implicit ctx: RenderContext) {
    ctx.response setContentType contentType
    val os = ctx.response.getOutputStream
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

  def render(implicit ctx: RenderContext) {
    ctx.response setContentType (contentType(file.getName))
    println(file.getName)
    println(contentType(file.getName))
    streamOriginal(file, ctx.response)
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
  def render(implicit ctx: RenderContext) {
    renderString(ctx.response, content)
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

  def xml(implicit ctx: RenderContext): scala.xml.Elem

  def render(implicit ctx: RenderContext) {
    if (prettyPrint) renderPretty
    else renderPlain
  }

  private def renderPretty(implicit ctx: RenderContext) {
    val printer = new PrettyPrinter(1024, 2)
    val sb = new StringBuilder
    printer.format(xml, sb)
    renderString(ctx.response, sb toString)
  }

  private def renderPlain(implicit ctx: RenderContext) {
    renderString(ctx.response, xml.toString)
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

  def body(implicit ctx: RenderContext): scala.xml.Elem

  def xml(implicit ctx: RenderContext) =
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
  def render(implicit ctx: RenderContext) =
    ctx.response sendRedirect url
}

class ContextRedirectView(url: String = "") extends View {
  def render(implicit ctx: RenderContext) =
    ctx.response sendRedirect(contextify(url))
}

class StatusView(code: Int) extends View {
  def render(implicit ctx: RenderContext) {
    ctx.response setContentType "text/plain"
    ctx.response setStatus code
  }
}

class ErrorView(code: Int, error: String = "") extends View {
  def render(implicit ctx: RenderContext) =
    ctx.response sendError (code, error)
}

object View {

  implicit def titleAndBodyToView(titleAndBody: (String, scala.xml.Elem)) =
    new XhtmlView {
      def title = titleAndBody._1
      def body(implicit ctx: RenderContext) = titleAndBody._2
    }

  implicit def stringToView(s: String) = new TextView(s)

  implicit def elemToView(elem: xml.Elem) = new XhtmlView {
    def title = elem \\ "h1" text
    override def cssFiles = List("style.css")
    def body(implicit ctx: RenderContext) = elem
  }
}
