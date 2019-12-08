package com.javainstrumentor.tool

import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.{AST, ASTParser, CompilationUnit}

object Launcher extends App with LazyLogging {

  val filepath = "sample-projects/project1"
  val javaFiles = IOUtils.getJavaFilePathsFromResource(filepath)

  val parser = ASTParser.newParser(AST.JLS11)
  parser.setKind(ASTParser.K_COMPILATION_UNIT)
  parser.setResolveBindings(true)
  parser.setCompilerOptions(JavaCore.getOptions)
  val unitName = IOUtils.getFileName(javaFiles.head)
  parser.setUnitName(unitName)
  val sources = Array(Paths.get(IOUtils.resolveUriToResource(filepath)).toString)
  val classpath = Array.empty[String]
  parser.setEnvironment(classpath, sources, Array[String]("UTF-8"), true)
  parser.setSource(IOUtils.readFile(javaFiles.head))
  val cu = parser.createAST(null).asInstanceOf[CompilationUnit]
  println(cu)
}
