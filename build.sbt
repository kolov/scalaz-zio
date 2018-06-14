import Scalaz._

organization in ThisBuild := "org.scalaz"

version in ThisBuild := "0.1-SNAPSHOT"

publishTo in ThisBuild := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

dynverSonatypeSnapshots in ThisBuild := true

lazy val sonataCredentials = for {
  username <- sys.env.get("SONATYPE_USERNAME")
  password <- sys.env.get("SONATYPE_PASSWORD")
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)

credentials in ThisBuild ++= sonataCredentials.toSeq

lazy val root = project
  .in(file("."))
  .settings(
    skip in publish := true
  )
  .aggregate(coreJVM, coreJS, benchmarks, docs)
  .enablePlugins(ScalaJSPlugin)

lazy val core = crossProject
  .in(file("core"))
  .settings(stdSettings("zio"))
  .settings(
    libraryDependencies ++= Seq("org.specs2" %%% "specs2-core"          % "4.2.0" % Test,
                                "org.specs2" %%% "specs2-scalacheck"    % "4.2.0" % Test,
                                "org.specs2" %%% "specs2-matcher-extra" % "4.2.0" % Test),
    scalacOptions in Test ++= Seq("-Yrangepos")
  )

lazy val coreJVM = core.jvm

lazy val coreJS = core.js

lazy val benchmarks = project.module
  .dependsOn(coreJVM)
  .enablePlugins(JmhPlugin)
  .settings(
    skip in publish := true,
    libraryDependencies ++=
      Seq(
        "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
        "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided,
        "io.monix"       %% "monix"         % "3.0.0-RC1",
        "org.typelevel"  %% "cats-effect"   % "1.0.0-RC"
      )
  )

lazy val docs = project.in(file("docs"))
  .enablePlugins(MicrositesPlugin)
  .settings(stdSettings("zio-docs"))
  .settings(docSettings)
  .settings(noPublishSettings)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)


lazy val docSettings = Seq(
  micrositeName := "ZIO",
  micrositeDescription := "ZIO",
  micrositeAuthor := "John De Goes",
  micrositeHighlightTheme := "atom-one-light",
  micrositeHomepage := "https://github.com/scalaz/scalaz-zio",
//  micrositeBaseUrl := "/scalaz-zio",
  micrositeDocumentationUrl := "/scalaz-zio",
  micrositeGithubRepo := "https://github.com/scalaz/scalaz-zio",
//  micrositeBaseUrl := "https://github.com/scalaz/scalaz-zio",
  micrositePalette := Map(
    "brand-primary"   -> "#5B5988",
    "brand-secondary" -> "#292E53",
    "brand-tertiary"  -> "#222749",
    "gray-dark"       -> "#49494B",
    "gray"            -> "#7B7B7E",
    "gray-light"      -> "#E5E5E6",
    "gray-lighter"    -> "#F4F3F4",
    "white-color"     -> "#FFFFFF"),
  autoAPIMappings := true,
  ghpagesNoJekyll := false,
  fork in tut := true,

  git.remoteRepo := "git@github.com:scalaz/scalaz-zio.git",
  includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md"| "*.svg"
)
