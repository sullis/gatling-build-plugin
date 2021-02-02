package io.gatling.build.release

import io.gatling.build.GatlingReleasePlugin.autoImport._
import io.gatling.build.release.GatlingReleaseStep._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import sbt._
import sbt.Keys._

sealed trait GatlingReleaseProcess {
  def releaseSteps: Def.Initialize[Seq[ReleaseStep]]
  def releaseVersion: String => String = gatlingVersion(_.withoutQualifier)
  def releaseNextVersion: String => String
}

object GatlingReleaseProcess {
  private def checkSnapshotDeps = Def.setting {
    if (!skipSnapshotDepsCheck.value) checkSnapshotDependencies else noop
  }
  private def publishStep = Def.setting {
    // releaseP
    ReleaseStep(releaseStepTaskAggregated(releasePublishArtifactsAction in Global in thisProjectRef.value))
  }

  case object Minor extends GatlingReleaseProcess {
    override def releaseNextVersion: String => String = gatlingVersion(_.bumpMinor.asSnapshot)

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
        writeCurrentVersion,
        pushChanges,
        createBugfixBranch,
        setNextVersion,
        commitNextVersion,
        pushChanges
      )
    }
  }

  case object Patch extends GatlingReleaseProcess {
    override def releaseNextVersion: String => String = gatlingVersion(_.bumpPatch.asSnapshot)

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
        writeCurrentVersion,
        pushChanges,
        setNextVersion,
        commitNextVersion,
        pushChanges
      )
    }
  }

  case object Milestone extends GatlingReleaseProcess {
    override def releaseVersion: String => String = gatlingVersion(_.asMilestone)
    override def releaseNextVersion: String => String = gatlingVersion(_.asSnapshot)
    override def toString: String = "milestone"

    override def releaseSteps: Def.Initialize[Seq[ReleaseStep]] = Def.setting {
      Seq(
        checkSnapshotDeps.value,
        inquireVersions,
        runClean,
        runTest,
        setReleaseVersion,
        tagRelease,
        publishStep.value,
        writeCurrentVersion,
        pushChanges,
        setNextVersion
      )
    }
  }
}
