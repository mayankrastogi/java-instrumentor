package com.javainstrumentor.clientlib

object ApplicationConstants {

  val JAVA_COMPILE_COMMAND = "javac -cp "
  val JAVA_EXECUTE_COMMAND = "java "

  /**
   * The system property that is used to specify the execution server's IP address.
   */
  val PROPERTY_SERVER_IP = "com.javainstrumentor.clientlib.messageClientIP"
  /**
   * The system property that is used to specify the execution server's port.
   */
  val PROPERTY_SERVER_PORT = "com.javainstrumentor.clientlib.messageClientPort"
  /**
   * The system property that is used to specify the delimiter for separating keys and values in instrumentor
   * log messages.
   */
  val PROPERTY_SERVER_DELIMITER = "com.javainstrumentor.clientlib.messageClientDelimiter"
}
