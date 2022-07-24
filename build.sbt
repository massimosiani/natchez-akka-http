import laika.ast.Path.Root
import laika.ast.Styles
import laika.helium.config.{HeliumIcon, IconLink}
import Dependencies.versions._

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
      "com.typesafe.akka" %% "akka-http"    % akkaHttp % Optional,
      "org.tpolecat"     %%% "natchez-core" % natchez,
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
      "com.softwaremill.sttp.tapir" %%% "tapir-akka-http-server" % tapir,
      "com.softwaremill.sttp.tapir" %%% "tapir-core"             % tapir,
      "com.softwaremill.sttp.tapir" %%% "tapir-cats"             % tapir,
      "org.apache.logging.log4j"      % "log4j-api"              % log4j,
      "org.apache.logging.log4j"      % "log4j-core"             % log4j,
      "org.apache.logging.log4j"      % "log4j-slf4j-impl"       % log4j,
      "org.tpolecat"                %%% "natchez-log"            % natchez,
      "org.typelevel"               %%% "cats-effect"            % catsEffect,
      "org.typelevel"               %%% "log4cats-slf4j"         % log4cats,
    ),
  )

lazy val exampleVanillaAkka = crossProject(JVMPlatform)
  .in(file("examples/vanilla-akka"))
  .enablePlugins(NoPublishPlugin)
  .dependsOn(core)
  .settings(
    name := "vanilla akka http example",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"       %% "akka-actor"       % akka,
      "com.typesafe.akka"       %% "akka-stream"      % akka,
      "com.typesafe.akka"       %% "akka-http"        % akkaHttp,
      "org.apache.logging.log4j" % "log4j-api"        % log4j,
      "org.apache.logging.log4j" % "log4j-core"       % log4j,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4j,
      "org.tpolecat"           %%% "natchez-log"      % natchez,
      "org.typelevel"          %%% "cats-effect"      % catsEffect,
      "org.typelevel"          %%% "log4cats-slf4j"   % log4cats,
    ),
  )

lazy val tests = crossProject(JVMPlatform)
  .in(file("tests"))
  .enablePlugins(NoPublishPlugin)
  .dependsOn(core)
  .settings(
    name := "tests",
    libraryDependencies ++= Seq("org.scalacheck" %%% "scalacheck" % scalacheck, "org.scalameta" %%% "munit" % munit)
      .map(_ % Test),
  )
