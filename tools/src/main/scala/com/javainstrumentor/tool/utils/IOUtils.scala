package com.javainstrumentor.tool.utils

import java.net.URI
import java.nio.file.{Files, Path, Paths}

import scala.io.Source
import scala.jdk.StreamConverters._

object IOUtils {

  def getJavaFilePaths(root: String): List[String] = {
    val path = Paths.get(root)
    getJavaFilePaths(path)
  }

  def getJavaFilePaths(root: Path): List[String] = {
    Files
      .walk(root)
      .toScala(List)
      .map(_.normalize.toString)
      .filter(_.endsWith(".java"))
  }

  def getJavaFilePathsFromResource(name: String): List[String] = {
    val path = resolvePathToResource(name)
    getJavaFilePaths(path)
  }

  def resolvePathToResource(name: String): Path = Paths.get(resolveUriToResource(name))

  def resolveUriToResource(name: String): URI = getClass.getClassLoader.getResource(name).toURI

  def resolvePathStringToResource(name: String): String = resolvePathToResource(name).toString

  /**
   * Extracts the file name from a file path.
   *
   * @param filePath The file path.
   * @return The file name.
   */
  def getFileName(filePath: String): String = filePath.stripPrefix("'").stripSuffix("/").split("/").last

  def readFile(path: String): Array[Char] = {
    val file = Source.fromFile(path)
    val contents = file.toArray
    file.close()
    contents
  }
}
