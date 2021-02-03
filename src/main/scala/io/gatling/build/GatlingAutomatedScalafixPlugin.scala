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

import scalafix.sbt.ScalafixPlugin
import scalafix.sbt.ScalafixPlugin.autoImport._

import sbt._
import sbt.Keys._

object GatlingAutomatedScalafixPlugin extends AutoPlugin {
  override def requires: Plugins = ScalafixPlugin && GatlingBuildConfigPlugin

  object autoImport {
    val gatlingScalafixCheck = taskKey[Unit]("Check that gatling scalafix rules have been applied")

    def automateScalafixBeforeCompile(configurations: Configuration*): Seq[Setting[_]] =
      configurations.foldLeft(List.empty[Setting[_]]) {
        _ ++ inConfig(_)(
          Seq(
            compile := compile.dependsOn(scalafix.toTask("").dependsOn(scalafixWriteConfigFile)).value
          )
        )
      }
  }

  import autoImport._
  import config.ConfigUtils._

  private lazy val scalafixConfigFileSetting = resourceOnConfigDirectoryPath(".scalafix.conf")
  private lazy val scalafixWriteConfigFile = writeResourceOnConfigDirectoryFile(
    path = "default.scalafix.conf",
    fileSetting = scalafixConfigFileSetting
  )

  override def projectSettings: Seq[sbt.Setting[_]] =
    automateScalafixBeforeCompile(Test, Compile) ++
      Seq(
        scalafixDependencies in ThisBuild += "com.nequissimus" %% "sort-imports" % "0.5.4",
        scalafixConfig := Some(scalafixConfigFileSetting.value),
        gatlingScalafixCheck := scalafixAll.toTask(" --check").dependsOn(scalafixWriteConfigFile).value
      )
}
