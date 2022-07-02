ThisBuild / tlBaseVersion := "0.1"

ThisBuild / organization            := "io.github.massimosiani"
ThisBuild / organizationName        := "Massimo Siani"
ThisBuild / licenses                := Seq(License.Apache2)
ThisBuild / developers              := List(
  tlGitHubDev("massimosiani", "Massimo Siani")
)

ThisBuild / tlCiHeaderCheck         := false
ThisBuild / tlCiScalafixCheck       := true
ThisBuild / tlCiScalafmtCheck       := true
ThisBuild / tlSitePublishBranch     := Some("main")
ThisBuild / tlSonatypeUseLegacyHost := false

val Scala213 = "2.13.8"
ThisBuild / crossScalaVersions := Seq(Scala213)
ThisBuild / scalaVersion       := Scala213 // the default Scala

ThisBuild / scalacOptions ++= (if (tlIsScala3.value) Seq() else Seq("-language:implicitConversions", "-Xsource:3"))

lazy val root = tlCrossRootProject.aggregate(core, tests)

lazy val core = crossProject(JVMPlatform)
  .in(file("natchez-akka-http"))
  .settings(
    name        := "natchez-akka-http",
    description := "Integration for Natchez and Akka Http",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"    % "10.2.9" % Optional,
      "org.tpolecat"     %%% "natchez-core" % "0.1.6",
    ),
  )

lazy val tests = crossProject(JVMPlatform)
  .in(file("tests"))
  .enablePlugins(NoPublishPlugin)
  .dependsOn(core)
  .settings(
    name := "tests",
    libraryDependencies ++= Seq("org.scalacheck" %%% "scalacheck" % "1.16.0", "org.scalameta" %%% "munit" % "0.7.29")
      .map(_ % Test),
  )
