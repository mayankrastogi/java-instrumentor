package com.javainstrumentor.tool.parsing

import java.util

import com.javainstrumentor.clientlib.Instrumentor
import com.javainstrumentor.tool.parsing.scopetable.ScopeTableItem
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jdt.core.dom._

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.Try

/**
 * A visitor for visiting the nodes of an Abstract Syntax Tree (AST) of a Java Program, constructing a scope table from
 * the method and variable declarations, and inserting instrumentation logging statements into the program.
 *
 * When a syntax tree `accept`s this visitor, the following tasks are performed by the visitor:
 *
 *   1. An `import` statement is inserted for importing the [[com.javainstrumentor.clientlib.Instrumentor]] client
 * library which is responsible for sending the execution log to the instrumentation (server) program.
 *   2. A `scopeTable` is populated, with a unique identifier (unique within the same scope), for each method
 * declaration (and its parameters) and variable declaration.
 *   3. An instrumentation log statement using [[com.javainstrumentor.clientlib.Instrumentor#log]] is inserted into
 * the AST after each method declaration, parameters of the method declaration, and all assignment statements.
 *
 * The generated scope table can be obtained using the
 * [[com.javainstrumentor.tool.parsing.InstrumentationVisitor#scopeTable]] property.
 *
 * @param compilationUnit The compilation unit that represents the AST of a Java source file.
 * @param filePath        The path to the `.java` source file from which the `CompilationUnit` was created.
 */
class InstrumentationVisitor(compilationUnit: CompilationUnit, filePath: String) extends ASTVisitor with LazyLogging {

  /**
   * A private mutable map for creating a Hash Table for all methods and variables discovered.
   */
  private val _scopeTable: mutable.Map[String, ScopeTableItem] = mutable.Map.empty

  // Add the import statement for the client-library as soon as the visitor object is instantiated
  addClientLibraryImport()

  /**
   * An immutable Hash Table, populated with all the methods and variables discovered while visiting the AST.
   *
   * The `key` of the table is a `String` that uniquely identifies a variable in a given scope and compilation unit. No
   * two variables with the same identifier but different scopes will have the same identifier. The `value` is a
   * [[com.javainstrumentor.tool.parsing.scopetable.ScopeTableItem]].
   *
   * @return The scope table generated after visiting all the nodes of an AST.
   */
  def scopeTable: Map[String, ScopeTableItem] = _scopeTable.toMap

  /**
   * Adds an import statement for importing the [[com.javainstrumentor.clientlib.Instrumentor]] client library which is
   * responsible for sending the execution log to the instrumentation (server) program.
   */
  def addClientLibraryImport(): Unit = {
    logger.trace("addClientLibraryImport()")

    logger.info("Adding import statement for client library...")

    val ast = compilationUnit.getAST
    val importDeclaration = ast.newImportDeclaration()
    importDeclaration.setName(ast.newName(Instrumentor.getClass.getCanonicalName.stripSuffix("$")))

    logger.debug(s"importDeclaration: $importDeclaration")

    compilationUnit
      .imports()
      .asInstanceOf[util.List[ImportDeclaration]]
      .add(importDeclaration)

    logger.info("Import statement added.")
  }

  /**
   * Creates a [[org.eclipse.jdt.core.dom.MethodInvocation]] expression that can be inserted in a block of statements
   * for logging the value of a variable using the [[com.javainstrumentor.clientlib.Instrumentor#log]] method of the
   * client library.
   *
   * @param ast        The Abstract syntax tree that will be the owner of the method invocation statement.
   * @param id         The unique identifier of the variable which can be obtained using `node.resolveBinding.getKey()`.
   * @param identifier The identifier name of the variable.
   * @return The method invocation statement for logging the value of the variable.
   */
  def createInstrumentationLogStatement(ast: AST, id: String, identifier: String): MethodInvocation = {
    logger.trace(s"createInstrumentationLogStatement(ast: $ast, id: $id, identifier: $identifier)")

    val logStatement = ast.newMethodInvocation()

    logStatement.setExpression(ast.newName(Instrumentor.getClass.getSimpleName.stripSuffix("$")))
    logStatement.setName(ast.newSimpleName("log"))

    val args = List(
      newStringLiteral(ast, id),
      ast.newName(identifier)
    )
    logger.debug(s"args: $args")

    logStatement.arguments().asInstanceOf[util.List[Expression]].addAll(args.asJava)
    logger.debug(s"logStatement: $logStatement")

    logStatement
  }

  /**
   * Creates a new [[org.eclipse.jdt.core.dom.StringLiteral]] owned by the specified AST.
   *
   * @param ast          The AST that will own the String literal.
   * @param literalValue The literal value.
   * @return The String Literal.
   */
  def newStringLiteral(ast: AST, literalValue: String): StringLiteral = {
    val literal = ast.newStringLiteral()
    literal.setLiteralValue(literalValue)
    literal
  }

