package io.gatling.build.publish

sealed abstract class GatlingQualifier(val status: String)
object GatlingQualifier {
  case object Release extends GatlingQualifier("releases")
  case object Snapshot extends GatlingQualifier("snapshots")
  case object Milestone extends GatlingQualifier("milestones")
}
