package com.javainstrumentor.tool.parsing

import java.util
import java.util.UUID

import com.javainstrumentor.clientlib.Instrumentor
import com.javainstrumentor.tool.parsing.scopetable.{ScopeTableItem, ScopeTableItemKind}
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jdt.core.dom._

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.Try

class InstrumentationVisitor(compilationUnit: CompilationUnit, filePath: String) extends ASTVisitor with LazyLogging {

  private val addedNodes: mutable.Set[ASTNode] = mutable.Set.empty

  private val _scopeTable: mutable.Map[UUID, ScopeTableItem] = mutable.Map.empty

  addClientLibraryImport()

  def scopeTable: Map[UUID, ScopeTableItem] = _scopeTable.toMap

  def addClientLibraryImport(): Unit = {
    val ast = compilationUnit.getAST
    val importDeclaration = ast.newImportDeclaration()
    importDeclaration.setName(ast.newName(Instrumentor.getClass.getCanonicalName.stripSuffix("$")))
    compilationUnit
      .imports()
      .asInstanceOf[util.List[ImportDeclaration]]
      .add(importDeclaration)

    addedNodes += importDeclaration
  }

  override def visit(node: MethodDeclaration): Boolean = {
    // Add entry to local scope table
    val scope = ScopeTableItem(
      UUID.randomUUID(),
      node.getName.getIdentifier,
      getLineNumber(node),
      ScopeTableItemKind.Method,
      Try(node.resolveBinding().getDeclaringClass.getQualifiedName).toOption,
      filePath
    )
    _scopeTable(scope.id) = scope

    val ast = node.getAST
    val methodInstrumentation = ast.newMethodInvocation()

    val params = node.parameters().asInstanceOf[util.List[SingleVariableDeclaration]].asScala.flatMap(p => Seq[Expression](
      newStringLiteral(ast, p.getType.resolveBinding().getQualifiedName),
      newStringLiteral(ast, p.getName.getIdentifier),
      ast.newName(p.getName.getIdentifier)
    ))
    logger.debug("params: " + params)

    methodInstrumentation.setExpression(ast.newName(Instrumentor.getClass.getSimpleName.stripSuffix("$")))
    methodInstrumentation.setName(ast.newSimpleName("log"))
    val args = List(
      ast.newNumberLiteral(getLineNumber(node).toString),
      newStringLiteral(ast, "Method"),
    ) ++ params
    logger.debug("args: " + args)

    methodInstrumentation.arguments().asInstanceOf[util.List[Expression]].addAll(args.asJava)
    node.getBody.statements().asInstanceOf[util.List[Statement]].add(0, ast.newExpressionStatement(methodInstrumentation))

    addedNodes += methodInstrumentation

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
          val scope = ScopeTableItem(
            UUID.randomUUID(),
            f.getName.getIdentifier,
            getLineNumber(f),
            ScopeTableItemKind.Assignment,
            None,
            filePath
          )
          _scopeTable(scope.id) = scope

          val methodInstrumentation = ast.newMethodInvocation()

          methodInstrumentation.setExpression(ast.newName(Instrumentor.getClass.getSimpleName.stripSuffix("$")))
          methodInstrumentation.setName(ast.newSimpleName("log"))
          val args = List[Expression](
            ast.newNumberLiteral(getLineNumber(f).toString),
            newStringLiteral(ast, "Declaration"),
            ast.newName(f.getName.getIdentifier)
          )
          logger.debug("args: " + args)

          methodInstrumentation.arguments().asInstanceOf[util.List[Expression]].addAll(args.asJava)

          val insertionIndex = statements.indexOf(node) + 1
          statements.add(insertionIndex, ast.newExpressionStatement(methodInstrumentation))
        }
    }

    super.visit(node)
  }

  def newStringLiteral(ast: AST, literalValue: String): StringLiteral = {
    val literal = ast.newStringLiteral()
    literal.setLiteralValue(literalValue)
    literal
  }

  def getLineNumber(node: ASTNode): Int = compilationUnit.getLineNumber(node.getStartPosition)

  override def visit(node: Assignment): Boolean = {
    logger.debug("visit(node: Assignment) = " + node.getParent.getParent)

    logger.debug("node.getLeftHandSide.getNodeType: " + node.getLeftHandSide.getNodeType)

    val lhsName = node.getLeftHandSide match {
      case qualifiedName: QualifiedName => qualifiedName.toString
      case simpleName: SimpleName => simpleName.getIdentifier
    }

    node.getParent.getParent match {
      case block: Block =>

        val statements = block.statements().asInstanceOf[util.List[Statement]]
        logger.trace("statements: " + statements)

        val ast = block.getAST
        val methodInstrumentation = ast.newMethodInvocation()

        methodInstrumentation.setExpression(ast.newName(Instrumentor.getClass.getSimpleName.stripSuffix("$")))
        methodInstrumentation.setName(ast.newSimpleName("log"))
        val args = List[Expression](
          ast.newNumberLiteral(getLineNumber(node).toString),
          newStringLiteral(ast, "Assignment"),
          ast.newName(node.getLeftHandSide.asInstanceOf[Name].getFullyQualifiedName)
        )
        logger.debug("args: " + args)

        methodInstrumentation.arguments().asInstanceOf[util.List[Expression]].addAll(args.asJava)

        val insertionIndex = statements.indexOf(node.getParent) + 1
        statements.add(insertionIndex, ast.newExpressionStatement(methodInstrumentation))
    }
    super.visit(node)
  }

  override def preVisit2(node: ASTNode): Boolean = !addedNodes.contains(node)

}
