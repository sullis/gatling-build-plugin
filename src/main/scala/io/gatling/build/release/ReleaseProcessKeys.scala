package io.gatling.build.release

import io.gatling.build.GatlingReleasePlugin.autoimport._
import io.gatling.build.GatlingPublishKeys.pushToPrivateNexus
import io.gatling.build.release.GatlingReleaseStep._

import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

import sbt.Keys._
import sbt._

import xerial.sbt.Sonatype.SonatypeCommand.sonatypeReleaseAll

object ReleaseProcessKeys {
  def fullReleaseProcess: Seq[ReleaseStep] = {
    val checkSnapshotDeps = if (!skipSnapshotDepsCheck.value) checkSnapshotDependencies else noop
    val publishStep = ReleaseStep(releaseStepTaskAggregated(releasePublishArtifactsAction in Global in thisProjectRef.value))
    val sonatypeRelease = if (publishMavenStyle.value && !(pushToPrivateNexus ?? false).value) ReleaseStep(releaseStepCommand(sonatypeReleaseAll)) else noop

    gatlingReleaseProcessSetting.value match {
      case GatlingReleaseProcess.Minor =>
        Seq(
          checkSnapshotDeps,
          checkMinorVersion,
          inquireVersions,
          runClean,
          runTest,
          setReleaseVersion,
          commitReleaseVersion,
          tagRelease,
          publishStep,
          pushChanges,
          createBugfixBranch,
          setNextVersion,
          commitNextVersion,
          pushChanges,
          sonatypeRelease
        )

      case GatlingReleaseProcess.Patch =>
        Seq(
          checkSnapshotDeps,
          checkPatchVersion,
          inquireVersions,
          runClean,
          runTest,
          setReleaseVersion,
          commitReleaseVersion,
          tagRelease,
          publishStep,
          pushChanges,
          setNextVersion,
          commitNextVersion,
          pushChanges,
          sonatypeRelease
        )

      case GatlingReleaseProcess.Milestone =>
        Seq(
          checkSnapshotDeps,
          setMilestoneReleaseVersion,
          inquireVersions,
          runClean,
          runTest,
          setReleaseVersion,
          tagRelease,
          publishStep,
          pushTagAndReset
        )
    }
  }
}
