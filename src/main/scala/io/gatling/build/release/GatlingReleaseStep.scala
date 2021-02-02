package io.gatling.build.release

import io.gatling.build.publish.GatlingVersion
import sbtrelease.ReleasePlugin.autoImport.ReleaseKeys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.Utilities._
import sbt.Keys._
import sbt._
import sbtrelease.{ versionFormatError, Git }

import scala.sys.process.ProcessLogger

object GatlingReleaseStep {
  val noop: ReleaseStep = ReleaseStep(identity)

  def gatlingVersion(transform: GatlingVersion => GatlingVersion = identity): String => String = { ver =>
    GatlingVersion(ver).map(transform(_).string).getOrElse(versionFormatError(ver))
  }

  lazy val createBugfixBranch: ReleaseStep = { st: State =>
    val masterVersions = st.get(versions).getOrElse(sys.error("Versions should have already been inquired"))
    val extracted = st.extract
    val git = extractGitVcs(extracted)

    val rawModuleVersion = extracted.get(version)
    val moduleVersion = GatlingVersion(rawModuleVersion).getOrElse(versionFormatError(rawModuleVersion))
    val branchName = moduleVersion.branchName

    val minorBugFixBranchState = extracted.appendWithSession(
      Seq(
        releaseVersion := gatlingVersion(_.withoutQualifier),
        releaseNextVersion := gatlingVersion(_.bumpPatch.asSnapshot)
      ),
      st
    )
    val inquireBugFixState = inquireVersions.action(minorBugFixBranchState)
    val bugFixState = setNextVersion.action(inquireBugFixState)

    val gitLog = stdErrorToStdOut(st.log) // Git outputs to standard error, so use a logger that redirects stderr to info
    val originBranch = git.currentBranch
    git.cmd("checkout", "-b", branchName) ! gitLog

    val committedBugFixState = commitNextVersion(bugFixState)

    git.cmd("push", "--set-upstream", "origin", s"$branchName:$branchName") ! gitLog

    git.cmd("checkout", originBranch) ! gitLog
    committedBugFixState.put(versions, masterVersions)
  }

  lazy val writeCurrentVersion: ReleaseStep = { st: State =>
    IO.write(st.extract.get(target) / "release-info", st.extract.get(version))
    st
  }

  lazy val checkMinorVersion: ReleaseStep = checkVersionStep(
    _.isPatch,
    version => s"Cannot release a minor version when current version is patch (${version.string})"
  )

  lazy val checkPatchVersion: ReleaseStep = checkVersionStep(
    _.isMinor,
    version => s"Cannot release a patch version when current version is minor (${version.string})"
  )

  private def extractGitVcs(extracted: Extracted): Git =
    extracted
      .get(releaseVcs)
      .collect { case git: Git => git }
      .getOrElse(sys.error("Aborting release. Working directory is not a Git repository."))

  private def checkVersionStep(validate: GatlingVersion => Boolean, error: GatlingVersion => String): ReleaseStep =
    ReleaseStep(
      identity,
      check = { st: State =>
        val rawCurrentVersion = st.extract.get(version)
        val currentVersion = GatlingVersion(rawCurrentVersion).getOrElse(sys.error(s"Invalid version format $rawCurrentVersion"))
        if (validate(currentVersion)) {
          sys.error(error(currentVersion))
        }
        st
      }
    )

  private def stdErrorToStdOut(delegate: ProcessLogger): ProcessLogger = new ProcessLogger {
    override def out(s: => String): Unit = delegate.out(s)
    override def err(s: => String): Unit = delegate.out(s)
    override def buffer[T](f: => T): T = delegate.buffer(f)
  }
}
