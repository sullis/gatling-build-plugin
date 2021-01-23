package io.gatling.build.config

import io.gatling.build.config.Keys._

import sbt._

import scala.io.Source

object ConfigUtils {
  def resourceOnConfigDirectoryPath(name: String): Def.Initialize[File] = Def.setting {
    gatlingBuildConfigDirectory.value / name
  }

  def writeResourceOnConfigDirectoryFile(path: String, fileSetting: Def.Initialize[File]): Def.Initialize[Task[File]] = Def.task {
    val file = fileSetting.value
    val resourceUrl = getClass.getResource(s"/$path")
    val resource = Source.fromURL(resourceUrl)
    resource.getLines.foreach { line => IO.write(file, s"$line\n", IO.defaultCharset, append = true) }
    resource.close()
    file
  }
}
