package com.javainstrumentor.tool.IPC

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.{ServerSocket, Socket}

import org.slf4j.{Logger, LoggerFactory}

/**
  *
  * IPC server which listens to client's message at specific port
  *
  * @param map Map having instrumented variables whose values will be sent by the instrumented execution
  */

class SocketServer(val map: Map[String, Int]) extends Runnable {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)


  //Placeholder to indicate whether server started listening to the client's message
  var started: Boolean = false

  /**
    * Start's listening to client's messages
    */
  def start(): Unit = {
    logger.info("Connecting......")
    val serverSocket = new ServerSocket(30)
    started = true
    val clientSocket: Socket = serverSocket.accept()

    logger.info("is client connected to server ? : {} ", clientSocket.isConnected)

    val out = new PrintWriter(clientSocket.getOutputStream, true)

    //Input reader from the client
    val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))


    var inComingMessage: String = null
    while ((inComingMessage = in.readLine()) != null) {
      logger.debug("Recieving message : {} ", inComingMessage)
      val key = inComingMessage.split("-")(0)
      val value = inComingMessage.split("-")(1)
      if (map.contains(key)) {
        logger.debug("old value in map for key {} is {} : ", key, map(key))
        logger.debug("new value in map for key {} is {} : ", key, value)
      }
      if ("!!-!!".equals(inComingMessage)) {
        out.println("Good bye!!")
        started = false
        return
      }
    }

  }

  override def run(): Unit = {
    start()
  }
}
