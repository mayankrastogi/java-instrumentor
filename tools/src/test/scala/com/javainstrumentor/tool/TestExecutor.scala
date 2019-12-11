package com.javainstrumentor.tool

import com.javainstrumentor.tool.Constants.ConfigReader
import com.javainstrumentor.tool.execution.JavaProcessExecutor
import com.javainstrumentor.tool.utils.IOUtils
import org.scalatest.FlatSpec
import org.slf4j.LoggerFactory

class TestExecutor extends FlatSpec {


  val javaProcessExecutor = new JavaProcessExecutor
  val testProject = "test-project"
  val mainClass = "MainClass.java"
  val classPath: String = IOUtils.resolvePathStringToResource(testProject)

  val logger = LoggerFactory.getLogger(this.getClass)

  "JavaExecutor" should "sucessfully compile given the classpath and file" in {

    val executionResult = javaProcessExecutor.compileJavaCode(classPath, mainClass)

    assert(executionResult == 0, "Compilation failed")

  }


  "JavaExecuctor" should "successfully execute the given file " in {

    val executionResult = javaProcessExecutor.compileAndExecute(mainClass, classPath)
    assert(executionResult == 0, "Compilation and execution failed")
  }

}
