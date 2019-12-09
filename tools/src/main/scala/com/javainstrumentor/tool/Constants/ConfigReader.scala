package com.javainstrumentor.tool.Constants

import com.typesafe.config.{Config, ConfigFactory}
import scala.jdk.CollectionConverters._

object ConfigReader {

  val configReader: Config = ConfigFactory.load("application.conf")

  val projects = configReader.getStringList("PROJECTS").asScala


}
