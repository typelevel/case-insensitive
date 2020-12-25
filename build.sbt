import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import sbt.ForkOptions
import sbt.Tests._

val catsV = "2.3.1"
val disciplineSpecs2V = "1.1.3"
val scalacheckV = "1.15.2"
val specs2V = "4.10.5"

val kindProjectorV = "0.11.2"
val betterMonadicForV = "0.3.1"

val scala_2_12 = "2.12.12"
val scala_2_13 = "2.13.3"

// Projects
lazy val `case-insensitive` = project
  .in(file("."))
  .enablePlugins(NoPublishPlugin)
  .aggregate(core.jvm, core.js, testing.jvm, testing.js, tests.jvm, tests.js, bench, site)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "case-insensitive",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % catsV
    )
  )

lazy val testing = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("testing"))
  .settings(commonSettings)
  .settings(
    name := "case-insensitive-testing",
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % scalacheckV
    )
  )
  .jsSettings(
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-locales" % "1.1.0"
  )
  .dependsOn(core)

lazy val tests = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("tests"))
  .enablePlugins(NoPublishPlugin)
  .settings(commonSettings)
  .settings(
    name := "case-insensitive-tests",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-laws" % catsV,
      "org.specs2" %%% "specs2-core" % specs2V,
      "org.specs2" %%% "specs2-scalacheck" % specs2V,
      "org.typelevel" %%% "discipline-specs2" % disciplineSpecs2V
    ).map(_ % Test)
  )
  .jvmSettings(
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

lazy val bench = project
  .in(file("bench"))
  .enablePlugins(NoPublishPlugin)
  .enablePlugins(JmhPlugin)
  .settings(commonSettings)
  .settings(
    name := "case-insensitive-bench",
  )
  .dependsOn(core.jvm)

lazy val site = project.in(file("site"))
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(MdocPlugin)
  .enablePlugins(NoPublishPlugin)
  .settings(commonSettings)
  .dependsOn(core.jvm, core.js, testing.jvm, testing.js)
  .settings{
    import microsites._
    Seq(
      micrositeName := "case-insensitive",
      micrositeDescription := "Case-insensitive data structures for Scala",
      micrositeAuthor := "Ross A. Baker",
      micrositeGithubOwner := "typelevel",
      micrositeGithubRepo := "case-insensitive",
      micrositeBaseUrl := "/case-insensitive",
      micrositeDocumentationUrl := "https://www.javadoc.io/doc/org.typelevel/case-insensitive_2.12",
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
      micrositePushSiteWith := GHPagesPlugin,
      micrositeExtraMdFiles := Map(
          file("CODE_OF_CONDUCT.md")  -> ExtraMdFileConfig("code-of-conduct.md",   "page", Map("title" -> "code of conduct",   "section" -> "code of conduct",   "position" -> "100")),
          file("LICENSE")             -> ExtraMdFileConfig("license.md",   "page", Map("title" -> "license",   "section" -> "license",   "position" -> "101"))
      )
    )
  }

// General Settings
lazy val commonSettings = Seq(
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % betterMonadicForV),

  headerLicenseStyle := HeaderLicenseStyle.SpdxSyntax
) ++ automateHeaderSettings(Compile, Test)

// General Settings
inThisBuild(List(
  organization := "org.typelevel",
  organizationName := "Typelevel",
  publishGithubUser := "rossabaker",
  publishFullName := "Ross A. Baker",
  baseVersion := "0.3",

  crossScalaVersions := Seq(scala_2_12, scala_2_13),

  homepage := Some(url("https://github.com/typelevel/case-insensitive")),
  startYear := Some(2020),
  licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  scmInfo := Some(ScmInfo(url("https://github.com/typelevel/case-insensitive"),
    "git@github.com:typelevel/case-insensitive.git")),

  githubWorkflowTargetTags ++= Seq("v*"),
  githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v"))),
  githubWorkflowBuildPreamble := Seq(
    WorkflowStep.Use("actions", "setup-ruby", "v1"),
    WorkflowStep.Run(List(
      "gem install bundler",
      "bundle install --gemfile=site/Gemfile"
    ), name = Some("Install Jekyll"))
  ),
  githubWorkflowBuild := Seq(
    WorkflowStep.Sbt(List(
      "headerCheck",
      "test:headerCheck",
      "scalafmtCheckAll",
      "testIfRelevant",
      "mimaReportBinaryIssuesIfRelevant",
      "doc",
      "makeMicrosite"
    )),
  ),
  githubWorkflowPublish := Seq(
    WorkflowStep.Sbt(List("ci-release")),
    WorkflowStep.Run(List(
      """eval "$$(ssh-agent -s)"""",
      """echo "$$SSH_PRIVATE_KEY" | ssh-add -""",
      """git config --global user.name "GitHub Actions CI"""",
      """git config --global user.email "ghactions@invalid""""
    )),
    WorkflowStep.Sbt(List("site/publishMicrosite"),
      name = Some(s"Publish microsite"),
      env = Map("SSH_PRIVATE_KEY" -> "${{ secrets.SSH_PRIVATE_KEY }}"))
  ),
))
