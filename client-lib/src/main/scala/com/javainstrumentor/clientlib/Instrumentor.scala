package com.javainstrumentor.clientlib

/**
 * Client-library for sending instrumentation log messages to the execution server.
 *
 * The IP address, port, and the delimiter need to be specified using system properties while running the instrumented
 * program.
 *
 * @see [[com.javainstrumentor.clientlib.ApplicationConstants#PROPERTY_SERVER_IP]],
 *      [[com.javainstrumentor.clientlib.ApplicationConstants#PROPERTY_SERVER_PORT]],
 *      [[com.javainstrumentor.clientlib.ApplicationConstants#PROPERTY_SERVER_DELIMITER]]
 */
object Instrumentor {

  /**
   * The IP Address of the execution server.
   */
  private lazy val IPAddress = System.getProperty(ApplicationConstants.PROPERTY_SERVER_IP)
  /**
   * The port of the execution server.
   */
  private lazy val port = System.getProperty(ApplicationConstants.PROPERTY_SERVER_PORT).toInt
  /**
   * The delimiter to use for separating keys from values in a log message that is sent to the execution server.
   */
  private lazy val delimiter = System.getProperty(ApplicationConstants.PROPERTY_SERVER_DELIMITER)

  //Initialize the client program when the first message is ready to be sent
  private lazy val client: MessageClient = new MessageClient(IPAddress, port)

  /**
   * Sends the instrumentation log message to the execution server.
   *
   * @param key   The unique identifier of the variable whose value is being logged.
   * @param value The current value of the variable.
   */
  def log(key: String, value: Any): Unit = {
    val message = s"$key$delimiter${value.toString}"

    // Make IPC call to Instrumentor Server
    client.sendMessage(message)
  }
}
