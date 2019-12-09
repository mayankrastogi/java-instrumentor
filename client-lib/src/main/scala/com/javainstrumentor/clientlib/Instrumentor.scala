package com.javainstrumentor.clientlib

object Instrumentor {

  private var serverUri: String = _

  def setServerConfig(uri: String): Unit = ???

  def log(values: Any*): Unit = {
    println(values.mkString)

    // TODO: Make IPC call to InstrumentorServer
  }
}
