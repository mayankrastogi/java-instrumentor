package com.javainstrumentor.tool.tests

import com.javainstrumentor.clientlib.Instrumentor
import com.javainstrumentor.tool.parsing.JavaProject
import com.javainstrumentor.tool.parsing.scopetable.ScopeTableItem
import com.javainstrumentor.tool.utils.IOUtils
import org.scalatest._

class TestJavaProject extends WordSpecLike with Matchers with Inspectors with Inside with BeforeAndAfter {

  val testProjectDir = "test-project"
  val testProjectOutputDir = "."
  val testProjectDirFiles: List[String] = IOUtils.getJavaFilePathsFromResource(testProjectDir)

  var project: JavaProject = _

  before {
    project = JavaProject(testProjectDir, testProjectOutputDir, resolveFromResources = true)
  }

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
      val sources = project.instrument.parsedSources

      val mainClassAST = sources.keys.filter(_.endsWith("MainClass.java")).map(sources(_)).head
      val sampleClassAST = sources.keys.filter(_.endsWith("SampleClass.java")).map(sources(_)).head

      val mainMethodLogRegex = """Instrumentor\.log\(".*(?<=MainClass~MainClass;\.main\(\[Ljava\/lang\/String;\)V#args#0#0",args\);)"""
      mainClassAST.toString.linesIterator.toIndexedSeq(3) should include regex mainMethodLogRegex

