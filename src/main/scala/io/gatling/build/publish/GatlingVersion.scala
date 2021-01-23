package io.gatling.build.publish

import sbtrelease.Version

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.Try

object GatlingVersion {

  lazy val MilestoneQualifierPrefix = "-M"
  lazy val MilestoneFormatterPattern = "yyyyMMddhhmmss"
  lazy val MilestoneFormatter = new SimpleDateFormat(MilestoneFormatterPattern)

  implicit class GatlingVersion(version: Version) {
    def isMilestone: Boolean =
      version.qualifier.exists { qualifier =>
        qualifier.length == MilestoneFormatterPattern.length + MilestoneQualifierPrefix.length &&
          qualifier.startsWith(MilestoneQualifierPrefix) &&
          Try(MilestoneFormatter.parse(qualifier.substring(MilestoneQualifierPrefix.length))).isSuccess
      }

    def asMilestone: Version = {
      val qualifier = MilestoneQualifierPrefix + MilestoneFormatter.format(new Date())
      version.copy(qualifier = Some(qualifier))
    }
  }
}
