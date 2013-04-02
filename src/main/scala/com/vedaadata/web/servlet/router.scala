package com.vedaadata.web.servlet

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.collection.mutable.ListBuffer
import com.vedaadata.web._
import scala.collection.JavaConversions
import javax.servlet.http.HttpSession

case class ServletCycle(request: HttpServletRequest, response: HttpServletResponse)

class RouterServlet extends HttpServlet with CommonExtractors {

  private val routeBuilder = new ListBuffer[PartialFunction[ServletCycle, Servicer]]

  private var default: PartialFunction[ServletCycle, Servicer] = {
    case _ => Error(HttpServletResponse.SC_NOT_FOUND)
  }

  /**
   * Defines any number of routes, usually in the form of `case` statements.
   * An invocation (or several) of this method should be the main part of your servlet (in the constructor).
   */
  def route(r: PartialFunction[ServletCycle, Servicer]) { r +=: routeBuilder }

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
    val cycle = new ServletCycle(request, response)
    routes(cycle) service cycle
  }

  private[servlet] def path(request: HttpServletRequest) = request.getServletPath

  protected class Method(name: String) {

    def elements(s: String) = s match {
      case null => Nil
      case p => (p split "/").toList drop 1
    }

    def unapply(cycle: ServletCycle) =
      if (cycle.request.getMethod.toUpperCase == name) Some(elements(path(cycle.request))) else None
  }

  /**
   * Matches a GET request and extract the URL path as a list of strings.
   */
  object get extends Method("GET")

  /**
   * Matches a POST request and extract the URL path as a list of strings.
   */
  object post extends Method("POST")

  /**
   * Matches a PUT request and extract the URL path as a list of strings.
   */
  object put extends Method("PUT")

  /**
   * Matches a DELETE request and extract the URL path as a list of strings.
   */
  object delete extends Method("DELETE")

  /**
   * Extracts elements from Seqs of Strings.
   */
  object /: {
    def unapply(elements: Seq[String]): Option[(String, Seq[String])] =
      if (elements.nonEmpty)
        Some(elements.head, elements.tail)
      else None
  }

  /**
   * Matches Seqs with 2 or more elements and extracts the first and the last.
   */
  object **: {
    def unapply(elements: Seq[String]): Option[(String, String)] =
      if (elements.length > 1) Some(elements.head, elements.last) else None
  }

  /**
   * Extracts the body and last "file extension" in a string,
   * such as ("my.cool.textfile", "txt") from "my.cool.textfile.txt".
   * Does not match if there are no periods (dots) in the string.
   */
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

  /**
   * Extracts a HttpServletRequest from a cycle.
   */
  object Request {
    def unapply(cycle: ServletCycle) = Some(cycle.request)
  }

  /**
   * Extracts a HttpServletResponse from a cycle.
   */
  object Response {
    def unapply(cycle: ServletCycle) = Some(cycle.response)
  }

  class Parameters private[web] (protected val self: Map[String, Seq[String]])
    extends AbstractParameters {

    def paramsCompanion = Parameters

    lazy val multi = new MultiParameters(self)
  }

  class MultiParameters private[web] (protected val self: Map[String, Seq[String]])
    extends AbstractMultiParameters {

    def paramsCompanion = Parameters
  }

  /**
   * Extracts request parameters from a cycle.
   */
  object Parameters extends ParametersCompanion {

    def fromRequest(request: HttpServletRequest) =
      new Parameters(JavaConversions mapAsScalaMap request.getParameterMap.asInstanceOf[java.util.Map[String, Array[String]]] map {
        case (k, v) => (k, v.toSeq)
      } toMap)

    def unapply(cycle: ServletCycle) =
      Some(fromRequest(cycle.request))
  }

  /**
   * Extracts a HttpSession from a cycle.
   */
  object Session {
    def unapply(cycle: ServletCycle) = Some(cycle.request.getSession)
  }

  /**
   * Extracts an instance of type T from a named HttpSession attribute.
   * The common use case is to use some mutable T that can be
   * manipulated directly without having to set the attribute again.
   *
   * A concrete object of this class must implement the `init` method.
   */
  abstract class SessionData[T](name: String, debug: Boolean = false) {
    def init: T
    def apply(session: HttpSession) = Option(session getAttribute name) match {
      case Some(x) =>
        if (debug) println("Found session data as: " + name)
        x.asInstanceOf[T]
      case None =>
        if (debug) println("Initializing session data as: " + name)
        val data = init
        session setAttribute (name, data)
        data
    }
    def unapply(session: HttpSession) = Some(apply(session))
  }

  /**
   * Convenience method for returning a simple 200 OK status code.
   */
  def ok = Status(HttpServletResponse.SC_OK)

  /**
   * Convenience method for returning a simple 404 NOT FOUND status code.
   */
  def notFound = Error(HttpServletResponse.SC_NOT_FOUND)

  /**
   * Convenience method for returning a simple SERVER ERROR status code.
   */
  def serverError = Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)

  /**
   * Convenience method for a local redirect, e.g. as in a post-redirect-get pattern.
   */
  def redirect: Servicer = ContextRedirect("")

}

trait PathInfoRouting { this: RouterServlet =>
  override private[servlet] def path(request: HttpServletRequest) = request.getPathInfo
  override def redirect = ContextServletRedirect("")
}
