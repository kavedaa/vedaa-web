package com.vedaadata.web.servlet

import com.vedaadata.web._

abstract class Servicer extends Completer[ServletCycle]

case class Redirect(url: String) extends Servicer {
  def complete(implicit cycle: ServletCycle) = cycle.response.r.sendRedirect(url)
}