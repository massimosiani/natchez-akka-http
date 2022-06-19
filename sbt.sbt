addCommandAlias("fix", "; scalafmtAll; scalafmtSbt ; all compile:scalafix test:scalafix it:scalafix")
addCommandAlias(
  "fixCheck",
  "; scalafmtCheckAll; scalafmtSbtCheck ; compile:scalafix --check ; test:scalafix --check ; it:scalafix --check",
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
ThisBuild / scalafixDependencies += "org.typelevel"        %% "typelevel-scalafix" % "0.1.4"
ThisBuild / scalaVersion                                   := "2.13.8"
ThisBuild / semanticdbEnabled                              := true
ThisBuild / semanticdbVersion                              := scalafixSemanticdb.revision
ThisBuild / turbo                                          := true
