package com.javainstrumentor.tool

import com.javainstrumentor.tool.parsing.scopetable.ScopeTableItem
import com.javainstrumentor.tool.parsing.{InstrumentationVisitor, JavaProject}
import com.typesafe.scalalogging.LazyLogging

object Launcher extends App with LazyLogging {

  val project1Path = "sample-projects/project1"
  val project1OutputPath = "code-gen/project1"

  val project1 = JavaProject(project1Path, project1OutputPath, resolveFromResources = true)

  var scopeTable: Map[String, ScopeTableItem] = Map.empty

  project1.parsedSources.foreach { case (path, ast) =>
    val visitor = new InstrumentationVisitor(ast, path)
    ast.accept(visitor)
    scopeTable ++= visitor.scopeTable
    //    println(visitor.scopeTable)
    println(ast)
  }

  println("ScopeTable: ")
  scopeTable.values.foreach(println)

  project1.writeInstrumentedProject()
}
