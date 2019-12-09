package com.javainstrumentor.tool.IPC

import java.io.{BufferedReader, InputStreamReader}
import java.net.{ServerSocket, Socket}

import com.javainstrumentor.tool.Constants.ConfigReader
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
    val serverSocket = new ServerSocket(ConfigReader.messageClientPort)
    started = true
    val clientSocket: Socket = serverSocket.accept()

    logger.info("is client connected to server ? : {} ", clientSocket.isConnected)

    //    val out = new PrintWriter(clientSocket.getOutputStream, true)

    //Input reader from the client
    val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))

    try {
      while (in.ready()) {
        val inComingMessage = in.readLine()
        logger.info("Recieving message : {} ", inComingMessage)
        //      val key = inComingMessage.split(ConfigReader.messageClientDelimiter)(0)
        //      val value = inComingMessage.split(ConfigReader.messageClientDelimiter)(1)
        //
        //      logger.debug("new value in map for key {} is {} : ", key, value)
        //      if (map.contains(key)) {
        //        logger.debug("old value in map for key {} is {} : ", key, map(key))
        //        logger.debug("new value in map for key {} is {} : ", key, value)
        //      }
        //      if ("!!-!!".equals(inComingMessage)) {
        //        out.println("Good bye!!")
        //        started = false
        //        return
        //      }
      }
    }
    catch {
      case e: Exception => logger.error("MessageServer connection terminated.", e)
    }
    finally {
      serverSocket.close()
      clientSocket.close()
      //      out.close()
      in.close()
    }

  }

  override def run(): Unit = {
    start()
  }
}
