package io.gatling.build

import scala.util.Properties._
import io.gatling.build.publish.GatlingVersion
import sbt._
import sbt.Keys._

object GatlingPublishPlugin extends AutoPlugin {
  override def requires: Plugins = plugins.JvmPlugin

  object autoImport {
    val gatlingPublishAddSonatypeResolvers = settingKey[Boolean]("Use Sonatype repositories for CI or during release process")
    val isMilestone = settingKey[Boolean]("Indicate if release process is milestone")
  }

  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    publishMavenStyle := true,
    gatlingPublishAddSonatypeResolvers := false,
    crossPaths := false,
    isMilestone := version(GatlingVersion(_).exists(_.isMilestone)).value,
    resolvers ++= (if (gatlingPublishAddSonatypeResolvers.value) sonatypeRepositories else Seq.empty) :+ Resolver.mavenLocal
  )

  private def sonatypeRepositories: Seq[Resolver] =
    Seq(
      envOrNone("CI").map(_ => Opts.resolver.sonatypeSnapshots),
      propOrNone("release").map(_ => Opts.resolver.sonatypeReleases)
    ).flatten
}
