package io.gatling.build

import com.jsuereth.sbtpgp.PgpKeys.publishSigned

import sbtrelease.ReleasePlugin.autoImport._

import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.SonatypeKeys._

import sbt.complete.DefaultParsers._
import sbt.complete.Parser

import io.gatling.build.release.ReleaseProcessKeys._
import io.gatling.build.release.GatlingReleaseProcess

import sbt.Keys._
import sbt._

object GatlingReleasePlugin extends AutoPlugin {
  override def requires: Plugins = Sonatype && GatlingPublishPlugin

  object autoimport {
    lazy val gatlingReleaseProcessSetting = settingKey[GatlingReleaseProcess]("Gatling release process setting")
    lazy val skipSnapshotDepsCheck = settingKey[Boolean]("Skip snapshot dependencies check during release")
  }

  import autoimport._

  private lazy val releaseProcessParser: Parser[GatlingReleaseProcess] =
    Space ~> token(
      "minor" ^^^ GatlingReleaseProcess.Minor |
        "patch" ^^^ GatlingReleaseProcess.Patch |
        "milestone" ^^^ GatlingReleaseProcess.Milestone,
      description = "minor|patch|milestone"
    )

  def gatlingRelease = Command("gatling-release")(_ => releaseProcessParser) { (state, releaseProcess) =>
    val extracted = Project.extract(state)
    val stateWithReleaseVersionBump = extracted.appendWithSession(
      Seq(
        releaseVersionBump := releaseProcess.bump.getOrElse(releaseVersionBump.value),
        gatlingReleaseProcessSetting := releaseProcess
      ),
      state
    )

    Command.process("release with-defaults", stateWithReleaseVersionBump)
  }

  override def projectSettings: Seq[Setting[_]] = Seq(
    skipSnapshotDepsCheck := false,
    gatlingReleaseProcessSetting := GatlingReleaseProcess.Patch,
    releaseProcess := fullReleaseProcess,
    releaseCrossBuild := false,
    releasePublishArtifactsAction := publishSigned.value,
    sonatypeProfileName := "io.gatling",
    commands += gatlingRelease
  )
}
