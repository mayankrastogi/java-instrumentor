package com.javainstrumentor.tool

import com.javainstrumentor.tool.Constants.ConfigReader
import com.javainstrumentor.tool.IPC.SocketServer
import com.javainstrumentor.tool.execution.JavaProcessExecutor
import com.javainstrumentor.tool.parsing.scopetable.ScopeTableItem
import com.javainstrumentor.tool.parsing.{InstrumentationVisitor, Instrumentor, JavaProject}
import com.typesafe.scalalogging.LazyLogging

/**
  * Launcher that orchestrates ASTParsing, code Instrumentation, Scope table creation, Setting up IPC and executing instrumented code in a separate JVM
  */
object Launcher extends App with LazyLogging {

  // Extract the project paths to be parsed and instrumented
  val projects = ConfigReader.projects
  val executor = new JavaProcessExecutor
  val instrumentor = new Instrumentor


  //For every project
  projects.foreach(project => {


    val projectInputPath = project.getString(ConfigReader.projectInput)

    val projectOutputPath = project.getString(ConfigReader.projectOutput)

    val mainClass = project.getString(ConfigReader.mainClass)

    logger.info("Instrumenting project... {} ", projectInputPath)


    val scopeTable: Map[String, ScopeTableItem] = instrumentor.instrumentAndFindScopeTable(projectInputPath, projectOutputPath)


    val server = new SocketServer(scopeTable)


    val serverThread = new Thread(server)

    serverThread.start()

    //Wait for the IPC server to start
    while (!server.started) {
      logger.debug("waiting to get started....")
    }

    logger.debug("Waiting for the instrumented JVM process...")

    //Compile and execute on a separate JVM
    executor.compileAndExecute(mainClass, projectOutputPath)
    logger.debug("Joining the thread")
    serverThread.join()
    logger.debug("after Joining the thread")

    logger.info("updated Scope Table")
    logger.info("*********************************")
    server.map.values.foreach(item => println(item.values))


  })

}
