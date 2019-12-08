package com.javainstrumentor.tool

import com.javainstrumentor.tool.parsing.JavaProject
import com.typesafe.scalalogging.LazyLogging

object Launcher extends App with LazyLogging {

  val project1Path = "sample-projects/project1"

  val project1 = JavaProject(project1Path, resolveFromResources = true)

  project1.parsedSources.foreach(println)
}
