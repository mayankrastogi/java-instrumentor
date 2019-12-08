package com.javainstrumentor.tool.parsing

import com.javainstrumentor.tool.utils.IOUtils
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.{AST, ASTParser, CompilationUnit}

case class JavaProject(projectDir: String, resolveFromResources: Boolean = false) {

  lazy val astParser: ASTParser = createParser
  lazy val parsedSources: List[CompilationUnit] = parseASTs

  private def parseASTs: List[CompilationUnit] = {
    IOUtils
      .getJavaFilePaths(resolvedProjectDir)
      .map(parseJavaFile)
  }

  private def parseJavaFile(filepath: String): CompilationUnit = {
    astParser.setUnitName(IOUtils.getFileName(filepath))
    astParser.setSource(IOUtils.readFile(filepath))

    astParser.createAST(null).asInstanceOf[CompilationUnit]
  }

  private def createParser: ASTParser = {
    val sources = Array(resolvedProjectDir)
    val classpath = Array.empty[String]

    val parser = ASTParser.newParser(AST.JLS11)

    parser.setKind(ASTParser.K_COMPILATION_UNIT)
    parser.setResolveBindings(true)
    parser.setCompilerOptions(JavaCore.getOptions)
    parser.setEnvironment(classpath, sources, Array[String]("UTF-8"), true)

    parser
  }

  def resolvedProjectDir: String = {
    if (resolveFromResources) IOUtils.resolvePathStringToResource(projectDir) else projectDir
  }
}
