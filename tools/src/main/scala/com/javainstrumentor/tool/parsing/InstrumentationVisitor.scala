package com.javainstrumentor.tool.parsing

import java.util

import com.javainstrumentor.clientlib.Instrumentor
import com.javainstrumentor.tool.parsing.scopetable.ScopeTableItem
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jdt.core.dom._

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.Try

class InstrumentationVisitor(compilationUnit: CompilationUnit, filePath: String) extends ASTVisitor with LazyLogging {

  private val _scopeTable: mutable.Map[String, ScopeTableItem] = mutable.Map.empty

  addClientLibraryImport()

  def scopeTable: Map[String, ScopeTableItem] = _scopeTable.toMap

  def addClientLibraryImport(): Unit = {
    val ast = compilationUnit.getAST
    val importDeclaration = ast.newImportDeclaration()
    importDeclaration.setName(ast.newName(Instrumentor.getClass.getCanonicalName.stripSuffix("$")))
    compilationUnit
      .imports()
      .asInstanceOf[util.List[ImportDeclaration]]
      .add(importDeclaration)
  }

  override def visit(node: MethodDeclaration): Boolean = {
    // Add entry to local scope table
    logger.info("visit(node: MethodDeclaration): key = " + node.resolveBinding().getKey)

    //    val scope = ScopeTableItem(
    //      UUID.randomUUID(),
    //      node.getName.getIdentifier,
    //      getLineNumber(node),
    //      ScopeTableItemKind.Method,
    //      Try(node.resolveBinding().getDeclaringClass.getQualifiedName).toOption,
    //      filePath
    //    )

    val ast = node.getAST

    val methodParams = node.parameters().asInstanceOf[util.List[SingleVariableDeclaration]].asScala
    logger.debug("params: " + methodParams)

    val scope = ScopeTableItem(
      node.resolveBinding().getKey,
      node.getName.getIdentifier,
      node.getReturnType2.resolveBinding().getQualifiedName,
      Some(methodParams.map(_.toString).toList),
      Try(node.resolveBinding().getDeclaringClass.getQualifiedName).toOption,
      filePath,
      getLineNumber(node)
    )
    _scopeTable(scope.id) = scope

    methodParams.foreach(param => {
      val scopeTableItem = ScopeTableItem(
        param.resolveBinding().getKey,
        param.getName.getIdentifier,
        param.getType.resolveBinding().getQualifiedName,
        None,
        None,
        filePath,
        getLineNumber(node)
      )
      _scopeTable(scopeTableItem.id) = scopeTableItem
    })

    methodParams.reverse.foreach(param => {
      val methodInstrumentation = ast.newMethodInvocation()

      methodInstrumentation.setExpression(ast.newName(Instrumentor.getClass.getSimpleName.stripSuffix("$")))
      methodInstrumentation.setName(ast.newSimpleName("log"))

      val args = List(
        newStringLiteral(ast, param.resolveBinding().getKey),
        ast.newName(param.getName.getIdentifier)
      )
      methodInstrumentation.arguments().asInstanceOf[util.List[Expression]].addAll(args.asJava)

      node.getBody.statements().asInstanceOf[util.List[Statement]].add(0, ast.newExpressionStatement(methodInstrumentation))
    })

    super.visit(node)
  }

  override def visit(node: VariableDeclarationStatement): Boolean = {
    logger.debug("visit(node: VariableDeclarationStatement) = " + node)

    node.getParent match {
      case block: Block =>

        val ast = block.getAST

        val statements = block.statements().asInstanceOf[util.List[Statement]]
        logger.trace("statements: " + statements)

        node.fragments().asInstanceOf[util.List[VariableDeclarationFragment]].asScala.foreach { f =>

          logger.info("visit(node: VariableDeclarationStatement): key = " + f.resolveBinding().getKey)

          //          val scope = ScopeTableItem(
          //            UUID.randomUUID(),
          //            f.getName.getIdentifier,
          //            getLineNumber(f),
          //            ScopeTableItemKind.Assignment,
          //            None,
          //            filePath
          //          )

          val scope = ScopeTableItem(
            f.resolveBinding().getKey,
            f.getName.getIdentifier,
            f.resolveBinding().getType.getQualifiedName,
            None,
            Try(f.resolveBinding().getDeclaringClass.getQualifiedName).toOption,
            filePath,
            getLineNumber(f)
          )
          _scopeTable(scope.id) = scope

        }
    }

    super.visit(node)
  }

  def getLineNumber(node: ASTNode): Int = compilationUnit.getLineNumber(node.getStartPosition)

  override def visit(node: Assignment): Boolean = {
    logger.debug("visit(node: Assignment) = " + node.getParent.getParent)

    logger.debug("node.getLeftHandSide.getNodeType: " + node.getLeftHandSide.getNodeType)

    logger.info("visit(node: Assignment): key = " + node.getLeftHandSide.asInstanceOf[Name].resolveBinding().getKey)

    //    val lhsName = node.getLeftHandSide match {
    //      case qualifiedName: QualifiedName => qualifiedName.toString
    //      case simpleName: SimpleName => simpleName.getIdentifier
    //    }

    node.getParent.getParent match {
      case block: Block =>

        val statements = block.statements().asInstanceOf[util.List[Statement]]
        logger.trace("statements: " + statements)

        val ast = block.getAST
        val methodInstrumentation = ast.newMethodInvocation()

        methodInstrumentation.setExpression(ast.newName(Instrumentor.getClass.getSimpleName.stripSuffix("$")))
        methodInstrumentation.setName(ast.newSimpleName("log"))
        val args = List[Expression](
          newStringLiteral(ast, node.getLeftHandSide.asInstanceOf[Name].resolveBinding().getKey),
          ast.newName(node.getLeftHandSide.asInstanceOf[Name].getFullyQualifiedName)
        )

        methodInstrumentation.arguments().asInstanceOf[util.List[Expression]].addAll(args.asJava)

        val insertionIndex = statements.indexOf(node.getParent) + 1
        statements.add(insertionIndex, ast.newExpressionStatement(methodInstrumentation))
    }
    super.visit(node)
  }

  def newStringLiteral(ast: AST, literalValue: String): StringLiteral = {
    val literal = ast.newStringLiteral()
    literal.setLiteralValue(literalValue)
    literal
  }

}
