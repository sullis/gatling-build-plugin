package io.gatling.build.publish

import sbt._

object Repositories {
  private val DefaultRepositoryPrefix = "NEXUS_REPOSITORY"
  private val SbtHome = Path.userHome / ".sbt"
  private val PublicNexusCredentialsFile = SbtHome / ".credentials"

  private def publicNexusRepository(status: GatlingQualifier) =
    Resolver.sonatypeRepo(status.status)

  def repositoryFromEnv(prefix: String, qualifier: GatlingQualifier): Option[MavenRepository] =
    for {
      host <- sys.env.get(prefix + "_HOST")
      name = sys.env.getOrElse(prefix + "_NAME", "Unnamed repository")
      secure = sys.env.getOrElse(prefix + "_SECURE", "true").toBoolean
    } yield {
      sys.env.get(prefix + "_URL").map { name at _ withAllowInsecureProtocol !secure }.getOrElse {
        val scheme = if (secure) "https" else "http"
        s"$name (${qualifier.status})" at s"$scheme://$host/content/repositories/${qualifier.status}" withAllowInsecureProtocol !secure
      }
    }

  def nexusRepository(kind: GatlingQualifier): MavenRepository =
    repositoryFromEnv(DefaultRepositoryPrefix, kind) getOrElse publicNexusRepository(kind)

  def credentialsFromEnv(prefix: String): Option[Credentials] =
    for {
      realm <- sys.env.get(prefix + "_REALM")
      host <- sys.env.get(prefix + "_HOST")
      userName <- sys.env.get(prefix + "_USERNAME")
      passwd <- sys.env.get(prefix + "_PASSWORD")
    } yield Credentials(realm, host, userName, passwd)

  def credentials: Seq[Credentials] =
    Seq(
      credentialsFromEnv(DefaultRepositoryPrefix),
      Option(PublicNexusCredentialsFile).filter(_.exists).map(Credentials.apply)
    ).flatten
}
