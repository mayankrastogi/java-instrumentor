package com.javainstrumentor.tool.execution


import com.javainstrumentor.clientlib.ApplicationConstants
import com.javainstrumentor.tool.Constants.ConfigReader
import com.javainstrumentor.tool.utils.IOUtils
import com.typesafe.scalalogging.LazyLogging

import scala.language.postfixOps
import scala.sys.process._


/**
  * Creates new Java process
  * Compiles and executes the project in a separate process
  */
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
  def compileAndExecute(mainFilePath: String, cp: String, args: Option[String] = None): Int = {

    //Compiling the java code
    try {
      compileJavaCode(cp, mainFilePath)
    }
    catch {
      case exception: Exception => println(exception)
        return -1
    }
    try {

      // java -Dvar1 -Dvar2 -cp classPath class arg1 arg2 arg3
      val command = ApplicationConstants.JAVA_EXECUTE_COMMAND +
        s" -D${ApplicationConstants.PROPERTY_SERVER_IP}=${ConfigReader.messageClientIPAddress}" +
        s" -D${ApplicationConstants.PROPERTY_SERVER_PORT}=${ConfigReader.messageClientPort}" +
        s" -D${ApplicationConstants.PROPERTY_SERVER_DELIMITER}=${ConfigReader.messageClientDelimiter}" +
        " -cp " + s"${IOUtils.resolveAbsolutePath(cp)}$cpDelimiter$systemClassPath" + " " +
        mainFilePath.split("/").last.replace(".java", "") + " " + args.getOrElse("")

      logger.info(s"Running java program: $command")

      val executedResult = command !

      executedResult
    }
    catch {
      case exception: Exception => {
        println(exception)
        -1
      }
    }


  }

  /**
    * * Compiles java code. Creates a Javac Command using the classpath and the file having main class
    * * ex: javac -cp class/path1:classpath2 main.java
    *
    * @param cp       the class path for the project to be compiled
    * @param mainFile The filename having main method
    */
  def compileJavaCode(cp: String, mainFile: String): Int = {

    val command = ApplicationConstants.JAVA_COMPILE_COMMAND + s"${IOUtils.resolveAbsolutePath(cp)}$cpDelimiter$systemClassPath ${IOUtils.resolveAbsolutePath(cp)}/$mainFile"

    logger.info("Running command {}", command)
    command !


  }

}
