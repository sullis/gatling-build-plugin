package io.gatling.build.release

import io.gatling.build.GatlingPublishKeys.pushToPrivateNexus
import io.gatling.build.GatlingReleasePlugin.autoimport.skipSnapshotDepsCheck
import io.gatling.build.release.GatlingReleaseStep._

import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.Version.Bump

import xerial.sbt.Sonatype.SonatypeCommand.sonatypeReleaseAll

import sbt._
import sbt.Keys._

sealed trait GatlingReleaseProcess {
  def bump: Option[Bump]
  def releaseSteps: Def.Initialize[Seq[ReleaseStep]]
}
object GatlingReleaseProcess {
  private def checkSnapshotDeps = Def.setting {
    if (!skipSnapshotDepsCheck.value) checkSnapshotDependencies else noop
  }
  private def publishStep = Def.setting {
    ReleaseStep(releaseStepTaskAggregated(releasePublishArtifactsAction in Global in thisProjectRef.value))
  }
  private def sonatypeRelease = Def.setting {
    if (publishMavenStyle.value && !(pushToPrivateNexus ?? false).value) ReleaseStep(releaseStepCommand(sonatypeReleaseAll)) else noop
  }

  case object Minor extends GatlingReleaseProcess {
    override def bump: Option[Bump] = Some(Bump.Minor)

    override def toString: String = "minor"

    override def releaseSteps: Def.Initialize[Seq[ReleaseStep]] = Def.setting {
      Seq(
        checkSnapshotDeps.value,
        checkMinorVersion,
        inquireVersions,
        runClean,
        runTest,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        publishStep.value,
        pushChanges,
        createBugfixBranch,
        setNextVersion,
        commitNextVersion,
        pushChanges,
        sonatypeRelease.value
      )
    }
  }

  case object Patch extends GatlingReleaseProcess {
    override def bump: Option[Bump] = Some(Bump.Bugfix)

    override def toString: String = "patch"

    override def releaseSteps: Def.Initialize[Seq[ReleaseStep]] = Def.setting {
      Seq(
        checkSnapshotDeps.value,
        checkPatchVersion,
        inquireVersions,
        runClean,
        runTest,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        publishStep.value,
        pushChanges,
        setNextVersion,
        commitNextVersion,
        pushChanges,
        sonatypeRelease.value
      )
    }
  }

  case object Milestone extends GatlingReleaseProcess {
    override def bump: Option[Bump] = None

    override def toString: String = "milestone"

    override def releaseSteps: Def.Initialize[Seq[ReleaseStep]] = Def.setting {
      Seq(
        checkSnapshotDeps.value,
        setMilestoneReleaseVersion,
        inquireVersions,
        runClean,
        runTest,
        setReleaseVersion,
        tagRelease,
        publishStep.value,
        pushTagAndReset
      )
    }
  }
}
