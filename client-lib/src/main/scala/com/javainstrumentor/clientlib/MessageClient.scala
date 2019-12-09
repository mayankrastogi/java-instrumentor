package com.javainstrumentor.clientlib

import java.io._
import java.net._

import com.typesafe.scalalogging.LazyLogging

/**
 * Opens a socket connection for message passing
 */

class MessageClient(ip: String, port: Int) extends LazyLogging {

  logger.info(s"Opening socket connection to, ip: $ip, port: $port")
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
