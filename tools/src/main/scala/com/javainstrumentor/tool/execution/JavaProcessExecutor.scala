package com.javainstrumentor.tool.execution


import com.javainstrumentor.clientlib.ApplicationConstants
import com.javainstrumentor.tool.Constants.ConfigReader
import com.javainstrumentor.tool.utils.IOUtils
import com.typesafe.scalalogging.LazyLogging

import scala.language.postfixOps
import scala.sys.process._

class JavaProcessExecutor extends LazyLogging {


  val systemClassPath: String = System.getProperty("java.class.path").split(" ")(0)

  val cpDelimiter: String = if (System.getProperty("os.name").toLowerCase.startsWith("windows")) ";" else ":"

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
        " -cp " + s"${IOUtils.resolveAbsolutePath(cp)}$cpDelimiter$systemClassPath" + " " + mainFilePath.split("/").last.replace(".java", "")

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

    val command = ApplicationConstants.JAVA_COMPILE_COMMAND + s"${IOUtils.resolveAbsolutePath(cp)}$cpDelimiter$systemClassPath ${IOUtils.resolveAbsolutePath(cp)}/$mainFilePath"

    logger.info("Running command {}", command)
    command !


  }

}
