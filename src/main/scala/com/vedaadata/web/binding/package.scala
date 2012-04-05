package com.vedaadata.web

import java.text.ParseException
import java.text.SimpleDateFormat

package object binding
{
  def parseDate(dateString: String) =
    try {
      Some((new SimpleDateFormat("yyyy.MM.dd")).parse(dateString))
    }
  catch {
    case ex: ParseException => None
  }

}
