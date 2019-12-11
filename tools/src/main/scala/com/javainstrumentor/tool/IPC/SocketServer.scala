package com.javainstrumentor.tool.IPC

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.{ServerSocket, Socket}

import com.javainstrumentor.tool.Constants.ConfigReader
import com.javainstrumentor.tool.parsing.scopetable.ScopeTableItem
import org.slf4j.{Logger, LoggerFactory}

/**
  *
  * IPC server which listens to client's message at specific port.
  *
  * Implements runnable, should be started in a separate thread as Instrumented code will be executed in a separate JVM for parallel IPC
  *
  * @param map Map having instrumented variables whose values will be sent by the instrumented execution
  *
  *            Captures the values sent by the instrumented code
  */

class SocketServer(val map: Map[String, ScopeTableItem]) extends Runnable {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)


  //Placeholder to indicate whether server started listening to the client's message
  var started: Boolean = false


  /**
    * Start's listening to client's messages and update the scope table with the values for corresponding messages
    */

  def start(): Unit = {
    logger.info("Connecting......")
    val serverSocket = new ServerSocket(ConfigReader.messageClientPort)
    logger.info("Started server socket connection at {}:{} ", ConfigReader.messageClientIPAddress, ConfigReader.messageClientPort)
    started = true
    val clientSocket: Socket = serverSocket.accept()

    logger.info("is client connected to server ? : {} ", clientSocket.isConnected)


    //Input reader from the client
    val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))

    logger.info("Ready ? {} ", in.ready())


    logger.info("Scope table passed: {} ", map)
    var inComingMessage = ""

    try {

      while ((inComingMessage = in.readLine()) != null) {

        logger.info("Recieving message : {} ", inComingMessage)
        val key = inComingMessage.split(ConfigReader.messageClientDelimiter)(0)
        val value = inComingMessage.split(ConfigReader.messageClientDelimiter)(1)

        logger.debug("key {} is {} : ", key, value)

        // If the scope table has the variable associated with the message
        if (map.contains(key)) {
          val scopeTableItem = map(key)

          logger.info("Appending {} to key {} ", value, key)

          scopeTableItem.values += value

          logger.debug(" Updated scope item value {} ", scopeTableItem.values)
        }

      }
    }
    catch {
      case e: Exception => {}
    }
    finally {
      serverSocket.close()
      clientSocket.close()
      in.close()
    }

  }

  override def run(): Unit = {
    start()
  }
}
