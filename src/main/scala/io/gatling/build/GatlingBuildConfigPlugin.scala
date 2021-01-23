package io.gatling.build

import sbt._
import sbt.Keys._

object GatlingBuildConfigPlugin extends AutoPlugin {
  override def requires = empty

  import config.Keys._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    gatlingBuildConfigDirectory := target.value / "gatling-build-config"
  )
}
