import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import sbt.ForkOptions
import sbt.Tests._

val catsV = "2.1.1"
val disciplineSpecs2V = "1.1.0"
val scalacheckV = "1.14.2"
val specs2V = "4.9.4"

val kindProjectorV = "0.11.0"
val betterMonadicForV = "0.3.1"

// Projects
lazy val `case-insensitive` = project.in(file("."))
  .disablePlugins(MimaPlugin)
  .enablePlugins(NoPublishPlugin)
  .aggregate(core, testing, tests, bench)

lazy val core = project.in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "case-insensitive",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % catsV,
    ),
  )

lazy val testing = project.in(file("testing"))
  .settings(commonSettings)
  .settings(
    name := "case-insensitive-testing",
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % scalacheckV,
    ),
  )
  .dependsOn(core)

lazy val tests = project.in(file("tests"))
  .disablePlugins(MimaPlugin)
  .enablePlugins(NoPublishPlugin)
  .settings(commonSettings)
  .settings(
    name := "case-insensitive-tests",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-laws" % catsV,
      "org.specs2" %% "specs2-core" % specs2V,
      "org.specs2" %% "specs2-scalacheck" % specs2V,
      "org.typelevel" %% "discipline-specs2" % disciplineSpecs2V,
    ).map(_ % Test),
    Test / testGrouping := {
      val (turkish, english) = (Test / definedTests).value.partition(_.name.contains("Turkey"))
      def group(language: String, tests: Seq[TestDefinition]) =
        new Group(language, tests, SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Duser.language=${language}"))))
      List(
        group("en", english),
        group("tr", turkish),
      )
    }
  )
  .dependsOn(testing)

lazy val bench = project.in(file("bench"))
  .disablePlugins(MimaPlugin)
  .enablePlugins(NoPublishPlugin)
  .enablePlugins(JmhPlugin)
  .settings(commonSettings)
  .settings(
    name := "case-insensitive-bench",
  )
  .dependsOn(core)

lazy val site = project.in(file("site"))
  .disablePlugins(MimaPlugin)
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(MdocPlugin)
  .enablePlugins(NoPublishPlugin)
  .settings(commonSettings)
  .dependsOn(core, testing)
  .settings{
    import microsites._
    Seq(
      micrositeName := "case-insensitive",
      micrositeDescription := "Case-insensitive data structures for Scala",
      micrositeAuthor := "Ross A. Baker",
      micrositeGithubOwner := "rossabaker",
      micrositeGithubRepo := "case-insensitive",
      micrositeBaseUrl := "/case-insensitive",
      micrositeDocumentationUrl := "https://www.javadoc.io/doc/com.rossabaker/case-insensitive_2.12",
      micrositeGitterChannel := false,
      micrositeFooterText := None,
      micrositeHighlightTheme := "atom-one-light",
      micrositePalette := Map(
        "brand-primary" -> "#3e5b95",
        "brand-secondary" -> "#294066",
        "brand-tertiary" -> "#2d5799",
        "gray-dark" -> "#49494B",
        "gray" -> "#7B7B7E",
        "gray-light" -> "#E5E5E6",
        "gray-lighter" -> "#F4F3F4",
        "white-color" -> "#FFFFFF"
      ),
      micrositeCompilingDocsTool := WithMdoc,
      scalacOptions in Tut --= Seq(
        "-Xfatal-warnings",
        "-Ywarn-unused-import",
        "-Ywarn-numeric-widen",
        "-Ywarn-dead-code",
        "-Ywarn-unused:imports",
        "-Xlint:-missing-interpolator,_"
      ),
      micrositePushSiteWith := GHPagesPlugin,
      micrositeExtraMdFiles := Map(
          file("CODE_OF_CONDUCT.md")  -> ExtraMdFileConfig("code-of-conduct.md",   "page", Map("title" -> "code of conduct",   "section" -> "code of conduct",   "position" -> "100")),
          file("LICENSE")             -> ExtraMdFileConfig("license.md",   "page", Map("title" -> "license",   "section" -> "license",   "position" -> "101"))
      )
    )
  }

// General Settings
lazy val commonSettings = Seq(
  scalaVersion := "2.13.1",
  crossScalaVersions := Seq(scalaVersion.value, "2.12.11"),

  addCompilerPlugin("org.typelevel" %% "kind-projector" % kindProjectorV cross CrossVersion.full),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % betterMonadicForV),
) ++ automateHeaderSettings(Compile, Test)

// General Settings
inThisBuild(List(
  organization := "com.rossabaker",
  developers := List(
    Developer("rossabaker", "Ross A. Baker", "ross@rossabaker.com", url("https://github.com/rossabaker"))
  ),

  homepage := Some(url("https://github.com/rossabaker/case-insensitive")),
  organizationName := "Ross A. Baker",
  startYear := Some(2020),
  licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),

  pomIncludeRepository := { _ => false},
  scalacOptions in (Compile, doc) ++= Seq(
      "-groups",
      "-sourcepath", (baseDirectory in LocalRootProject).value.getAbsolutePath,
      "-doc-source-url", "https://github.com/rossabaker/case-insensitive/blob/v" + version.value + "€{FILE_PATH}.scala"
  ),
))
