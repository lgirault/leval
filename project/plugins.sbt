logLevel := Level.Warn

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.10.1")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

//sbt-scalajs-bundler is pulled by scalablytyped.converter
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.0")
//addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta39")