addCommandAlias("fix", "; all compile:scalafix test:scalafix it:scalafix; scalafmtAll; scalafmtSbt")
addCommandAlias(
  "fixCheck",
  "; compile:scalafix --check ; test:scalafix --check ; it:scalafix --check; scalafmtCheckAll; scalafmtSbtCheck",
)
addCommandAlias(
  "up2date",
  "reload plugins; dependencyUpdates; reload return; dependencyUpdates",
)
addCommandAlias(
  "coverAll",
  "coverage; test; it:test; coverageAggregate",
)

Global / onChangedBuildSource       := ReloadOnSourceChanges
Test / turbo                        := true
IntegrationTest / parallelExecution := false

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports"   % "0.6.0"
ThisBuild / scalafixDependencies += "org.typelevel"        %% "typelevel-scalafix" % "0.1.5"
ThisBuild / scalafixScalaBinaryVersion                     := (if (tlIsScala3.value) "3.1" else "2.13")
ThisBuild / semanticdbEnabled                              := true
ThisBuild / semanticdbVersion                              := scalafixSemanticdb.revision
ThisBuild / turbo                                          := true
