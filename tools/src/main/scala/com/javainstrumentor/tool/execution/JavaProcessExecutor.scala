package com.javainstrumentor.tool.execution


import com.javainstrumentor.tool.Constants.ApplicationConstants

import sys.process._
import language.postfixOps

object JavaProcessExecutor extends App {

  /**
    * Compiles and executes the java files
    *
    * @param args         the command line args to be given to executing project
    * @param mainFilePath name of the java class that has main method
    * @param cp           the classpath
    */
  def compileAndExecute(mainFilePath: String, cp: String, args: Option[String] = None) {

    //Compiling the java code
    try {
      compileJavaCode(cp, mainFilePath)
    }
    catch {
      case exception: Exception => println(exception)
        return
    }
    try {
      val executedResult = ApplicationConstants.JAVA_EXECUTE_COMMAND + cp + " "+ mainFilePath.split("/").takeRight(1).mkString.replace(".java","")  !!

      println(executedResult)
    }
    catch {
      case exception: Exception => println(exception)
    }


  }

  /**
    * Compiles java code: javac
    *
    * @param files
    */
  private def compileJavaCode(cp: String, mainFilePath: String): Unit = {

    val command = ApplicationConstants.JAVA_COMPILE_COMMAND + cp + " " + mainFilePath

    var compileResult = command !!

    print(compileResult)


  }

}
