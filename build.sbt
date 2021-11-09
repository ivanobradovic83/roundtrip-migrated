import com.typesafe.sbt.packager.SettingsHelper.makeDeploymentSettings
import ReleaseTransformations._

import com.typesafe.sbt.packager.docker.DockerPermissionStrategy

name := "sdu-cwc-roundtrip-publishone"

organization := "nl.sdu.cwc"

scalaVersion := "2.13.4"

resolvers ++= Seq(
  "CWC Releases" at "https://artifactory.k8s.awssdu.nl/artifactory/cwc-releases",
  "CWC Snapshots" at "https://artifactory.k8s.awssdu.nl/artifactory/cwc-snapshots"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(DebianPlugin)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(SbtWeb)
  .enablePlugins(UniversalDeployPlugin)
  .enablePlugins(JDebPackaging)
  .enablePlugins(SystemVPlugin)
  .enablePlugins(DockerPlugin)
  .enablePlugins(GitVersioning)

libraryDependencies ++= Seq(
  guice,
  ws,
  "nl.sdu.cwc" %% "cwc-common" % "0.4.15-play28",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  "org.webjars" % "bootstrap" % "4.4.1-1",
  "org.webjars" % "font-awesome" % "5.12.0",
  "org.webjars" % "jquery-ui" % "1.12.1",
  "org.webjars" %% "webjars-play" % "2.8.0-1",
  "de.leanovate.play-mockws" %% "play-mockws" % "2.8.0" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
)

buildInfoKeys := Seq[BuildInfoKey](name, scalaVersion, sbtVersion)

buildInfoKeys += BuildInfoKey.map(version) {
  case (k, v) =>
    val sha = git.gitHeadCommit.value.map(_.substring(0, 6))
    k -> (v + " " + sha.getOrElse("local build"))
}

buildInfoPackage := "build"

// Releasing
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

TwirlKeys.templateImports += "views.alerts.Alert"

// Debian packaging
makeDeploymentSettings(Debian, packageBin in Debian, "deb")
packageSummary in Debian := "CWC Roundtrip PublishOne"
packageDescription in Debian := "CWC Roundtrip PublishOne"
maintainer in Debian := "CWC Team <cwcteam@sdu.nl>"
daemonUser in Linux := "cwcdownloader"

// Publishing
publishTo := {
  val artifactory = "https://artifactory.k8s.awssdu.nl/artifactory/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at artifactory + "cwc-snapshots")
  else
    Some("releases" at artifactory + "cwc-releases")
}

makeDeploymentSettings(Debian, packageBin in Debian, "deb")

publish in Debian := (publish in Debian).triggeredBy(publish in Compile).value

// Docker
dockerBaseImage := "117533191630.dkr.ecr.eu-west-1.amazonaws.com/upstream-fork/openjdk:8"
dockerExposedPorts := Seq(9081)
dockerExposedVolumes := Seq("/data/event-logs")
dockerEntrypoint := Seq("bin/sdu-cwc-roundtrip-publishone",
                        "-Dconfig.file=/etc/sdu-cwc-roundtrip-publishone/startup.conf",
                        "-Dlogger.file=conf/docker-logger.xml",
                        "-Dhttp.port=9081")
dockerRepository := Some("117533191630.dkr.ecr.eu-west-1.amazonaws.com")
dockerUsername := Some("cwc")
dockerUpdateLatest := false
daemonUserUid in Docker := None
daemonUser in Docker := "daemon"
dockerEnvVars := Map("TZ" -> "Europe/Amsterdam")
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown

//Coverage report
coverageExcludedPackages := """<empty>;Reverse.*;controllers;router.Routes.*;Application.*;build;views.html"""
