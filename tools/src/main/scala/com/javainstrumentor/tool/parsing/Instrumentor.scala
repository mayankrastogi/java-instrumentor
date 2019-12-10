package com.javainstrumentor.tool.parsing

import com.javainstrumentor.tool.parsing.scopetable.ScopeTableItem

class Instrumentor {

  /**
    * Instruments the given java project and returns the scope table for the project
    *
    * @param inputPath  The input path of the project to be instrumented
    * @param outputPath The output path of the project to be instrumented
    * @return ScopeTable
    */

  def instrumentAndFindScopeTable(inputPath: String, outputPath: String): Map[String, ScopeTableItem] = {

    val javaProject = JavaProject(inputPath, outputPath, resolveFromResources = true)

    var scopeTable: Map[String, ScopeTableItem] = Map.empty

    javaProject.parsedSources.foreach { case (path, ast) =>
      val visitor = new InstrumentationVisitor(ast, path)
      ast.accept(visitor)
      scopeTable ++= visitor.scopeTable
      //    println(visitor.scopeTable)
      println(ast)
    }

    println("ScopeTable: ")
    scopeTable.values.foreach(println)


    javaProject.writeInstrumentedProject()

    scopeTable
  }
}
