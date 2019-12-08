package com.javainstrumentor.clientlib

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.{ServerSocket, Socket}

import org.slf4j.{Logger, LoggerFactory}

/**
  * Sample server to test IPC
  */
object SocketServer {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)


  def start(): Unit = {


    logger.info("Connecting......")
    val serverSocket = new ServerSocket(30)


    //Should spawn a new java process

    println("here")

    val clientSocket: Socket = serverSocket.accept()

    logger.info("is client connected to server ? : {} ", clientSocket.isConnected)

    val out = new PrintWriter(clientSocket.getOutputStream, true)
    val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))


    logger.info("recieving mesage: {} ", in.readLine())
    var inComingMessage: String = null
    while ((inComingMessage = in.readLine()) != null) {
      out.println(inComingMessage)
      println("Recieving message : {} ", inComingMessage)
      logger.info("Reciend message from client; {} ", inComingMessage)
      if ("!!".equals(inComingMessage)) {
        out.println("Good bye!!")
        return
      }
    }

  }

  //
  //  def stop(): Unit = {
  //    in.close()
  //    out.close()
  //    serverSocket.close()
  //    clientSocket.close()
  //
  //  }

  def main(args: Array[String]): Unit = {
    val socketServer = SocketServer
    socketServer.start()
  }


}
