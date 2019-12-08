package com.javainstrumentor.execution


import java.io.File
import java.util
import java.io.{BufferedReader, InputStreamReader}
import System.{getProperty => Prop}

import com.javainstrumentor.Constants.ApplicationConstants

import sys.process._
import language.postfixOps

object JavaProcessExecutor extends App {

  /**
    * Compiles and executes the java files
    *
    * @param files    List of all files belonging to the project to be executed
    * @param args     the command line args to be given to executing project
    * @param mainFile name of the java class that has main method
    * @param cp       the classpath
    */
  def compileAndExecute(files: List[String], args: Option[String], mainFile: String, cp: String) {

    //Compiling the java code
    try {
      compileJavaCode(files)
    }
    catch {
      case exception: Exception => println(exception)
        return
    }
    try {
      val executedResult = ApplicationConstants.JAVA_COMPILE_COMMAND + cp + " " + mainFile !!

      println(executedResult)
    }


  }

  /**
    * Compiles java code: javac
    *
    * @param files
    */
  def compileJavaCode(files: List[String]): Unit = {

    val command = ApplicationConstants.JAVA_COMPILE_COMMAND + files.mkString(" ")

    var compileResult = command !!

    print(compileResult)


  }

}
