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

import io.gatling.build.license._
import io.gatling.build.publish.GatlingVersion

import com.jsuereth.sbtpgp.PgpKeys.publishSigned
import de.heikoseeberger.sbtheader.AutomateHeaderPlugin
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.headerLicense
import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport.{ releasePublishArtifactsAction, releaseStepCommandAndRemaining }
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.autoImport.sonatypePublishTo

object GatlingOssPlugin extends AutoPlugin {
  override def requires =
    GatlingAutomatedScalafixPlugin &&
      GatlingAutomatedScalafmtPlugin &&
      GatlingBasicInfoPlugin &&
      GatlingCompilerSettingsPlugin &&
      GatlingPublishPlugin &&
      GatlingReleasePlugin &&
      AutomateHeaderPlugin &&
      Sonatype

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    headerLicense := ApacheV2License,
    publishTo := {
      if (GatlingVersion(version.value).exists(_.isMilestone)) {
        publishTo.value
      } else {
        sonatypePublishTo.value
      }
    },
    releasePublishArtifactsAction := publishArtifactsAction.value
  )

  val publishArtifactsAction = Def.taskDyn {
    /*
     * Issues:
     *  - sbt-sonatype plugin only declares commands (not tasks)
     *  - sonatypeOpen command calls appendWithoutSession, and release version is reset to its -SNAPSHOT
     *
     * Workaround:
     *  - retrieve sonatypePublishTo settings after applying sonatypeOpen command
     *  - inject it to the state needed by publishSigned task
     *  - call sonatypeClose command with full state from sonatypeOpen
     */
    val startState = state.value
    val log = streams.value.log
    Def.task {
      log.info(s"Opening sonatype staging")
      val sonatypeOpenState = releaseStepCommandAndRemaining("sonatypeOpen")(startState)
      log.info("compile, package, sign and publish")
      val extracted = Project.extract(startState)
      val ref = extracted.get(thisProjectRef)
      Def.unit(
        extracted.runAggregated(
          publishSigned in Global in ref,
          startState.appendWithSession(
            Seq(
              sonatypePublishTo := sonatypeOpenState.getSetting(sonatypePublishTo).get
            )
          )
        )
      )
      log.info("Closing sonatype staging")
      Def.unit(releaseStepCommandAndRemaining("sonatypeClose")(sonatypeOpenState))
    }
  }
}
