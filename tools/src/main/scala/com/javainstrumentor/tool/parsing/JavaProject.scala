package com.javainstrumentor.tool.parsing

import java.nio.file.{Files, Paths}

import com.javainstrumentor.tool.utils.IOUtils
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.{AST, ASTParser, CompilationUnit}

case class JavaProject(projectDir: String, outputDir: String, resolveFromResources: Boolean = false) extends LazyLogging {

  logger.debug(s"Created new JavaProject(projectDir: $projectDir, resolveFromResources: $resolveFromResources)")

  lazy val parsedSources: Map[String, CompilationUnit] = parseASTs

  def writeInstrumentedProject(): Unit = {
    try {
      IOUtils.deleteDirectory(outputDir)
      IOUtils.createDirectory(outputDir).toEither.swap.map(e => logger.error("create directory failed", e))
      parsedSources.foreach { case (sourcePath, ast) =>
        val path = Paths.get(outputDir, sourcePath.replace(resolvedProjectDir, ""))
        Files.writeString(path, ast.toString)
      }
    }
    catch {
      case e: Exception => logger.error("Writing failed!", e)
    }
  }

  private def parseASTs: Map[String, CompilationUnit] = {
    logger.trace("Parsing ASTs...")

    IOUtils
      .getJavaFilePaths(resolvedProjectDir)
      .map(path => path -> parseJavaFile(path))
      .toMap
  }

  private def parseJavaFile(filepath: String): CompilationUnit = {
    logger.trace(s"parseJavaFile(filepath: $filepath)")

    val astParser = createParser

    astParser.setUnitName(filepath)
    astParser.setSource(IOUtils.readFile(filepath))

    astParser.createAST(null).asInstanceOf[CompilationUnit]
  }

  private def createParser: ASTParser = {
    logger.trace("Creating parser...")

    val sources = Array(resolvedProjectDir)
    logger.debug("sources: " + sources.mkString)

    val classpath = Array.empty[String]
    logger.debug("classpath: " + classpath.mkString)

    val parser = ASTParser.newParser(AST.JLS11)

    parser.setKind(ASTParser.K_COMPILATION_UNIT)
    parser.setResolveBindings(true)
    parser.setBindingsRecovery(true)
    parser.setCompilerOptions(JavaCore.getOptions)
    parser.setEnvironment(classpath, sources, Array[String]("UTF-8"), true)

    parser
  }

  def resolvedProjectDir: String = {
    if (resolveFromResources) IOUtils.resolvePathStringToResource(projectDir) else projectDir
  }
}
