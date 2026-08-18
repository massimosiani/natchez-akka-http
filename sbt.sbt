addCommandAlias("fix", "; Compile / scalafix; Test / scalafix; scalafmtAll; scalafmtSbt")
addCommandAlias(
  "fixCheck",
  "; Compile / scalafix --check; Test / scalafix --check; scalafmtCheckAll; scalafmtSbtCheck",
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
