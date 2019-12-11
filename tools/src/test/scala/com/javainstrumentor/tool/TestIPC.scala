package com.javainstrumentor.tool

import com.javainstrumentor.clientlib.MessageClient
import com.javainstrumentor.tool.Constants.ConfigReader
import com.javainstrumentor.tool.IPC.SocketServer
import com.javainstrumentor.tool.parsing.scopetable.ScopeTableItem
import com.javainstrumentor.tool.utils.IOUtils
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.slf4j.LoggerFactory

import scala.collection.mutable

class TestIPC extends FlatSpec with BeforeAndAfter {

  val logger = LoggerFactory.getLogger(this.getClass)
  val testScopeTable = mutable.Map[String, ScopeTableItem]()
  var socketServer: SocketServer = _
  var serverThread: Thread = _


  //create the socket server before starting the IPC test
  before {
    //generate scope table
    generateTestScopableItem.foreach(item => testScopeTable += (item.name -> item))
    val map = testScopeTable.toMap
    socketServer = new SocketServer(testScopeTable.toMap)

    //Starting a socket in seaprate thread
    serverThread = new Thread(socketServer)

    generateTestScopableItem.foreach(item => testScopeTable)

  }

//  after{
//    serverThread.join(10)
//  }

  def generateTestScopableItem: List[ScopeTableItem] = {

    val scopItems = List[ScopeTableItem](
      ScopeTableItem("tid1", "testName1", "int", None, None, "test/path1", 1),
      ScopeTableItem("tid2", "testName2", "float", None, None, "test/path2", 2),
      ScopeTableItem("tid3", "testName3", "string", None, None, "test/path3", 3)
    )
    scopItems

  }

  "Client" should "send message to the server socket" in {

    //started server
    serverThread.start()
    while (!socketServer.started) {
    }

    val messageClient = new MessageClient(ConfigReader.messageClientIPAddress, ConfigReader.messageClientPort)
    messageClient.sendMessage(s"t1d1${ConfigReader.messageClientDelimiter}value1")
    Thread.sleep(10)
    val value = socketServer.map.values.filter(item => item.id == "tid1").head.values.head

    logger.info("value received by server : {} ", value)
    assert(value == "value1")
//    messageClient.stopConnection()
//    serverThread.stop()

  }


}
