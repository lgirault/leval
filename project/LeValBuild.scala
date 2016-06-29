
import java.io.FileWriter

import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import sbt._, sbt.Keys._

//{project, Build}

object LeValBuild extends Build {

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
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.11.8",
    sbtVersion := "0.13.11",

    classPathFileName := "CLASSPATH",

    printClassPathFile in Test := classPathFileNameTask(Test).value,
    printClassPathFile in Compile := classPathFileNameTask(Compile).value,


    libraryDependencies ++=
      Seq("com.typesafe.akka" %% "akka-remote" % "2.4.7",
      //Multiple dependencies with the same organization/name but different versions. To avoid conflict, pick one version
//          "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
//          "org.scala-lang.modules" %% "scala-xml" % "1.0.3",
//          "org.scala-lang" % "scala-reflect" % "2.11.6",
  /*),
libraryDependencies in Test ++=
Seq(*/    "org.scalatest" %% "scalatest" % "2.2.4",
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

  lazy val root = (project in file(".")
    settings commonSettings("root")
    dependsOn (shared % "test->test;compile->compile",
               client % "test->test;compile->compile",
               server % "test->test;compile->compile")
    aggregate (shared, client, server)
    )

  val shared = (project
    settings commonSettings("shared")
    )

  val client = (project
    settings commonSettings("client")

    settings Seq[Setting[_]](
      mainClass in Compile := Some("leval.GUIClient"),
      libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.92-R10"
    )
    enablePlugins JavaAppPackaging
    dependsOn (shared % "test->test;compile->compile")
    )

  val server = (project
    settings commonSettings("server")
    settings {
      mainClass in Compile := Some("leval.Server")
    }
    enablePlugins JavaAppPackaging
    dependsOn (shared % "test->test;compile->compile")
    )

}