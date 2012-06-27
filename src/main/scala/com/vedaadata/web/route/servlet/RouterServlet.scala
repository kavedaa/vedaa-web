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

trait RelativeMapping { this: RouterServlet =>   
  override def path(request: HttpServletRequest) = request.getPathInfo 
}

class RouterServlet extends HttpServlet with CommonExtractors {

  class Query(val path: List[String], val params: Params)

  object Query {
    def apply(request: HttpServletRequest) = new Query(
      PathParams(path(request)).all,
      new Params(mapAsScalaMap(request.getParameterMap).toMap.asInstanceOf[Map[String, Array[String]]]))
  }

  class Req(val query: Query, val request: HttpServletRequest, val response: HttpServletResponse)

  object Req {
    def apply(request: HttpServletRequest, response: HttpServletResponse) = new Req(
      Query(request),
      request,
      response)
  }

  def path(request: HttpServletRequest) = request.getServletPath 
  
  val routeBuilder = new ListBuffer[PartialFunction[Req, View]]

  var default: PartialFunction[Req, View] = {
    case _ =>
      new ErrorView(HttpServletResponse.SC_NOT_FOUND)
  }

  lazy val routes = routeBuilder :+ default reduceLeft { _ orElse _ }

  def route(r: PartialFunction[Req, View]) { r +=: routeBuilder }

  def default(view: View) { default = { case _ => view } }

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    val req = Req(request, response)
    println(req.query.path)
    routes(req).render(request, response)
  }

  implicit def sessionPimp(session: HttpSession) = new SessionW(session)

  object + {
    def unapply(req: Req) = Some(req, req.request)
  }

  object - {
    def unapply(req: Req) = Some(req, req.response)
  }

  object get {
    def unapply(req: Req) = if (req.request.getMethod.toUpperCase == "GET") Some(req.query) else None
  }

  object post {
    def unapply(req: Req) = if (req.request.getMethod.toUpperCase == "POST") Some(req.query) else None
  }

  object ?: {
    def unapply(query: Query) = Some(query, query.params)
  }

  object / {
    def unapply(query: Query) = query.path isEmpty
  }

  object /: {
    def unapply(query: Query) =
      if (query.path nonEmpty)
        Some(query.path.head, new Query(query.path.tail, query.params))
      else None
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
      val elems = s.split("\\.").reverse
      elems.length match {
        case 0 => None
        case 1 => Some(elems(0), "")
        case _ => Some(elems(1), elems(0))
      }
    }
  }

  object $ {
    def unapply(request: HttpServletRequest) = Some(request.getSession)
  }

  class SessionW(self: HttpSession) {
    def apply(s: String) = Option(self.getAttribute(s))
    def update(s: String, x: Any) { self.setAttribute(s, x) }
  }

  abstract class SessionData[T](name: String) {
    def init: T
    def apply(session: HttpSession) = new SessionW(session)(name) match {
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

