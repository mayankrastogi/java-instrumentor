package com.javainstrumentor.tool

import com.javainstrumentor.tool.Constants.ConfigReader
import com.javainstrumentor.tool.Driver.executor
import com.javainstrumentor.tool.IPC.SocketServer
import com.javainstrumentor.tool.execution.JavaProcessExecutor
import com.javainstrumentor.tool.parsing.scopetable.ScopeTableItem
import com.javainstrumentor.tool.parsing.{InstrumentationVisitor, Instrumentor, JavaProject}
import com.typesafe.scalalogging.LazyLogging

object Launcher extends App with LazyLogging {

  val project1Path = "sample-projects/project1"
  val project1OutputPath = "code-gen/project1"

  val projects = ConfigReader.projects
  val executor = new JavaProcessExecutor
  val instrumentor = new Instrumentor


  projects.foreach(project => {


    val projectInputPath = project.getString(ConfigReader.projectInput)

    val projectOutputPath = project.getString(ConfigReader.projectOutput)

    val mainClass = project.getString(ConfigReader.mainClass)

    logger.info("Instrumenting project... {} ", projectInputPath)


    //Instruments and writes the instrumented code to the output path
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
