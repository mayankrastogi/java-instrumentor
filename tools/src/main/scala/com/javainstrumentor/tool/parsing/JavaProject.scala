package com.javainstrumentor.tool.parsing

import java.nio.file.{Files, Paths}

import com.javainstrumentor.tool.parsing.scopetable.ScopeTable.ScopeTable
import com.javainstrumentor.tool.parsing.scopetable.ScopeTableItem
import com.javainstrumentor.tool.utils.IOUtils
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.{AST, ASTParser, CompilationUnit}

import scala.collection.mutable

/**
 * A single Java Project which is to be instrumented.
 *
 * The AST of each `.java` source file in the project can be extracted using the
 * [[com.javainstrumentor.tool.parsing.JavaProject#parsedSources]] property. The project can be instrumented using
 * [[com.javainstrumentor.tool.parsing.JavaProject#instrument]] and its scope table can be obtained using
 * [[com.javainstrumentor.tool.parsing.JavaProject#scopeTable]] once the project has been instrumented. Thereafter,
 * [[com.javainstrumentor.tool.parsing.JavaProject#writeInstrumentedProject]] can be used to write the instrumented
 * source files.
 *
 * @param projectDir           The path to the source directory that contains all the source `.java` files for this
 *                             project.
 * @param outputDir            The path where the instrumented code will be written.
 * @param resolveFromResources If `true`, the `projectDir` will be resolved from this project's `resources` folder.
 */
case class JavaProject(projectDir: String, outputDir: String, resolveFromResources: Boolean = false) extends LazyLogging {

  logger.debug(s"Created new JavaProject(projectDir: $projectDir, outputDir: $outputDir, resolveFromResources: $resolveFromResources)")

  /**
   * Parses the AST of all source files in the project and returns a Map whose key is the path to a source file and the
   * value is the AST of that source file. The parsing happens the first time this property is accessed.
   *
   * Once the project has been instrumented, the ASTs will get updated with the instrumentation log statements added.
   */
  lazy val parsedSources: Map[String, CompilationUnit] = parseASTs

  private var _scopeTable: Option[ScopeTable] = None

  /**
   * The scope table of all the source files of the project, populated during the instrumentation step.
   *
   * @return The scope table if the project has been instrumented, `None` if the project has not been instrumented yet.
   */
  def scopeTable: Option[ScopeTable] = _scopeTable

  /**
   * Parses the ASTs, inserts instrumentation log statements, and builds the scope table.
   *
   * @return A reference to the project itself.
   */
  def instrument: JavaProject = {
    val projectScopeTable: mutable.Map[String, ScopeTableItem] = mutable.Map.empty

    parsedSources.foreach { case (path, ast) =>
      logger.info(s"Instrumenting $path")

      val visitor = new InstrumentationVisitor(ast, path)
      ast.accept(visitor)

      logger.trace(s"Instrumented AST:\n$ast")
      logger.debug(s"Scope table for $path:\n${visitor.scopeTable}")

      projectScopeTable ++= visitor.scopeTable
    }

    _scopeTable = Some(projectScopeTable.toMap)
    this
  }

  /**
   * Writes the instrumented java source files for all the files in the project using the parsed and instrumented ASTs.
   *
   * The files are written to the output-directory as specified while creating the project. The output directory is
   * truncated before the new files are written.
   *
   * @return A reference to the project itself.
   */
  def writeInstrumentedProject(): JavaProject = {
    try {
      IOUtils.deleteDirectory(outputDir)
      IOUtils.createDirectory(outputDir).toEither.swap.map(e => logger.error("Create directory failed", e))

      parsedSources.foreach { case (sourcePath, ast) =>
        val path = Paths.get(outputDir, sourcePath.replace(resolvedProjectDir, ""))
        Files.writeString(path, ast.toString)
      }
    }
    catch {
      case e: Exception => logger.error("Writing failed!", e)
    }
    this
  }

  /**
   * Parses the ASTs of all `.java` files found in the `projectDir`.
   *
   * @return A map of of file-paths to the parsed AST of that file.
   */
  private def parseASTs: Map[String, CompilationUnit] = {
    logger.trace("Parsing ASTs...")

    IOUtils
      .getJavaFilePaths(resolvedProjectDir)
      .map(path => path -> parseJavaFile(path))
      .toMap
  }

  /**
   * Parses the AST of a single `.java` file.
   *
   * @param filepath The path to the source file.
   * @return
   */
  private def parseJavaFile(filepath: String): CompilationUnit = {
    logger.trace(s"parseJavaFile(filepath: $filepath)")

    val astParser = createParser

    astParser.setUnitName(filepath)
    astParser.setSource(IOUtils.readFile(filepath))

    astParser.createAST(null).asInstanceOf[CompilationUnit]
  }

  /**
   * Creates and initializes an AST parser for parsing a file in this project.
   *
   * @return The AST parser.
   */
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

  /**
   * Resolves the correct path to the project directory, if the project dir is within the `resources` folder.
   *
   * @return The resolved path.
   */
  def resolvedProjectDir: String = {
    if (resolveFromResources) IOUtils.resolvePathStringToResource(projectDir) else projectDir
  }
}
