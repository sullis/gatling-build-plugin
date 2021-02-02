/*
 * Copyright 2011-2021 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.build

import io.gatling.build.config.ConfigUtils._

import org.scalafmt.sbt.ScalafmtPlugin
import org.scalafmt.sbt.ScalafmtPlugin.autoImport.{ scalafmtConfig, scalafmtOnCompile }
import sbt._

object GatlingAutomatedScalafmtPlugin extends AutoPlugin {

  override def requires: Plugins = ScalafmtPlugin && GatlingBuildConfigPlugin

  override def globalSettings: Seq[Def.Setting[_]] = Seq(
    scalafmtOnCompile := true
  )

  private lazy val scalafmtConfigFileSetting = resourceOnConfigDirectoryPath(".scalafmt.conf")
  private lazy val scalafmtWriteConfigFile = writeResourceOnConfigDirectoryFile(
    path = "default.scalafmt.conf",
    fileSetting = scalafmtConfigFileSetting
  )

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    scalafmtConfig := scalafmtWriteConfigFile.value
  )
}
