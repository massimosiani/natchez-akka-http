import laika.ast.Path.Root
import laika.ast.Styles
import laika.helium.config.{HeliumIcon, IconLink}

ThisBuild / tlBaseVersion := "0.2"

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

lazy val root = tlCrossRootProject.aggregate(core, exampleTapir, exampleVanillaAkka, tests)

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

lazy val docs = project
  .in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .settings(
    laikaConfig ~= { _.withRawContent },
    tlSiteHeliumConfig    := tlSiteHeliumConfig
      .value
      .site
      .topNavigationBar(
        homeLink = IconLink.internal(Root / "index.md", HeliumIcon.home),
        navLinks = tlSiteApiUrl.value.toList.map { url =>
          IconLink.external(
            url.toString,
            HeliumIcon.api,
            options = Styles("svg-link"),
          )
        } ++ List(
          IconLink.external(
            scmInfo.value.fold("https://github.com/massimosiani")(_.browseUrl.toString),
            HeliumIcon.github,
            options = Styles("svg-link"),
          )
        ),
      ),
    tlSiteApiPackage      := Some("natchez.akka.http"),
    tlSiteRelatedProjects := Seq(
      "akka-http" -> url("https://doc.akka.io/docs/akka-http/current/index.html"),
      "natchez"   -> url("https://github.com/tpolecat/natchez"),
      "tapir"     -> url("https://tapir.softwaremill.com/en/latest/"),
    ),
  )

lazy val exampleTapir = crossProject(JVMPlatform)
  .in(file("examples/tapir"))
  .enablePlugins(NoPublishPlugin)
  .dependsOn(core)
  .settings(
    name := "tapir example",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %%% "tapir-akka-http-server" % "1.0.1",
      "com.softwaremill.sttp.tapir" %%% "tapir-core"             % "1.0.1",
      "com.softwaremill.sttp.tapir" %%% "tapir-cats"             % "1.0.1",
      "org.apache.logging.log4j"      % "log4j-api"              % "2.18.0",
      "org.apache.logging.log4j"      % "log4j-core"             % "2.18.0",
      "org.apache.logging.log4j"      % "log4j-slf4j-impl"       % "2.18.0",
      "org.tpolecat"                %%% "natchez-log"            % "0.1.6",
      "org.typelevel"               %%% "cats-effect"            % "3.3.13",
      "org.typelevel"               %%% "log4cats-slf4j"         % "2.3.2",
    ),
  )

lazy val exampleVanillaAkka = crossProject(JVMPlatform)
  .in(file("examples/vanilla-akka"))
  .enablePlugins(NoPublishPlugin)
  .dependsOn(core)
  .settings(
    name := "vanilla akka http example",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"       %% "akka-actor"       % "2.6.19",
      "com.typesafe.akka"       %% "akka-stream"      % "2.6.19",
      "com.typesafe.akka"       %% "akka-http"        % "10.2.9",
      "org.apache.logging.log4j" % "log4j-api"        % "2.18.0",
      "org.apache.logging.log4j" % "log4j-core"       % "2.18.0",
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.18.0",
      "org.tpolecat"           %%% "natchez-log"      % "0.1.6",
      "org.typelevel"          %%% "cats-effect"      % "3.3.13",
      "org.typelevel"          %%% "log4cats-slf4j"   % "2.3.2",
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
