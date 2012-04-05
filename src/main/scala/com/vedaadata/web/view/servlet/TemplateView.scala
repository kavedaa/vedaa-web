package com.vedaadata.web.view.servlet

import com.vedaadata.template._
import com.vedaadata.web.view.ViewUtil

class TemplateView(beginning: String, end: String)(implicit manager: XhtmlTemplateManager)
extends DocTypeView with BaseXmlView {
  def docType = "<!DOCTYPE html>"
  def xml(implicit ctxPath: ContextPath) =
    manager.loadContextified(beginning, end)
}

class BinderView(beginning: String, end: String)(implicit val manager: XhtmlTemplateManager)
extends TemplateView(beginning, end) with ElementBinder with BinderDsl with ViewUtil {
  override def xml(implicit ctxPath: ContextPath) = new LinkContextifier(ctxPath.path)(bind(super.xml))
}
