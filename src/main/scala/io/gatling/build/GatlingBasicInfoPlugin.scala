package io.gatling.build

import sbt._
import sbt.Keys._

object GatlingBasicInfoPlugin extends AutoPlugin {
  override def requires = empty

  override def trigger = allRequirements

  object autoImport {
    val githubPath = settingKey[String]("Project path on Github")

    val gatlingDevelopers = settingKey[Seq[GatlingDeveloper]]("List of contributors for this project")
    case class GatlingDeveloper(emailAddress: String, name: String, isGatlingCorp: Boolean)
  }

  import autoImport._

  override def projectSettings =
    Seq(
      homepage := Some(url("https://gatling.io")),
      organization := "io.gatling",
      organizationName := "Gatling Corp",
      organizationHomepage := Some(url("https://gatling.io")),
      scmInfo := Some(
        ScmInfo(
          url(s"https://github.com/${githubPath.value}"),
          s"scm:git:https://github.com/${githubPath.value}.git",
          s"scm:git:git@github.com/${githubPath.value}.git"
        )
      ),
      startYear := Some(2011),
      gatlingDevelopers := Seq.empty,
      pomExtra := developersXml(gatlingDevelopers.value)
    )

  private def developersXml(devs: Seq[GatlingDeveloper]) = {
    <developers>
      {
      for (dev <- devs) yield {
        <developer>
          <id>{dev.emailAddress}</id>
          <name>{dev.name}</name>
          {
          if (dev.isGatlingCorp) {
            <organization>Gatling Corp</organization>
            <organizationUrl>https://gatling.io</organizationUrl>
          }
        }
        </developer>
      }
    }
    </developers>
  }

}
