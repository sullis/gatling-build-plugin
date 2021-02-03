scalaVersion := "2.12.3"
githubPath := "user/repository"

lazy val root = (project in file("."))
  .enablePlugins(automated.GatlingAutomatedScalafixPlugin)
  .settings(
    version := "0.1",
    name := "scalafix/automated"
  )
