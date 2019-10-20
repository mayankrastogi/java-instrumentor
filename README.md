# Course Project
### Description: you will create a program that adds logging automatically into Java programs.
### Grade: 25%.
#### You can obtain this Git repo using the command ```git clone git@bitbucket.org:cs474_fall2019/courseproject.git```. You need to fork this repo, as explained below and you cannot push your code into this repo, otherwise, your grade for this course project will be ZERO!

## Preliminaries
If you have not already done so as part of your previous course homeworks, you must create your account at [BitBucket](https://bitbucket.org/), a Git repo management system. It is imperative that you use your UIC email account that has the extension @uic.edu. Once you create an account with your UIC address, BibBucket will assign you an academic status that allows you to create private repos. Bitbucket users with free accounts cannot create private repos, which are essential for submitting your homeworks and the course project. Please contact your TA from your **UIC.EDU** account and they will add you to the team repo as developers, since they already have the admin privileges. Please use your emails from the class registration roster to add you to the team and you will receive an invitation from BitBucket to join the team. Since it is still a large class, please use your UIC email address for communications or Piazza and avoid emails from other accounts like funnybunny1992@gmail.com. If you don't receive a response within 12 hours, please contact us via Piazza, it may be a case that your direct emails went to the spam folder.

In case you have not done so, you will install [IntelliJ](https://www.jetbrains.com/student/) with your academic license, the JDK, the Scala runtime and the IntelliJ Scala plugin, the [Simple Build Toolkit (SBT)](https://www.scala-sbt.org/1.x/docs/index.html) or some other building tool like Maven or Gradle and make sure that you can create, compile, and run Java programs. Please make sure that you can run [Java monitoring tools](https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/tooldescr025.html) or you can choose a newer JDK and tools if you want to use a more recent one.

## WARNING 
There are a few implementations of automatic logging instrumentors on the Internet. I know about many of them. You can study these implementations and feel free to use the ideas in your own implementation, and you must acknowledge what you use in your README. However, blindly copying large parts of some existing implementation in your code will result in receiving the grade F for the entire course with the transfer of your case of plagiarism to the Dean of Students Office, which will be followed with severe penalties. Most likely, you will be suspended or complete dismissed from the program in the worst case. Please do not plagiarize existing implementations, it is not worth it!

## Functionality
You will create an instrumentation program that takes the syntactically correct source code of some Java applications that you can obtain from public repos on Github, and using the Eclipse Java Abstract Syntax Tree (AST) parser, your program will parse this application into an AST. Next, in your program the nodes of the AST are traversed in some order using the pattern Visitor to compute scopes and variables that are declared and used in them. Then, for each expression and statement in each scope, your program will insert an instrumenting statement (e.g., a logging statement) to capture the values of the variables. This instrumenting statement is based on a template code fragment that you will define, and inserting this template into the parsed program will instantiate the template with the references to concrete variables whose values are captured in the given scope. Once the instrumentation procedure is finished, the AST is unparsed  (i.e., the source code is generated from the parse tree) and the instrumented source code is outputted (e.g., the original file f.java may be renamed into _old_f.java and the instrumented code is written into f.java).

For example, consider the following code fragment:
```java
while( a.f() != null ){//line 10
	b = f(a);		//line 11
}
```
When traversing its AST, it is determined that the method f of the object a is used in line 10 and the object a is used in line 11 and the value of the variable b is assigned. As a result of your instrumentation, the changed code may look like the following:
```java
TypeRetByMethodF res123 = a.f()
while( res123 != null ){//line 10
	TemplateClass.instrum(10, "While", pair("package.class.method.a.f", res123));
	b = f(a -> h(a));		//line 12
	TemplateClass.instrum(12, "Assign", pair("package.class.method.Function<Integer, Integer>", a), pair("package.class.method.b", b));
}
```

The method **instrum** may be implemented a parameterized static member of your template class **TemplateClass**, its first argument is the line number, the second argument is the type of the instrumented statement, and the following arguments are the captured variables. The gist of this course is to learn how to use the Eclipse AST parser Application Programming Interface (API) as well as to gain new knowledge about various grammatical constructs of the Java programming language. You can find a lot of information on the Internet about the Java Eclipse AST parser including the Eclipse documentation pages (search for ASTParser Eclipse). Please make sure that you resolve dependencies and import required libraries including the following org.eclipse.jdt.{annotation, apt.core, compiler, runtime} and org.osgi.core. When you get an error message, search what library is required to resolve a specific dependency and add it. To get you started on the Eclipse Parser, you nay use the following code fragment below.

```java
ASTParser parser = ASTParser.newParser(AST.JLS8);
parser.setKind(ASTParser.K_COMPILATION_UNIT);
parser.setResolveBindings(true);
Map options = JavaCore.getOptions();
parser.setCompilerOptions(options);
String unitName = "test.java";
parser.setUnitName(unitName);
String[] sources = { source_path };
String[] classpath = {};
parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
parser.setSource(readFileToString(filepath).toCharArray());
final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
```

Once the application is instrumented, make sure that it can be compiled and run using some build script and as a result of its execution it will output a trace into a file that contains instrumentation statements with variable paths and values, lines of the executed code, and the types of the executed statements.

In the next step, your program will construct a table of all variables and their bindings in the application's scopes, and this table will include the path to a variable, its declaration in the line of the code, and each variable will be assigned a unique identifier. Next, once instrumented and compiled, your program will run multiple instances of the instrumented application with different input values by starting the JVM, loading the classes of the instrumented application, and running the method main automatically. Below is the example code that you can use to bootstrap your work.

```java
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.*;
LaunchingConnector conn = null;
List<Connector> connectors = Bootstrap.virtualMachineManager().allConnectors();
for (Connector connector : connectors) {
    if (connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
        conn = (LaunchingConnector)connector;
    }
}
if(conn == null) 	throw new Error("No launching connector");
Map<String, Connector.Argument> arguments = conn.defaultArguments();
Connector.Argument mainArg = (Connector.Argument)arguments.get("main");
mainArg.setValue("test");
Connector.Argument options =(Connector.Argument)arguments.get("options");
File f = null;
String currentDir = System.getProperty("user.dir");
System.out.println(currentDir);
String option_val;
option_val = "-cp " + currentDir + "\\src\\main\\java";
options.setValue(option_val);
VirtualMachine vm = conn.launch(arguments);        	
EventManager mgr = new EventManager(vm);
mgr.setEventRequests();
mgr.start();          
Process process = vm.process();
StreamRedirectThread outThread = new StreamRedirectThread("output reader", process.getInputStream(), System.out);
outThread.start();
```
Instead of outputting the instrumented code into a file directly, you will pass the trace information from the executing Java application into the program directly. Instead of using fully qualified names of the variables, you will use their identifiers from the scope table. That is, when the statement TemplateClass.instrum(12, "Assign", a, b); is executed in the Java application, it will insert a node into a linked list (or some other data structure that you choose) that is hosted in the program, and this inserted node will map to the values of the variables a and b in the assign statement in line 12 in the Java application. Please feel free to offer some clever optimizations and tricks for additional bonus points.

As before, this course project script is written using a retroscripting technique, in which the project outlines are generally and loosely drawn, and the individual students improvise to create the implementation that fits their refined objectives. In doing so, students are expected to stay within the basic requirements of the course project and they are free to experiments. Asking questions is important, so please ask away at Piazza!

## Baseline Submission
Your baseline project submission should include your application design with a conceptual explanation in the document or in the comments in the source code of the architecture and design choices that you made, and the documentation that describe the build, deployment and the runtime, to be considered for grading. Your project submission should include all your source code as well as non-code artifacts (e.g., resource files if applicable), your project should be buildable using SBT. Simply copying some instrumentation examples from open-source projects and modifying them a bit will result in desk-rejecting your submission.

## Piazza collaboration
You can post questions and replies, statements, comments, discussion, etc. on Piazza. For this homework, feel free to share your ideas, mistakes, code fragments, commands from scripts, and some of your technical solutions with the rest of the class, and you can ask and advise others using Piazza on where resources and sample programs can be found on the internet, how to resolve dependencies and configuration issues. When posting question and answers on Piazza, please select the appropriate folder, i.e., courseproject to ensure that all discussion threads can be easily located. Active participants and problem solvers will receive bonuses from the big brother :-) who is watching your exchanges on Piazza (i.e., your class instructor). However, *you must not post your dockerfile or your source code!*

## Git logistics
**This is a group project,** with at least one and at most five members allowed in a group. Each student can participate in at most one group; enrolling in more than one group will result in the grade zero. Each group will select a group leader who will create a private fork and will invite the other group classmates with the write access to that fork repo. Each submission will include the names of all groupmates in the README.md and all groupmates will receive the same grade for this course project submission. Group leaders with successful submissions and good quality work will receive an additional 2% bonus for their management skills - it applied only to groups with more than two members.

If you submitted your previous homework(s), it means that you were already added as a member of CS474_Fall2019 team in Bitbucket and you will see the course project repo. You will fork this repository and your fork will be private, no one else besides you, your forkmates, the TA and your course instructor will have access to your fork. Please remember to grant a read (or write) access to your repository to your TA and your instructor and write access to your forkmates. You can commit and push your code as many times as you want. Your code will not be visible and it should not be visible to other students except for your forkmates, of course. When you push your project, your instructor and the TA will see you code in your separate private fork. Making your fork public or inviting other students except for your forkmates to join your fork before the submission deadline will result in losing your grade. For grading, only the latest push timed before the deadline will be considered. **If you push after the deadline, your grade for the homework will be zero**. For more information about using the Git and Bitbucket specifically, please use this [link as the starting point](https://confluence.atlassian.com/bitbucket/bitbucket-cloud-documentation-home-221448814.html). For those of you who struggle with the Git, I recommend a book by Ryan Hodson on Ry's Git Tutorial. The other book called Pro Git is written by Scott Chacon and Ben Straub and published by Apress and it is [freely available](https://git-scm.com/book/en/v2/). There are multiple videos on youtube that go into details of the Git organization and use.

Please follow this naming convention while submitting your work : "Firstname_Lastname_project" without quotes, where the group leader will specify her/his first and last names **exactly as the group leader is registered with the University system**, so that we can easily recognize your submission. I repeat, make sure that you will give both your TA and the course instructor the read access to your *private forked repository*.

## Discussions and submission
You can post questions and replies, statements, comments, discussion, etc. on Piazza. Remember that you cannot share your code and your solutions privately, but you can ask and advise others using Piazza and StackOverflow or some other developer networks where resources and sample programs can be found on the Internet, how to resolve dependencies and configuration issues. Yet, your implementation should be your own and you cannot share it. Alternatively, you cannot copy and paste someone else's implementation and put your name on it. Your submissions will be checked for plagiarism. **Copying code from your classmates or from some sites on the Internet will result in severe academic penalties up to the termination of your enrollment in the University**. When posting question and answers on Piazza, please select the appropriate folder, i.e., **course project** to ensure that all discussion threads can be easily located.


## Submission deadline and logistics
Tuesday, December 10, 2019 at 10PM CST via the bitbucket repository. Your submission will include the source code, your documentation with instructions and detailed explanations on how to assemble and deploy your program both in IntelliJ and CLI SBT, and a document that explains how you built and deployed your program and what your experiences are with instrumenting open-source Java programs, and the limitations of your implementation. Again, do not forget, please make sure that you will give both your TA and your instructor the read access to your private forked repository. Your name should be shown in your README.md file and other documents. Your code should compile and run from the command line using the commands like ```sbt clean compile test``` and from the docker image. Naturally, you project should be IntelliJ friendly, i.e., your graders should be able to import your code into IntelliJ and run from there. Use .gitignore to exlude files that should not be pushed into the repo.

## Evaluation criteria
- the maximum grade for this homework is 25% with the bonus up to 2% for being the group leader for a group with more than two members. Points are subtracted from this maximum grade: for example, saying that 2% is lost if some requirement is not completed means that the resulting grade will be 25%-2% => 23%; if the core functionality does not work, no bonus points will be given;
- the code does not work in that it does not produce a correct output or crashes: up to 25% lost;
- insufficient comments in your code: up to 15% lost;
- insufficient tests in your codebase: up to 15% lost;
- not having tests that test the functionality of your instrumenter: up to 25% lost;
- missing essential comments and explanations from the source code that you wrote: up to 20% lost;
- your Scala code is simply a version of imperative Java code with many mutable variables: up to 5% lost;
- your code does not have sufficient comments or your accompanying documents do not contain a description of how you designed and implemented the instrumenter: up to 20% lost;
- the documentation exists but it is insufficient to understand : up to 20% lost;
- the minimum grade for this course project cannot be less than zero.

That's it, folks!