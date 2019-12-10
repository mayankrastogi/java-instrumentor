package com.javainstrumentor.tool.parsing.scopetable

import com.javainstrumentor.tool.utils.IOUtils

/**
 * Defines implicit methods that can be performed on a
 * [[com.javainstrumentor.tool.parsing.scopetable.ScopeTable.ScopeTable]] which is nothing but a type alias for a map of
 * variable IDs (`String`) to [[com.javainstrumentor.tool.parsing.scopetable.ScopeTableItem]].
 */
object ScopeTable {

  /**
   * A type alias for `Map[String, ScopeTableItem]` which represents a scope table.
   */
  type ScopeTable = Map[String, ScopeTableItem]

  /**
   * Methods that will implicitly be added to `Map[String, ScopeTableItem]` (`ScopeTable`).
   *
   * @param scopeTable The scope table map.
   */
  class ScopeTableExtensions(scopeTable: ScopeTable) {

    /**
     * Prints all the items in the scope table in a tabular format.
     *
     * @param stripFilePath The path to the project directory that will be stripped while printing file paths and IDs.
     * @return A string representing the scope table in tabular format.
     */
    def toPrettyString(stripFilePath: Option[String] = None): String = {
      // Sort the values in the scope table by their file path, and then by line number
      val items =
        scopeTable
          .values
          .toIndexedSeq
          .sortBy(item => (item.filePath, item.lineNumber))

      val format = "%30s | %6s | %25s | %30s | %60s | %30s | %50s | %100s | %s"

      val line = format.format("-" * 30, "-" * 6, "-" * 25, "-" * 30, "-" * 60, "-" * 30, "-" * 50, "-" * 100, "-" * 90)

      val header =
        Seq(
          line,
          format
            .format(
              "File Name",
              "Line #",
              "Identifier",
              "Type",
              "Method Arguments",
              "Declaring Class",
              "File Path",
              "ID",
              "Values"
            ),
          line
        )

      val rows =
        items
          .map(item =>
            format
              .format(
                item.fileName,
                item.lineNumber,
                item.name,
                item.dataType,
                item.methodArguments.map(_.mkString(", ")).getOrElse("N/A"),
                item.parentClass.getOrElse("N/A"),
                stripFilePath.map(IOUtils.stripSourceDirFromPath(item.filePath, _)).getOrElse(item.filePath),
                stripFilePath.map(IOUtils.stripSourceDirFromPath(item.id, _)).getOrElse(item.id),
                item.values.mkString(", ")
              )
          )

      val footer = Seq(line)

      (header ++ rows ++ footer).mkString("\n")
    }
  }

  implicit def scopeTableToScopeTableExtension(table: ScopeTable): ScopeTableExtensions = new ScopeTableExtensions(table)
}
