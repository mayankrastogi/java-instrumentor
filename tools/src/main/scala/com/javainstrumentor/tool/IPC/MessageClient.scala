package com.javainstrumentor.tool.IPC

import java.io._
import java.net._
import org.slf4j.{Logger, LoggerFactory}

/**
  * Opens a socket connection for message passing
  */

class MessageClient(ip: String, port: Int) {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val clientSocket = new Socket(ip, port)
  val outputStreamWriter = new PrintWriter(clientSocket.getOutputStream, true)

  def sendMessage(message: String): Unit = {
    logger.info("Sending message {} to {}:{} ", message, ip, port)
    outputStreamWriter.println(message)
  }

  def stopConnection(): Unit = {
    outputStreamWriter.close()
    clientSocket.close()
  }

}

//object MessageClient {
//  def main(args: Array[String]): Unit = {
//    val socketClient = new MessageClient(ApplicationConstants.SERVER_IP, ApplicationConstants.SOCKET_PORT)
//
//    val messages = List("Hi", "hello", "another message", "good bye", "!!")
//
//    messages.foreach(message => socketClient.sendMessage(message))
//
//  }
//}
