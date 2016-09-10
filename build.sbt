
import java.io.FileWriter

import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

//{project, Build}

val printClassPathFile = taskKey[File]("create a file containing the fullclass path")

val classPathFileName = settingKey[String]("Location of generated classpath script")

def classPathFileNameTask(cfg : Configuration): Def.Initialize[Task[File]] = Def.task {
  val f = baseDirectory.value / "target" / classPathFileName.value

  val writter = new FileWriter(f)
  val fcp = (fullClasspath in cfg).value.map(_.data.absolutePath)
  writter write "#!/bin/bash\n"
  writter write fcp.mkString("export CLASSPATH=", ":", "")
  // fish style :
  //writter.write(fcp.mkString("set CLASSPATH ", ":", ""))
  writter.close()
  f
}

def commonSettings(module: String) = Seq[Setting[_]](
  organization := "",
  name := s"leval-$module",
  version := "0.12",
  scalaVersion := "2.12.0-M4",
  maintainer := "L. Girault ( loic.girault@gmail.com )",

  classPathFileName := "CLASSPATH",

  printClassPathFile in Test := classPathFileNameTask(Test).value,
  printClassPathFile in Compile := classPathFileNameTask(Compile).value,


  libraryDependencies ++=
    Seq("com.typesafe.akka" %% "akka-remote" % "2.4.7",
      "org.scalatest" %% "scalatest" % "2.2.6",
      "com.typesafe.akka" %% "akka-testkit" % "2.4.7"),

  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",       // yes, this is 2 args
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",

    "-unchecked",
    "-Xlint",
    //"-Xfatal-warnings",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture",
    "-Ywarn-unused-import"
  )
)

lazy val common = project.
  settings(commonSettings("common"))
//settings (libraryDependencies += "org.typelevel" %% "cats" % "0.6.0")


lazy val client = (project
  settings commonSettings("client")

  enablePlugins JDKPackagerPlugin
  
  enablePlugins JavaAppPackaging
  
  settings (
  name := "leval", //overrides commonSettings name value

  mainClass in Compile := Some("leval.GUIClient"),

  libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.92-R10",

  packageSummary := "leval software client",
  packageDescription := "Software to play Le Val online",

  jdkPackagerType := "installer",
  jdkPackagerProperties := Map("app.name" -> name.value, "app.version" -> version.value),
  jdkPackagerAppArgs := Seq(maintainer.value, packageSummary.value, packageDescription.value),

  (javaHome in JDKPackager) := (javaHome in JDKPackager).value orElse {
    for {
      //f <- Some(file("C:\\Program Files\\Java\\jdk1.8.0_102\\")) if f.exists()
      f <- Some(file("/usr/lib/jvm/java-8-jdk/")) if f.exists()
    } yield f
  }

  )
  dependsOn (common % "test->test;compile->compile"))


lazy val server = (project
  settings commonSettings("server")
  settings {
  mainClass in Compile := Some("leval.Server")
}
  enablePlugins JavaAppPackaging
  dependsOn (common % "test->test;compile->compile")
  )

lazy val root = (project in file(".")
  settings commonSettings("root")
  dependsOn (common % "test->test;compile->compile",
  client % "test->test;compile->compile",
  server % "test->test;compile->compile")
  aggregate (common, client, server)
  )
