package com.javainstrumentor.clientlib

import scala.util.Try

object Instrumentor {

  private lazy val client: MessageClient = new MessageClient(IPAddress, port)
  private val IPAddress = Try(System.getProperty(ApplicationConstants.PROPERTY_SERVER_IP)).getOrElse("")
  private val port = Try(System.getProperty(ApplicationConstants.PROPERTY_SERVER_PORT).toInt).getOrElse(0)
  private val delimiter = Try(System.getProperty({
    ApplicationConstants.PROPERTY_SERVER_DELIMITER
  })).getOrElse("")

  def log(key: String, value: Any): Unit = {
    val message = s"$key$delimiter${value.toString}"

    // Make IPC call to Instrumentor Server
    client.sendMessage(message)
  }
}
