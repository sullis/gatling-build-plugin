lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "gatling-build-plugin",
    homepage := Some(new URL("https://gatling.io")),
    organization := "io.gatling",
    organizationHomepage := Some(new URL("https://gatling.io")),
    startYear := Some(2011),
    licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    scalacOptions := Seq(
      "-encoding",
      "UTF-8",
      "-target:jvm-1.8",
      "-deprecation",
      "-feature",
      "-unchecked"
    ) ++ (if (scala.util.Properties.javaVersion.startsWith("1.8")) Nil else Seq("-release", "8")),
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
// ------------------ //
// -- DEPENDENCIES -- //
// ------------------ //
    addSbtPlugin("org.scalameta"     % "sbt-scalafmt" % "2.3.4"),
    addSbtPlugin("ch.epfl.scala"     % "sbt-scalafix" % "0.9.23"),
    addSbtPlugin("com.github.gseitz" % "sbt-release"  % "1.0.13"),
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"      % "2.0.2"),
    addSbtPlugin("de.heikoseeberger" % "sbt-header"   % "5.4.0"),
    addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype" % "2.0"),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % Test
  )
