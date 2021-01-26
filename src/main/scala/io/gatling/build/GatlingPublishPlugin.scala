package io.gatling.build

import scala.util.Properties._
import io.gatling.build.publish.GatlingQualifier
import io.gatling.build.publish.GatlingVersion._
import io.gatling.build.publish.Repositories
import sbtrelease.Version
import sbt._
import sbt.Keys._

object GatlingPublishPlugin extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin

  object autoImport {
    val githubPath = settingKey[String]("Project path on Github")
    val projectDevelopers = settingKey[Seq[GatlingDeveloper]]("List of contributors for this project")
    val gatlingPublishAddSonatypeResolvers = settingKey[Boolean]("Use Sonatype repositories for CI or during release process")
    val gatlingPublishToPrivateNexus = settingKey[Boolean]("Should this project's artifacts be pushed to our private Nexus ?")
    val isMilestone = settingKey[Boolean]("Indicate if release process is milestone")

    case class GatlingDeveloper(emailAddress: String, name: String, isGatlingCorp: Boolean)
  }

  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    gatlingPublishAddSonatypeResolvers := false,
    crossPaths := false,
    gatlingPublishToPrivateNexus := isMilestone.value,
    publishTo := Some(Repositories.nexusRepository(GatlingQualifier(version.value))),
    isMilestone := version(Version(_).exists(_.isMilestone)).value,
    pomExtra := mavenScmBlock(githubPath.value) ++ developersXml(projectDevelopers.value),
    resolvers ++= (if (gatlingPublishAddSonatypeResolvers.value) sonatypeRepositories else Seq.empty) :+ Resolver.mavenLocal,
    credentials ++= Repositories.credentials
  )

  private def sonatypeRepositories: Seq[Resolver] =
    Seq(
      envOrNone("CI").map(_ => Opts.resolver.sonatypeSnapshots),
      propOrNone("release").map(_ => Opts.resolver.sonatypeReleases)
    ).flatten

  private def mavenScmBlock(githubPath: String) =
    <scm>
      <connection>scm:git:git@github.com:{githubPath}.git</connection>
      <developerConnection>scm:git:git@github.com:{githubPath}.git</developerConnection>
      <url>https://github.com/{githubPath}</url>
      <tag>HEAD</tag>
    </scm>

  private def developersXml(devs: Seq[GatlingDeveloper]) = {
    <developers>
      {
      for (dev <- devs) yield {
        <developer>
            <id>{dev.emailAddress}</id>
            <name>{dev.name}</name>
            {if (dev.isGatlingCorp) <organization>Gatling Corp</organization>}
        </developer>
      }
    }
    </developers>
  }
}
