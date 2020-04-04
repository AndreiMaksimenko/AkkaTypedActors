lazy val akkaHttpVersion = "10.1.11"
lazy val akkaVersion = "2.6.4"

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "com.epam",
      scalaVersion := "2.13.1"
    )
  ),
  name := "akkahttp",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.0-M2" % Test,
    "org.typelevel" %% "cats-core" % "2.1.1",
    "com.typesafe.play" %% "play-json" % "2.8.1"
  )
)
