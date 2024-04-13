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

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalafixScalaBinaryVersion := (if (tlIsScala3.value) "3.1" else "2.13")
