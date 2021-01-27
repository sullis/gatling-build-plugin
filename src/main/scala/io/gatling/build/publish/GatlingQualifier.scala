package io.gatling.build.publish

sealed abstract class GatlingQualifier(val status: String)
object GatlingQualifier {
  def apply(version: String): GatlingQualifier =
    GatlingVersion(version) match {
      case Some(ver) if ver.isSnapshot  => Snapshot
      case Some(ver) if ver.isMilestone => Milestone
      case _                            => Release
    }

  case object Release extends GatlingQualifier("releases")
  case object Snapshot extends GatlingQualifier("snapshots")
  case object Milestone extends GatlingQualifier("milestones")
}
