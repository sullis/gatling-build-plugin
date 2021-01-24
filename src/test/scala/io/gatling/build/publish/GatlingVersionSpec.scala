package io.gatling.build.publish

import org.scalatest.matchers.should._
import org.scalatest.wordspec._
import sbtrelease.Version

class GatlingVersionSpec extends AnyWordSpec with Matchers {
  import io.gatling.build.publish.GatlingVersion._
  "A version" when {
    "minor" should {
      val version = Version("1.12.0").get

      "be a minor" in {
        version.isMinor shouldBe true
      }

      "not be a patch" in {
        version.isPatch shouldBe false
      }

      "have a branch name" in {
        version.branchName shouldBe "1.12"
      }
    }

    "patch" should {
      val version = Version("1.12.1").get

      "not be a minor" in {
        version.isMilestone shouldBe false
      }

      "be a patch" in {
        version.isPatch shouldBe true
      }

      "have a branch name" in {
        version.branchName shouldBe "1.12"
      }
    }
  }
}
