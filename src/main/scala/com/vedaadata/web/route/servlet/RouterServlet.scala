package com.vedaadata.web.route.servlet

import com.vedaadata.web.route._
import com.vedaadata.web.binding.PathParams
import com.vedaadata.web.view.servlet._
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import scala.collection.mutable.ListBuffer
import collection.JavaConversions._

case class RenderContext(request: HttpServletRequest, response: HttpServletResponse)

trait RelativeMapping { this: RouterServlet =>
  override def path(request: HttpServletRequest) = request.getPathInfo
}

class RouterServlet extends HttpServlet with CommonExtractors {

  def path(request: HttpServletRequest) = request.getServletPath

  val routeBuilder = new ListBuffer[PartialFunction[RenderContext, View]]

  var default: PartialFunction[RenderContext, View] = {
    case _ =>
      new ErrorView(HttpServletResponse.SC_NOT_FOUND)
  }

  lazy val routes = routeBuilder :+ default reduceLeft { _ orElse _ }

  def route(r: PartialFunction[RenderContext, View]) { r +=: routeBuilder }

  def default(view: View) { default = { case _ => view } }

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    val ctx = RenderContext(request, response)
    routes(ctx) render ctx
  }

  class Method(name: String) {
    def unapply(ctx: RenderContext) =
      if (ctx.request.getMethod.toUpperCase == name) Some(ctx.request, ctx.response) else None
  }

  object get extends Method("GET")
  object post extends Method("POST")

  class Query(val path: List[String], val params: Params)

  object Query {
    def apply(request: HttpServletRequest) = new Query(
      PathParams(path(request)).all,
      new Params(mapAsScalaMap(request.getParameterMap).toMap.asInstanceOf[Map[String, Array[String]]]))
  }

  class Path(val elements: List[String])

  object Path {
    def apply(s: String) = new Path(s match {
      case null => Nil
      case p => (p split "/").toList drop 1
    })
    def unapply(request: HttpServletRequest) = Some(Path(path(request)))
  }

  object Params {
    def apply(request: HttpServletRequest) =
      new Params(mapAsScalaMap(request.getParameterMap).toMap.asInstanceOf[Map[String, Array[String]]])
    def unapply(request: HttpServletRequest) =
      Some(apply(request))
  }
  
  object Session {
    def unapply(request: HttpServletRequest) = Some(request.getSession)
  }

  object root {
    def unapply(query: Query) = query.path.isEmpty
  }

  object /: {

    def unapply(path: Path): Option[(String, Path)] =
      if (path.elements.nonEmpty)
        Some(path.elements.head, new Path(path.elements.tail))
      else None

    def unapply(request: HttpServletRequest): Option[(String, Path)] =
      unapply(Path(path(request)))
  }

  object withParams {
    def unapply(request: HttpServletRequest) = Some(request, Params(request))
  }

  object withSession {
    def unapply(request: HttpServletRequest) = Some(request, request.getSession)
  }
  
  object ?: {
    def unapply(query: Query) = Some(query, query.params)
  }

  object %: {
    def unapply(query: Query) =
      if (query.path.length == 2) Some(query.path.head, query.path.last) else None
  }

  object **: {
    def unapply(query: Query) =
      if (query.path.length > 1) Some(query.path.head, query.path.last) else None
  }

  object dot {
    def unapply(s: String) = {
      val elems = s.split("\\.").toList
      elems.length match {
        case 0 => None
        case 1 => Some(elems.last, "")
        case _ => Some(elems.init mkString ".", elems.last)
      }
    }
  }

  implicit class RichSession(val self: HttpSession) {
    def apply(s: String) = Option(self getAttribute s)
    def update(s: String, x: Any) { self setAttribute (s, x) }
  }

  abstract class SessionData[T](name: String) {
    def init: T
    def apply(session: HttpSession) = session(name) match {
      case Some(x) =>
        println("Found session data as: " + name)
        x.asInstanceOf[T]
      case None =>
        println("Initializing session data as: " + name)
        val data = init
        session setAttribute (name, data)
        data
    }
    def unapply(session: HttpSession) = Some(apply(session))
    def update(session: HttpSession, data: T) { session setAttribute (name, data) }
  }

  def ok = new StatusView(HttpServletResponse.SC_OK)
  def notFound = new ErrorView(HttpServletResponse.SC_NOT_FOUND)
  def serverError = new ErrorView(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)

}

