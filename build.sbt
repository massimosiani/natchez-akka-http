import laika.ast.Path.Root
import laika.ast.Styles
import laika.helium.config.{HeliumIcon, IconLink, ThemeNavigationSection, TextLink}
import Dependencies.versions._

ThisBuild / tlBaseVersion := "0.3"

ThisBuild / organization     := "io.github.massimosiani"
ThisBuild / organizationName := "Massimo Siani"
ThisBuild / licenses         := Seq(License.Apache2)
ThisBuild / developers       := List(
  tlGitHubDev("massimosiani", "Massimo Siani")
)

ThisBuild / tlCiHeaderCheck          := true
ThisBuild / tlCiScalafixCheck        := true
ThisBuild / tlCiScalafmtCheck        := true
ThisBuild / tlMimaPreviousVersions   := Set.empty // TODO: remove after release
ThisBuild / tlSiteIsTypelevelProject := None
ThisBuild / tlSitePublishBranch      := Some("main")
ThisBuild / tlSonatypeUseLegacyHost  := false

val Scala213 = "2.13.11"
ThisBuild / crossScalaVersions := Seq(Scala213)
ThisBuild / scalaVersion       := Scala213 // the default Scala

ThisBuild / scalacOptions ++= (if (tlIsScala3.value) Seq() else Seq("-language:implicitConversions", "-Xsource:3"))

lazy val root = tlCrossRootProject.aggregate(natchezAkkaHttp, natchezPekkoHttp, exampleTapir, exampleVanillaAkka, tests)

lazy val natchezAkkaHttp = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("natchez-akka-http"))
  .settings(
    name        := "natchez-akka-http",
    description := "Integration for Natchez and Akka Http",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor"   % akka     % Optional,
      "com.typesafe.akka" %% "akka-http"    % akkaHttp % Optional,
      "org.tpolecat"     %%% "natchez-core" % natchez,
    ),
  )

lazy val natchezPekkoHttp = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("natchez-pekko-http"))
  .settings(
    name        := "natchez-pekko-http",
    description := "Integration for Natchez and Pekko Http",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor"  % pekko,
      "org.apache.pekko" %% "pekko-http"   % pekkoHttp,
      "org.tpolecat"    %%% "natchez-core" % natchez,
    ),
  )

lazy val docs = project
  .in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .settings(
    laikaConfig ~= { _.withRawContent },
    tlSiteHelium     := tlSiteHelium
      .value
      .site
      .topNavigationBar(
        homeLink = IconLink.internal(Root / "natchez-akka-http.md", HeliumIcon.home),
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
      )
      .site
      .mainNavigation(appendLinks =
        Seq(
          ThemeNavigationSection(
            "akka-http",
            TextLink.external("https://doc.akka.io/docs/akka-http/current/index.html", "akka-http"),
          ),
          ThemeNavigationSection("pekko", TextLink.external("https://pekko.apache.org/", "pekko")),
          ThemeNavigationSection("natchez", TextLink.external("https://github.com/tpolecat/natchez", "natchez")),
          ThemeNavigationSection("tapir", TextLink.external("https://tapir.softwaremill.com/en/latest/", "tapir")),
        )
      ),
    tlSiteApiPackage := Some("natchez.akka.http"),
  )

lazy val exampleTapir = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("examples/tapir"))
  .enablePlugins(NoPublishPlugin)
  .dependsOn(natchezAkkaHttp)
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
  .crossType(CrossType.Pure)
  .in(file("examples/vanilla-akka"))
  .enablePlugins(NoPublishPlugin)
  .dependsOn(natchezAkkaHttp)
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
  .crossType(CrossType.Pure)
  .in(file("tests"))
  .enablePlugins(NoPublishPlugin)
  .dependsOn(natchezAkkaHttp)
  .settings(
    name := "tests",
    libraryDependencies ++= Seq("org.scalacheck" %%% "scalacheck" % scalacheck, "org.scalameta" %%% "munit" % munit)
      .map(_ % Test),
  )