      val sampleMethodLogRegex = """Instrumentor\.log\(".*(?<=SampleClass~SampleClass;\.sampleMethod\(I\)I#sampleMethodInput#0#0",sampleMethodInput\);)"""
      sampleClassAST.toString.linesIterator.toIndexedSeq(3) should include regex sampleMethodLogRegex
    }

    "add instrumentation log statements for all assignment expressions" in {
      val sources = project.instrument.parsedSources

      val mainClassAST = sources.keys.filter(_.endsWith("MainClass.java")).map(sources(_)).head.toString.linesIterator.toIndexedSeq
      val sampleClassObjectRegex = """Instrumentor\.log\(".*(?<=MainClass~MainClass;\.main\(\[Ljava\/lang\/String;\)V#sampleClassObject",sampleClassObject\);)"""
      val sampleMethodInputRegex = """Instrumentor\.log\(".*(?<=MainClass~MainClass;\.main\(\[Ljava\/lang\/String;\)V#sampleMethodInput",sampleMethodInput\);)"""
      val sampleMethodReturnRegex = """Instrumentor\.log\(".*(?<=MainClass~MainClass;\.main\(\[Ljava\/lang\/String;\)V#sampleMethodReturn",sampleMethodReturn\);)"""

      mainClassAST(6) should include regex sampleClassObjectRegex
      mainClassAST(9) should include regex sampleMethodInputRegex
      mainClassAST(12) should include regex sampleMethodReturnRegex

      val sampleClassAST = sources.keys.filter(_.endsWith("SampleClass.java")).map(sources(_)).head.toString.linesIterator.toIndexedSeq
      val numberRegex = """Instrumentor\.log\(".*(?<=SampleClass~SampleClass;\.sampleMethod\(I\)I#number",number\);)"""
      val sampleMethodInputInSampleClassRegex = """Instrumentor\.log\(".*(?<=SampleClass~SampleClass;\.sampleMethod\(I\)I#sampleMethodInput#0#0",sampleMethodInput\);)"""

      sampleClassAST(6) should include regex numberRegex
      sampleClassAST(8) should include regex sampleMethodInputInSampleClassRegex
    }

    "add an entry in the scope table for all method declarations and their parameters" in {
      val scopeTable = project.instrument.scopeTable.get.values

      // MainClass

      forExactly(1, scopeTable) { item =>
        inside(item) { case ScopeTableItem(id, name, dataType, methodArguments, parentClass, filePath, lineNumber) =>
          id should endWith("MainClass~MainClass;.main([Ljava/lang/String;)V")
          name shouldBe "main"
          dataType shouldBe "void"
          methodArguments.get should contain only "String[] args"
          parentClass shouldBe Some("MainClass")
          filePath should endWith("MainClass.java")
          lineNumber shouldBe 2
        }
      }

      forExactly(1, scopeTable) { item =>
        inside(item) { case ScopeTableItem(id, name, dataType, methodArguments, parentClass, filePath, lineNumber) =>
          id should endWith("MainClass~MainClass;.main([Ljava/lang/String;)V#args#0#0")
          name shouldBe "args"
          dataType shouldBe "java.lang.String[]"
          methodArguments shouldBe empty
          parentClass shouldBe empty
          filePath should endWith("MainClass.java")
          lineNumber shouldBe 2
        }
      }

      // SampleClass

      forExactly(1, scopeTable) { item =>
        inside(item) { case ScopeTableItem(id, name, dataType, methodArguments, parentClass, filePath, lineNumber) =>
          id should endWith("SampleClass~SampleClass;.sampleMethod(I)I")
          name shouldBe "sampleMethod"
          dataType shouldBe "int"
          methodArguments.get should contain only "int sampleMethodInput"
          parentClass shouldBe Some("SampleClass")
          filePath should endWith("SampleClass.java")
          lineNumber shouldBe 2
        }
      }

      forExactly(1, scopeTable) { item =>
        inside(item) { case ScopeTableItem(id, name, dataType, methodArguments, parentClass, filePath, lineNumber) =>
          id should endWith("SampleClass~SampleClass;.sampleMethod(I)I#sampleMethodInput#0#0")
          name shouldBe "sampleMethodInput"
          dataType shouldBe "int"
          methodArguments shouldBe empty
          parentClass shouldBe empty
          filePath should endWith("SampleClass.java")
          lineNumber shouldBe 2
        }
      }
    }

    "add an entry in the scope table for all variable declarations" in {
      val scopeTable = project.instrument.scopeTable.get.values

      // MainClass

      forExactly(1, scopeTable) { item =>
        inside(item) { case ScopeTableItem(id, name, dataType, methodArguments, parentClass, filePath, lineNumber) =>
          id should endWith("MainClass~MainClass;.main([Ljava/lang/String;)V#sampleClassObject")
          name shouldBe "sampleClassObject"
          dataType shouldBe "SampleClass"
          methodArguments shouldBe empty
          parentClass shouldBe empty
          filePath should endWith("MainClass.java")
          lineNumber shouldBe 3
        }
      }

      forExactly(1, scopeTable) { item =>
        inside(item) { case ScopeTableItem(id, name, dataType, methodArguments, parentClass, filePath, lineNumber) =>
          id should endWith("MainClass~MainClass;.main([Ljava/lang/String;)V#sampleMethodInput")
          name shouldBe "sampleMethodInput"
          dataType shouldBe "int"
          methodArguments shouldBe empty
          parentClass shouldBe empty
          filePath should endWith("MainClass.java")
          lineNumber shouldBe 5
        }
      }

      forExactly(1, scopeTable) { item =>
        inside(item) { case ScopeTableItem(id, name, dataType, methodArguments, parentClass, filePath, lineNumber) =>
          id should endWith("MainClass~MainClass;.main([Ljava/lang/String;)V#sampleMethodReturn")
          name shouldBe "sampleMethodReturn"
          dataType shouldBe "int"
          methodArguments shouldBe empty
          parentClass shouldBe empty
          filePath should endWith("MainClass.java")
          lineNumber shouldBe 7
        }
      }

      // SampleClass

      forExactly(1, scopeTable) { item =>
        inside(item) { case ScopeTableItem(id, name, dataType, methodArguments, parentClass, filePath, lineNumber) =>
          id should endWith("SampleClass~SampleClass;.sampleMethod(I)I#number")
          name shouldBe "number"
          dataType shouldBe "int"
          methodArguments shouldBe empty
          parentClass shouldBe empty
          filePath should endWith("SampleClass.java")
          lineNumber shouldBe 3
        }
      }
    }
  }
}
