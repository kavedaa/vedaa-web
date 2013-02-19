package com.vedaadata.web.servlet

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.collection.mutable.ListBuffer
import com.vedaadata.web._

class RouterServlet extends HttpServlet {

  private val routeBuilder = new ListBuffer[PartialFunction[(HttpServletRequest, HttpServletResponse), Servicer]]

  private var default: PartialFunction[(HttpServletRequest, HttpServletResponse), Servicer] = {
    case _ => Error(HttpServletResponse.SC_NOT_FOUND)
  }

  /**
   * Defines any number of routes, usually in the form of `case` statements.
   * An invocation (or several) of this method should be the main part of your servlet (in the constructor).
   */
  def route(r: PartialFunction[(HttpServletRequest, HttpServletResponse), Servicer]) { r +=: routeBuilder }

  /**
   * Defines a default servicer that gets used if none of those defined in `route` matches.
   * (You can of course also define that as the last case in `route`, but
   * using this method makes it more explicit.)
   */
  def default(servicer: Servicer) { default = { case _ => servicer } }

  private lazy val routes = routeBuilder :+ default reduceLeft (_ orElse _)

  /**
   * Defines the default encoding for processing action requests as "UTF-8".
   * Override this method to use a different encoding.
   */
  def encoding = "UTF-8"

  override protected def service(request: HttpServletRequest, response: HttpServletResponse) {
    request setCharacterEncoding encoding
    val cycle = new ServletCycle(new HttpServletRequestProxy(request), new HttpServletResponseProxy(response))
    routes(request, response) complete cycle
  }

  protected class Method(name: String) {
    def unapply(rr: (HttpServletRequest, HttpServletResponse)) =
      if (rr._1.getMethod.toUpperCase == name) Some(rr._1, rr._2) else None
  }

  /**
   * Matches a GET request and extracts a (request, response) pair.
   */
  object get extends Method("GET")

  /**
   * Matches a POST request and extracts a (request, response) pair.
   */
  object post extends Method("POST")

  /**
   * Matches a PUT request and extracts a (request, response) pair.
   */
  object put extends Method("PUT")

  /**
   * Matches a DELETE request and extracts a (request, response) pair.
   */
  object delete extends Method("DELETE")

  private[servlet] def path(request: HttpServletRequest) = request.getServletPath

  
}

trait RelativeMapping { this: RouterServlet =>
  override private[servlet] def path(request: HttpServletRequest) = request.getPathInfo
}