  /**
   * Computes the line number of the given [[org.eclipse.jdt.core.dom.ASTNode]] inside the visitor's compilation unit.
   *
   * @param node The node.
   * @return The line number of the node.
   */
  def getLineNumber(node: ASTNode): Int = compilationUnit.getLineNumber(node.getStartPosition)

  /**
   * Visits a method declaration and adds the method, along with its parameters, to the scope table. An instrumentation
   * log statement is also inserted as the first statement of the method body to log the values of the input parameters
   * upon invocation of the method.
   *
   * @param node The method declaration node to be visited.
   * @return `true` if the children of this node should be visited, and `false` if the children of this node should be
   *         skipped.
   */
  override def visit(node: MethodDeclaration): Boolean = {
    logger.trace(s"visit(node: MethodDeclaration = $node)")

    val ast = node.getAST

    val methodParams = node.parameters().asInstanceOf[util.List[SingleVariableDeclaration]].asScala
    logger.debug(s"methodParams: $methodParams")

    logger.info(s"Adding method `${node.getName}` to scope table.")

    val scope = ScopeTableItem(
      node.resolveBinding().getKey,
      node.getName.getIdentifier,
      node.getReturnType2.resolveBinding().getQualifiedName,
      Some(methodParams.map(_.toString).toList),
      Try(node.resolveBinding().getDeclaringClass.getQualifiedName).toOption,
      filePath,
      getLineNumber(node)
    )
    logger.debug(s"scope: $scope")

    // Add entry to scope table for the current compilation unit
    _scopeTable(scope.id) = scope

    // Add each parameter of the method to the scope table and create instrumentation log statements to be inserted
    val logStatements =
      methodParams
        .map(param => {
          logger.info(s"Adding method parameter `${param.getName}` to scope table.")

          val scopeTableItem = ScopeTableItem(
            param.resolveBinding().getKey,
            param.getName.getIdentifier,
            param.getType.resolveBinding().getQualifiedName,
            None,
            None,
            filePath,
            getLineNumber(node)
          )

          logger.debug(s"scopeTableItem: $scope")

          // Add entry to scope table for the current compilation unit
          _scopeTable(scopeTableItem.id) = scopeTableItem

          // Create instrumentation log statement
          createInstrumentationLogStatement(ast, param.resolveBinding().getKey, param.getName.getIdentifier)
        })
        .map(ast.newExpressionStatement)
        .asJava

    logger.info(s"Adding instrumentation log statements for method parameters.")
    node
      .getBody
      .statements()
      .asInstanceOf[util.List[Statement]]
      .addAll(0, logStatements)

    super.visit(node)
  }

  /**
   * Visits a variable declaration statement and adds the variable to the scope table.
   *
   * An instrumentation log statement is NOT inserted after the declaration because a variable may not be initialized at
   * the time of declaration, causing the instrumented code to fail during compilation.
   *
   * @param node The variable declaration statement node to be visited.
   * @return `true` if the children of this node should be visited, and `false` if the children of this node should be
   *         skipped.
   */
  override def visit(node: VariableDeclarationStatement): Boolean = {
    logger.trace(s"visit(node: VariableDeclarationStatement) = $node")

    node
      .fragments()
      .asInstanceOf[util.List[VariableDeclarationFragment]]
      .asScala
      .foreach { fragment =>
        logger.info(s"Adding variable declaration `${fragment.getName}` to scope table.")

        val scope = ScopeTableItem(
          fragment.resolveBinding().getKey,
          fragment.getName.getIdentifier,
          fragment.resolveBinding().getType.getQualifiedName,
          None,
          Try(fragment.resolveBinding().getDeclaringClass.getQualifiedName).toOption,
          filePath,
          getLineNumber(fragment)
        )

        logger.debug(s"scope: $scope")

        // Add entry to scope table for the current compilation unit
        _scopeTable(scope.id) = scope
      }

    super.visit(node)
  }

  /**
   * Visits an assignment expression and inserts an instrumentation log statement after the assignment statement
   * containing the assignment expression.
   *
   * @param node The assignment expression node to be visited.
   * @return `true` if the children of this node should be visited, and `false` if the children of this node should be
   *         skipped.
   */
  override def visit(node: Assignment): Boolean = {
    logger.trace(s"visit(node: Assignment) = $node")

    // Get the block in which the assignment statement of the assignment expression is contained
    node.getParent.getParent match {
      case block: Block =>
        logger.info(s"Adding instrumentation log statement for the LHS of the assignment.")

        val ast = block.getAST
        val lhs = node.getLeftHandSide.asInstanceOf[Name]
        logger.debug(s"lhs: $lhs")

        val logStatement = createInstrumentationLogStatement(ast, lhs.resolveBinding().getKey, lhs.getFullyQualifiedName)

        val statements = block.statements().asInstanceOf[util.List[Statement]]
        logger.trace("statements: " + statements)

        val insertionIndex = statements.indexOf(node.getParent) + 1
        statements.add(insertionIndex, ast.newExpressionStatement(logStatement))
    }
    super.visit(node)
  }
}
