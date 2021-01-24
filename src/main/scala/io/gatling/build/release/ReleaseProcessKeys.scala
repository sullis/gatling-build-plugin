package io.gatling.build.release

import io.gatling.build.GatlingReleasePlugin.autoimport._
import io.gatling.build.GatlingPublishKeys.pushToPrivateNexus
import io.gatling.build.release.GatlingReleaseStep._

import sbt.Keys._
import sbt._

import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

import xerial.sbt.Sonatype.SonatypeCommand.sonatypeReleaseAll

object ReleaseProcessKeys {

  val gatlingReleaseSettings = Seq(
    skipSnapshotDepsCheck := false,
    gatlingReleaseProcessSetting := GatlingReleaseProcess.Patch,
    releaseProcess := {
      val releaseOnSonatype = publishMavenStyle.value && !(pushToPrivateNexus ?? false).value
      fullReleaseProcess(thisProjectRef.value, skipSnapshotDepsCheck.value, releaseOnSonatype, gatlingReleaseProcessSetting.value)
    }
  )

  private def fullReleaseProcess(
      ref: ProjectRef,
      skipSnapshotDepsCheck: Boolean,
      releaseOnSonatype: Boolean,
      gatlingReleaseProcess: GatlingReleaseProcess
  ): Seq[ReleaseStep] = {
    val checkSnapshotDeps = if (!skipSnapshotDepsCheck) checkSnapshotDependencies else noop
    val publishStep = ReleaseStep(releaseStepTaskAggregated(releasePublishArtifactsAction in Global in ref))
    val sonatypeRelease = if (releaseOnSonatype) ReleaseStep(releaseStepCommand(sonatypeReleaseAll)) else noop

    gatlingReleaseProcess match {
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
