package com.javainstrumentor.tool.parsing.scopetable

import com.javainstrumentor.tool.utils.IOUtils

import scala.collection.mutable.ListBuffer
//
//case class ScopeTableItem(id: UUID, name: String, lineNumber: Int, kind: ScopeTableItemKind, parentClass: Option[String], file: String) {
//
//}

case class ScopeTableItem(id: String,
                          name: String,
                          dataType: String,
                          methodArguments: Option[List[String]],
                          parentClass: Option[String],
                          filePath: String,
                          lineNumber: Int) {

  val values: ListBuffer[Any] = ListBuffer.empty

  override def toString: String = {
    s"""ScopeTableItem(name: $name,
       |                dataType: $dataType,
       |                methodArguments: $methodArguments,
       |                parentClass: $parentClass,
       |                filePath: $fileName,
       |                lineNumber: $lineNumber)""".stripMargin
  }

  def fileName: String = IOUtils.getFileName(filePath)
}