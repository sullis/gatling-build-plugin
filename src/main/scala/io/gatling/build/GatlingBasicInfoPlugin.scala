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

import sbt._
import sbt.Keys._

object GatlingBasicInfoPlugin extends AutoPlugin {
  override def requires = empty

  override def trigger = allRequirements

  object autoImport {
    val githubPath = settingKey[String]("Project path on Github")

    val gatlingDevelopers = settingKey[Seq[GatlingDeveloper]]("List of contributors for this project")
    case class GatlingDeveloper(emailAddress: String, name: String, isGatlingCorp: Boolean)
  }

  import autoImport._

  override def projectSettings =
    Seq(
      homepage := Some(url("https://gatling.io")),
      organization := "io.gatling",
      organizationName := "Gatling Corp",
      organizationHomepage := Some(url("https://gatling.io")),
      scmInfo := Some(
        ScmInfo(
          url(s"https://github.com/${githubPath.value}"),
          s"scm:git:https://github.com/${githubPath.value}.git",
          s"scm:git:git@github.com/${githubPath.value}.git"
        )
      ),
      startYear := Some(2011),
      gatlingDevelopers := Seq.empty,
      pomExtra := developersXml(gatlingDevelopers.value)
    )

  private def developersXml(devs: Seq[GatlingDeveloper]) = {
    <developers>
      {
      for (dev <- devs) yield {
        <developer>
          <id>{dev.emailAddress}</id>
          <name>{dev.name}</name>
          {
          if (dev.isGatlingCorp) {
            <organization>Gatling Corp</organization>
            <organizationUrl>https://gatling.io</organizationUrl>
          }
        }
        </developer>
      }
    }
    </developers>
  }

}
