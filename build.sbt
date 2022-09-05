import java.io.FileWriter

val Http4sVersion = "1.0.0-M35"
val CirceVersion = "0.14.2"
val CatsVersion = "2.8.0"
val CatsEffectVersion = "3.3.14"
val LogbackVersion = "1.2.11"
val PixiVersion = "6.5.2"
val ScalaTestVersion = "3.2.13"

val printClassPathFile =
  taskKey[File]("create a file containing the fullclass path")

val classPathFileName =
  settingKey[String]("Location of generated classpath script")

def classPathFileNameTask(cfg: Configuration): Def.Initialize[Task[File]] =
  Def.task {
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
ThisBuild / developers := List(
  Developer(
    "lgirault",
    "L. Girault",
    "loic.girault+leval@posteo.net",
    url("https://github.com/lgirault")
  )
)

lazy val `pixi-js` = project
  .settings(
    name := "pixi-js",
    Compile / npmDependencies ++= Seq(
      //"pixi.js" -> PixiVersion,
      //following are transitive dependency of pixi.js but explicit them for scalablytypes
      "@types/earcut" -> "2.1.1",
      "@types/offscreencanvas" -> "2019.7.0",
      "eventemitter3" -> "3.1.0"
    ),
    // stIgnore ++= List(
    //   "pixi.js" // https://github.com/ScalablyTyped/Converter/issues/471
    // ),
    stFlavour := Flavour.ScalajsReact
  )
  .enablePlugins(ScalaJSPlugin,
      ScalaJSBundlerPlugin,
      ScalablyTypedConverterPlugin
      )

lazy val leval = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(
    name := "leval",
    version := "0.15",
    classPathFileName := "CLASSPATH",
    Test / printClassPathFile := classPathFileNameTask(Test).value,
    Compile / printClassPathFile := classPathFileNameTask(Compile).value,
    libraryDependencies ++=
      Seq(
        "org.typelevel" %%% "cats-core" % CatsVersion,
        "io.circe" %%% "circe-generic" % CirceVersion,
        // "io.circe" %%% "circe-generic-extras" % CirceVersion,
        "io.circe" %%% "circe-parser" % CirceVersion,
        "org.scalatest" %%% "scalatest" % ScalaTestVersion,
        "dev.optics" %%% "monocle-core" % "3.1.0"
      ),
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-deprecation",
      "-unchecked",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-Xfatal-warnings",
      //    "-Xlint",
      //    "-Yno-adapted-args",
      //    "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
      //    "-Ywarn-numeric-widen",
      //    "-Ywarn-value-discard",
      //    "-Xfuture",
      //    "-Ywarn-unused-import",
      // "-rewrite",
      "-new-syntax",
      "-source:future",
      "-explain"
    )
  )
  .jvmSettings(
    libraryDependencies ++=
      Seq(
        "org.http4s" %% "http4s-ember-server" % Http4sVersion,
        "org.http4s" %% "http4s-ember-client" % Http4sVersion,
        "org.http4s" %% "http4s-circe" % Http4sVersion,
        "org.http4s" %% "http4s-dsl" % Http4sVersion,
        "ch.qos.logback" % "logback-classic" % LogbackVersion % Runtime
      )
  )
  .jsSettings(
    // Add JS-specific settings here
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % "2.1.1",
      "com.github.japgolly.scalajs-react" %%% "extra" % "2.1.1"
    ),

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
    Compile / npmDependencies ++= Seq(
      "react" -> "18.2.0",
      "react-dom" -> "18.2.0",
      "pixi.js" -> PixiVersion,
    )//,
  //  stIgnore ++= List(
  //    "react",
  //    "react-dom",
  //    //"pixi.js" // https://github.com/ScalablyTyped/Converter/issues/471
  //  ),
  //  stFlavour := Flavour.ScalajsReact
  )
  .jsConfigure { project =>

    project
      .dependsOn(`pixi-js`)
      .enablePlugins(
      ScalaJSBundlerPlugin
      // ScalablyTypedConverterPlugin
    )
  }
