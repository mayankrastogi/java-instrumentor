package com.javainstrumentor.tool.Constants

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable
import scala.jdk.CollectionConverters._

object ConfigReader {

  val configReader: Config = ConfigFactory.load("application.conf")

  val projects: mutable.Buffer[String] = configReader.getStringList("PROJECTS").asScala

  val messageClientIPAddress: String = configReader.getString("message-client.ip")
  val messageClientPort: Int = configReader.getInt("message-client.port")
  val messageClientDelimiter: String = configReader.getString("message-client.delimiter")
}
