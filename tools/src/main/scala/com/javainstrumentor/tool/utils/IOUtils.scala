package com.javainstrumentor.tool.utils

import java.net.URI
import java.nio.file.{Files, Path, Paths}
import java.util.Comparator

import scala.io.Source
import scala.jdk.StreamConverters._
import scala.util.Try

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

  def createDirectory(dirName: String): Try[Path] = Try {
    Files.createDirectories(Paths.get(dirName))
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
  def getFileName(filePath: String): String = Paths.get(filePath).getFileName.toString

  def readFile(path: String): Array[Char] = {
    val file = Source.fromFile(path)
    val contents = file.toArray
    file.close()
    contents
  }

  def deleteDirectory(dirName: String): Try[Unit] = Try {
    Files
      .walk(Paths.get(dirName))
      .sorted(Comparator.reverseOrder())
      .forEach(_.toFile.delete)
  }

  /**
    * Generates absolute path for the given path
    *
    * @param path relative path
    * @return the absolute path given to this path
    */
  def resolveAbsolutePath(path: String): String = {

    Paths.get(path).toAbsolutePath.toString

  }
}
