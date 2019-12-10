package com.javainstrumentor.clientlib

import scala.util.Try

object Instrumentor {

  private lazy val client: MessageClient = new MessageClient(IPAddress, port)
  private lazy val IPAddress = System.getProperty(ApplicationConstants.PROPERTY_SERVER_IP)
  private lazy val port = System.getProperty(ApplicationConstants.PROPERTY_SERVER_PORT).toInt
  private lazy val delimiter = System.getProperty({
    ApplicationConstants.PROPERTY_SERVER_DELIMITER
  })

  def log(key: String, value: Any): Unit = {
    val message = s"$key$delimiter${value.toString}"

    // Make IPC call to Instrumentor Server
    client.sendMessage(message)
  }
}
