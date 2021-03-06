package com.javainstrumentor.tool.Constants

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable
import scala.jdk.CollectionConverters._

object ConfigReader {

  val configReader: Config = ConfigFactory.load("application.conf")

  //  val projects: mutable.Buffer[String] = configReader.getStringList("PROJECTS").asScala
  //  val projects: mutable.Buffer[String] = configReader.getStringList("project-paths").asScala
  val projects = configReader.getConfigList("project-paths").asScala
  val messageClientIPAddress: String = configReader.getString("message-client.ip")
  val messageClientPort: Int = configReader.getInt("message-client.port")
  val messageClientDelimiter: String = configReader.getString("message-client.delimiter")

  val projectInput = "input-path"
  val projectOutput = "output-path"
  val mainClass = "main-class"
  val args = "args"
}
