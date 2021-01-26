package io.gatling.build

import com.jsuereth.sbtpgp.PgpKeys.publishSigned

import sbtrelease.ReleasePlugin.autoImport._

import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.SonatypeKeys._

import sbt.complete.DefaultParsers._
import sbt.complete.Parser

import io.gatling.build.release.GatlingReleaseProcess

import sbt.Keys._
import sbt._

object GatlingReleasePlugin extends AutoPlugin {
  override def requires: Plugins = Sonatype && GatlingPublishPlugin

  object autoImport {
    lazy val skipSnapshotDepsCheck = settingKey[Boolean]("Skip snapshot dependencies check during release")
  }

  import autoImport._

  private lazy val releaseProcessParser: Parser[GatlingReleaseProcess] =
    Space ~> token(
      "minor" ^^^ GatlingReleaseProcess.Minor |
        "patch" ^^^ GatlingReleaseProcess.Patch |
        "milestone" ^^^ GatlingReleaseProcess.Milestone,
      description = "minor|patch|milestone"
    )

  def gatlingRelease = Command("gatling-release")(_ => releaseProcessParser) { (state, gatlingReleaseProcess) =>
    val extracted = Project.extract(state)
    val stateWithReleaseVersionBump = extracted.appendWithSession(
      Seq(
        releaseVersion := gatlingReleaseProcess.releaseVersion,
        releaseVersionBump := gatlingReleaseProcess.bump,
        releaseNextVersion := gatlingReleaseProcess.releaseNextVersion,
        releaseProcess := gatlingReleaseProcess.releaseSteps.value
      ),
      state
    )

    Command.process("release with-defaults", stateWithReleaseVersionBump)
  }

  override def projectSettings: Seq[Setting[_]] = Seq(
    skipSnapshotDepsCheck := false,
    releaseCrossBuild := false,
    releasePublishArtifactsAction := publishSigned.value,
    sonatypeProfileName := "io.gatling",
    commands += gatlingRelease
  )
}
