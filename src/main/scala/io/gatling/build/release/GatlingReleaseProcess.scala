package io.gatling.build.release

import sbtrelease.Version.Bump

sealed trait GatlingReleaseProcess {
  def bump: Option[Bump]
}
object GatlingReleaseProcess {
  case object Minor extends GatlingReleaseProcess {
    override def bump: Option[Bump] = Some(Bump.Minor)
    override def toString: String = "minor"
  }
  case object Patch extends GatlingReleaseProcess {
    override def bump: Option[Bump] = Some(Bump.Bugfix)
    override def toString: String = "patch"
  }
  case object Milestone extends GatlingReleaseProcess {
    override def bump: Option[Bump] = None
    override def toString: String = "milestone"
  }
}
