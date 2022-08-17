import java.io.FileWriter

val printClassPathFile = taskKey[File]("create a file containing the fullclass path")

val classPathFileName = settingKey[String]("Location of generated classpath script")

def classPathFileNameTask(cfg: Configuration): Def.Initialize[Task[File]] = Def.task {
  val f = baseDirectory.value / "target" / classPathFileName.value

  val writter = new FileWriter(f)
  val fcp = (cfg / fullClasspath).value.map(_.data.absolutePath)
  writter write "#!/bin/bash\n"
  writter write fcp.mkString("export CLASSPATH=", ":", "")
  // fish style :
  // writter.write(fcp.mkString("set CLASSPATH ", ":", ""))
  writter.close()
  f
}

ThisBuild / scalaVersion := "3.1.3"

lazy val leval = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(
    name := "leval",
    version := "0.15",
    // maintainer := "L. Girault ( loic.girault@posteo.net )",

    classPathFileName := "CLASSPATH",
    Test / printClassPathFile := classPathFileNameTask(Test).value,
    Compile / printClassPathFile := classPathFileNameTask(Compile).value,
    libraryDependencies ++=
      Seq(
        //      "com.typesafe.akka" %% "akka-remote" % "2.4.7",
        //      "com.typesafe.akka" %% "akka-slf4j" % "2.4.7",
        "ch.qos.logback" % "logback-classic" % "1.2.11",
        "org.scalatest" %%% "scalatest" % "3.2.13"
        //  "com.typesafe.akka" %% "akka-testkit" % "2.4.7"
      ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8", // yes, this is 2 args
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-unchecked",
      "-Xfatal-warnings",
      //    "-Xlint",
      //    "-Yno-adapted-args",
      //    "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
      //    "-Ywarn-numeric-widen",
      //    "-Ywarn-value-discard",
      //    "-Xfuture",
      //    "-Ywarn-unused-import",
      // "-rewrite",
      "-source:3.0-migration"
    )
  )
  .jvmSettings(
    libraryDependencies ++=
      Seq(
        "ch.qos.logback" % "logback-classic" % "1.2.11"
      )
  )
  .jsSettings(
    // Add JS-specific settings here
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq("com.github.japgolly.scalajs-react" %%% "core" % "2.1.1"),

    // copy  javascript files to js folder,that are generated using fastOptJS/fullOptJS
//    Compile/ fullOptJS / crossTarget := file("js"),
//      Compile / fastOptJS / crossTarget := file("js"),
//      Compile / packageJSDependencies / crossTarget := file("js"),
//      Compile / packageMinifiedJSDependencies / crossTarget := file("js"),
//    Compile / fastOptJS / artifactPath := ((Compile / fastOptJS / crossTarget).value / ((fastOptJS / moduleName).value + "-opt.js" )),

    // there is an error related source map with webpack :x
    fastOptJS / scalaJSLinkerConfig ~= {
      _.withSourceMap(false)
    },
    fullOptJS / scalaJSLinkerConfig ~= {
      _.withSourceMap(false)
    },
    Compile / npmDependencies ++= Seq("react" -> "18.2.0", "react-dom" -> "18.2.0")
  )
  .jsConfigure { project =>
    project.enablePlugins(
      ScalaJSBundlerPlugin
    )
  }
