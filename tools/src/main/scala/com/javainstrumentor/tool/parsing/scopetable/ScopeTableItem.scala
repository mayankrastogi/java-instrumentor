package com.javainstrumentor.tool.parsing.scopetable

import java.util.UUID

import scala.collection.mutable.ListBuffer

case class ScopeTableItem(id: UUID, name: String, lineNumber: Int, kind: ScopeTableItemKind, parentClass: Option[String], file: String) {
  val values: ListBuffer[Any] = ListBuffer.empty
}
