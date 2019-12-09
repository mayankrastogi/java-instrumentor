package com.javainstrumentor.tool.execution


import com.javainstrumentor.clientlib.ApplicationConstants
import com.javainstrumentor.tool.Constants.ConfigReader
import com.typesafe.scalalogging.LazyLogging

import scala.language.postfixOps
import scala.sys.process._

object JavaProcessExecutor extends App with LazyLogging {

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
      val command = ApplicationConstants.JAVA_EXECUTE_COMMAND +
        s" -D${ApplicationConstants.PROPERTY_SERVER_IP}=${ConfigReader.messageClientIPAddress}" +
        s" -D${ApplicationConstants.PROPERTY_SERVER_PORT}=${ConfigReader.messageClientPort}" +
        s" -D${ApplicationConstants.PROPERTY_SERVER_DELIMITER}=${ConfigReader.messageClientDelimiter}" +
        " -cp " + cp + " " + mainFilePath.split("/").last.replace(".java", "")

      logger.info(s"Running java program: $command")

      val executedResult = command !

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

    command !


  }

}
