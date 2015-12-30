import sbt.Project.projectToRef

lazy val clients = Seq(client)
lazy val scalaV = "2.11.7"

lazy val server = (project in file("server")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := clients,
  pipelineStages := Seq(scalaJSProd, gzip),
  libraryDependencies ++= Seq(
    jdbc,
    javaWs,
    "com.typesafe.play" %% "play-slick" % "1.1.1",
    "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
    "com.vmunier" %% "play-scalajs-scripts" % "0.3.0",
    "org.webjars" %% "webjars-play" % "2.4.0-1",
    "org.webjars" % "bootstrap" % "3.3.5",
    "org.webjars" % "d3js" % "3.5.6",
    "org.webjars" % "openlayers" % "3.8.2",
    "io.jeo" % "proj4j" % "0.1.1")
).enablePlugins(PlayScala).
  aggregate(clients.map(projectToRef): _*).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.2",
    "com.github.japgolly.fork.scalaz" %%% "scalaz-core" % "7.1.2",
    "be.doeraene" %%% "scalajs-jquery" % "0.8.1"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSPlay).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := scalaV).
  jsConfigure(_ enablePlugins ScalaJSPlay)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

onLoad in Global := (Command.process("project server", _: State)) compose
  (onLoad in Global).value
