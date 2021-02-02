package io.gatling.build

import com.jsuereth.sbtpgp.PgpKeys.publishSigned

import sbtrelease.ReleasePlugin.autoImport._

import sbt.complete.DefaultParsers._
import sbt.complete.Parser

import io.gatling.build.release.GatlingReleaseProcess

import sbt.Keys._
import sbt._

object GatlingReleasePlugin extends AutoPlugin {
  override def requires: Plugins = GatlingPublishPlugin

  object autoImport {
    lazy val skipSnapshotDepsCheck = settingKey[Boolean]("Skip snapshot dependencies check during release")
  }

  import autoImport._

  private lazy val minorParser: Parser[GatlingReleaseProcess] =
    (Space ~> token("minor")) ^^^ GatlingReleaseProcess.Minor

  private lazy val patchParser: Parser[GatlingReleaseProcess] =
    (Space ~> token("patch")) ^^^ GatlingReleaseProcess.Patch

  private lazy val milestoneParser: Parser[GatlingReleaseProcess] =
    (Space ~> token("milestone")) ^^^ GatlingReleaseProcess.Milestone

  private lazy val releaseProcessParser: Parser[GatlingReleaseProcess] = minorParser | patchParser | milestoneParser

  def gatlingRelease =
    Command("gatling-release", ("gatling-release <minor|patch|milestone>", "release in Gatling way"), "release in Gatling way")(_ => releaseProcessParser) {
      (state, gatlingReleaseProcess) =>
        val extracted = Project.extract(state)
        val stateWithReleaseVersionBump = extracted.appendWithSession(
          Seq(
            releaseVersion := gatlingReleaseProcess.releaseVersion,
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
    commands += gatlingRelease
  )
}
