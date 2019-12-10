package com.javainstrumentor.tool.tests

import com.javainstrumentor.clientlib.Instrumentor
import com.javainstrumentor.tool.parsing.JavaProject
import com.javainstrumentor.tool.utils.IOUtils
import org.scalatest.{Inspectors, Matchers, WordSpecLike}

class TestJavaProject extends WordSpecLike with Matchers with Inspectors {

  val testProjectDir = "test-project"
  val testProjectOutputDir = "."
  val testProjectDirFiles: List[String] = IOUtils.getJavaFilePathsFromResource(testProjectDir)

  val project = JavaProject(testProjectDir, testProjectOutputDir, resolveFromResources = true)

  "A JavaProject, when parsed" must {

    "parse ASTs for all source files in the project" in {
      val sources = project.parsedSources

      sources should have size 2
      sources.keys should contain allElementsOf testProjectDirFiles
    }

    "have the same structure as the input source files" in {
      forAll(project.parsedSources) { source =>
        val originalSource = String.valueOf(IOUtils.readFile(source._1))
        val parsedSource = source._2.toString

        parsedSource.linesIterator should have size originalSource.linesIterator.size
      }
    }
  }

  "A JavaProject, when instrumented" must {

    "add import statement for the Instrumentor client library in all source files" in {
      forAll(project.instrument.parsedSources.values) { ast =>
        ast.toString should startWith(s"import ${Instrumentor.getClass.getCanonicalName.stripSuffix("$")}")
      }
    }

    "add instrumentation log statements for all method declarations" in {
      val sources = project.parsedSources

      val mainClassAST = sources.keys.filter(_.endsWith("MainClass.java")).map(sources(_)).head
      val sampleClassAST = sources.keys.filter(_.endsWith("SampleClass.java")).map(sources(_)).head

      val mainMethodLogRegex = """Instrumentor\.log\(".*(?<=MainClass~MainClass;\.main\(\[Ljava\/lang\/String;\)V#args#0#0",args\);)"""
      mainClassAST.toString.linesIterator.toIndexedSeq(3) should include regex mainMethodLogRegex

      val sampleMethodLogRegex = """Instrumentor\.log\(".*(?<=SampleClass~SampleClass;\.sampleMethod\(I\)I#sampleMethodInput#0#0",sampleMethodInput\);)"""
      sampleClassAST.toString.linesIterator.toIndexedSeq(3) should include regex sampleMethodLogRegex
    }

    //    "add instrumentation log statements for all  variable declarations" in {
    //      val sources = project.parsedSources
    //
    //      val mainClassAST = sources.keys.filter(_.endsWith("MainClass.java")).map(sources(_)).head
    //      val sampleClassAST = sources.keys.filter(_.endsWith("SampleClass.java")).map(sources(_)).head
    //    }
  }
}
