import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import sbt.ForkOptions
import sbt.Tests._

val catsV = "2.7.0"
val scalacheckV = "1.15.4"
val disciplineMunitV = "1.0.9"

ThisBuild / tlVersionIntroduced := Map(
  "3" -> "1.1.4"
)

// Projects
lazy val `case-insensitive` = project
  .in(file("."))
  .enablePlugins(NoPublishPlugin)
  .aggregate(core.jvm, core.js, testing.jvm, testing.js, tests.jvm, tests.js, bench)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "case-insensitive",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % catsV
    )
  )

lazy val testing = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("testing"))
  .settings(
    name := "case-insensitive-testing",
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % scalacheckV
    )
  )
  .jsSettings(
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-locales" % "1.2.0"
  )
  .dependsOn(core)

lazy val tests = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("tests"))
  .enablePlugins(NoPublishPlugin)
  .settings(
    name := "case-insensitive-tests",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-laws" % catsV,
      "org.typelevel" %%% "discipline-munit" % disciplineMunitV
    ).map(_ % Test)
  )
  .jvmSettings(
    Test / testGrouping := {
      val (turkish, english) = (Test / definedTests).value.partition(_.name.contains("Turkey"))
      def group(language: String, tests: Seq[TestDefinition]) =
        new Group(
          language,
          tests,
          SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Duser.language=$language"))))
      List(
        group("en", english),
        group("tr", turkish)
      )
    }
  )
  .dependsOn(testing)

lazy val bench = project
  .in(file("bench"))
  .enablePlugins(NoPublishPlugin)
  .enablePlugins(JmhPlugin)
  .settings(
    name := "case-insensitive-bench"
  )
  .dependsOn(core.jvm)

lazy val site = project
  .in(file("site"))
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(MdocPlugin)
  .enablePlugins(NoPublishPlugin)
  .dependsOn(core.jvm, testing.jvm)
  .settings {
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
        file("CODE_OF_CONDUCT.md") -> ExtraMdFileConfig(
          "code-of-conduct.md",
          "page",
          Map("title" -> "code of conduct", "section" -> "code of conduct", "position" -> "100")),
        file("LICENSE") -> ExtraMdFileConfig(
          "license.md",
          "page",
          Map("title" -> "license", "section" -> "license", "position" -> "101"))
      ),
      githubWorkflowArtifactUpload := false
    ),
  }

val Scala213 = "2.13.8"
val Scala213Cond = s"matrix.scala == '$Scala213'"

// General Settings
inThisBuild(
  List(
    tlBaseVersion := "1.2",
    crossScalaVersions := Seq("2.12.15", Scala213, "3.0.2"),
    homepage := Some(url("https://typelevel.org/case-insensitive")),
    startYear := Some(2020),
    githubWorkflowBuildPreamble ++= Seq(
      WorkflowStep
        .Use(UseRef.Public("actions", "setup-ruby", "v1"), params = Map("ruby-version" -> "2.7")),
      WorkflowStep.Run(
        List(
          "gem install bundler",
          "bundle install --gemfile=site/Gemfile"
        ),
        name = Some("Install Jekyll"))
    ),
    githubWorkflowBuild +=
      WorkflowStep.Sbt(List(s"++$Scala213", "site/makeMicrosite"), cond = Some(Scala213Cond)),
    githubWorkflowPublish := Seq(
      WorkflowStep.Sbt(List("release")),
      WorkflowStep.Run(List(
        """eval "$(ssh-agent -s)"""",
        """echo "$SSH_PRIVATE_KEY" | ssh-add -""",
        """git config --global user.name "GitHub Actions CI"""",
        """git config --global user.email "ghactions@invalid""""
      )),
      WorkflowStep.Sbt(
        List("++${Scala213}", "site/publishMicrosite"),
        name = Some(s"Publish microsite"),
        env = Map("SSH_PRIVATE_KEY" -> "${{ secrets.SSH_PRIVATE_KEY }}"))
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  ))
