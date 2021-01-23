package io.gatling.build.config

import sbt._

object Keys {
  val gatlingBuildConfigDirectory = settingKey[File]("Location where to put configuration from gatling-build-plugin. Defaults to target/gatling-build-config")
}
