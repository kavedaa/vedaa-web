package com.vedaadata.web

import java.text.SimpleDateFormat

trait XhtmlUtils {

  def checkBox(name: String, checked: Boolean = false, value: String = "1", clazz: String = "", disabled: Boolean = false) =
    (checked, disabled) match {
      case (false, false) =>
        <input type="checkbox" id={ name } class={ clazz } name={ name } value={ value }/>
      case (false, true) =>
        <input type="checkbox" id={ name } class={ clazz } name={ name } value={ value } disabled="disabled"/>
      case (true, false) =>
        <input type="checkbox" id={ name } class={ clazz } name={ name } value={ value } checked="checked"/>
      case (true, true) =>
        <input type="checkbox" id={ name } class={ clazz } name={ name } value={ value } checked="checked" disabled="disabled"/>
    }

  def radio(name: String, value: String, checked: Boolean = false, disabled: Boolean = false) =
    (checked, disabled) match {
      case (false, false) =>
        <input type="radio" name={ name } value={ value }/>
      case (false, true) =>
        <input type="radio" name={ name } value={ value } disabled="disabled"/>
      case (true, false) =>
        <input type="radio" name={ name } value={ value } checked="checked"/>
      case (true, true) =>
        <input type="radio" name={ name } value={ value } checked="checked" disabled="disabled"/>
    }

  def input(inputType: String, name: String = "", value: String = "") =
    <input type={ inputType } name={ name } value={ value }/>

  def selectOption(value: String, label: String, selected: Boolean = false) =
    if (selected)
      <option value={ value } selected="">{ label }</option>
    else
      <option value={ value }>{ label }</option>

  def textInputOption[T](name: String, itemOption: Option[T], mapper: T => String) =
    itemOption match {
      case Some(item) =>
        <input type="text" name={ name } value={ mapper(item) }/>
      case None =>
        <input type="text" name={ name }/>
    }

  def dateOptionFormat(dateOption: Option[java.util.Date])(implicit df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")): String =
    dateOption match {
      case Some(date) => dateFormat(date)(df)
      case None => ""
    }

  def dateFormat(date: java.util.Date)(implicit df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")): String =
    if (date == null) new String
    else df.format(date)

  def yearFormat(date: java.util.Date) =
    dateFormat(date)(new SimpleDateFormat("yyyy"))

  def yearOptionFormat(date: Option[java.util.Date]) = date match {
    case Some(date) => dateFormat(date)(new SimpleDateFormat("yyyy"))
    case None => ""
  }

  def blankIfZero(x: Int) =
    if (x == 0) "" else x.toString

}

object XhtmlUtils extends XhtmlUtils