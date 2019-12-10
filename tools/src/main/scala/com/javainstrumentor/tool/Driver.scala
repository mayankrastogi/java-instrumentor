package com.javainstrumentor.tool

import com.javainstrumentor.tool.Constants.ConfigReader
import com.javainstrumentor.tool.IPC.SocketServer
import com.javainstrumentor.tool.execution.JavaProcessExecutor

object Driver extends App  {


  //Load files

  val projects = ConfigReader.projects
  val executor = new JavaProcessExecutor






  projects.foreach(project => {


//    val mainFile = project.split("/").last
//    val classPath = project.replace(mainFile, "") + ";" + System.getProperty("java.class.path").split(" ")(0)
//
//
//    //TODO : Extract AST and instrument code
//
//
//    //Pass HashTable to start so that the messages are captured and printed on console
//
//    val map = Map("key1" -> 1, "key2"  -> 2)
//
//    val server = new SocketServer(map)
//
//    new Thread(server).start()
//
//
//
//    //Wait till socket
//    while (!server.started) {
//      println("waiting to get started....")
//    }
//    println("connected!!")
//
//    executor.compileAndExecute(project, classPath)
//    Thread.sleep(10)

  })
}

