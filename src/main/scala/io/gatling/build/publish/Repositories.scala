package io.gatling.build.publish

import scala.collection.JavaConverters._

import java.util.Properties

import sbt._

object Repositories {
  private val SbtHome = Path.userHome / ".sbt"
  private val PublicNexusCredentialsFile = SbtHome / ".credentials"
  private val PrivateNexusCredentialsFile = SbtHome / ".private-nexus-credentials"

  private val PrivateNexusRepositoriesRoot = {
    val props = new Properties
    IO.load(props, PrivateNexusCredentialsFile)
    val propsAsMap = props.asScala.map { case (k, v) => (k, v.trim) }
    for {
      scheme <- propsAsMap.get("scheme")
      host <- propsAsMap.get("host")
    } yield s"$scheme://$host/content/repositories"
  }

  private def publicNexusRepository(status: GatlingQualifier) =
    Resolver.sonatypeRepo(status.status)

  private def privateNexusRepository(status: GatlingQualifier) =
    PrivateNexusRepositoriesRoot.map(s"Private Nexus ${status.status.capitalize}" at _ + s"/${status.status}/" withAllowInsecureProtocol true)

  def nexusRepository(kind: GatlingQualifier, usePrivate: Boolean): Option[MavenRepository] =
    if (usePrivate) privateNexusRepository(kind) else Some(publicNexusRepository(kind))

  def credentials(usePrivate: Boolean): Credentials =
    Credentials(if (usePrivate) PrivateNexusCredentialsFile else PublicNexusCredentialsFile)
}
