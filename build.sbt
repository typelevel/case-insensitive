import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import sbt.ForkOptions
import sbt.Tests._

val catsV = "2.12.0"
val scalaJavaLocalesV = "1.5.4"
val scalacheckV = "1.18.0"
val munitV = "1.0.0"
val disciplineMunitV = "2.0.0"

ThisBuild / tlVersionIntroduced := Map(
  "3" -> "1.1.4"
)

// Utility

lazy val wildcardImport: SettingKey[Char] =
  settingKey[Char]("Character to use for wildcard imports.")
ThisBuild / wildcardImport := {
  if (tlIsScala3.value) {
    '*'
  } else {
    '_'
  }
}

/** Helper function to build the correct initial commands to full import the given packages. Order
  * is important here.
  */
def fullImports(packages: List[String], wildcard: Char): String =
  packages.map(value => s"import ${value}.${wildcard}").mkString("\n")

// Projects
lazy val root = tlCrossRootProject.aggregate(core, testing, tests, bench)

lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "case-insensitive",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % catsV
    ),
    console / initialCommands := {
      fullImports(List("cats", "cats.syntax.all", "org.typelevel.ci"), wildcardImport.value)
    },
    // For some reason consoleQuick delegates to console, which doesn't make
    // sense to me since the only difference is that console compiles the
    // local module, but consoleQuick only has the dependencies. That is,
    // you'd use consoleQuick if the local build isn't building and you just
    // wanted to fiddle with a dependency on the classpath. But the only place
    // to put local module imports has to be console, but since consoleQuick
    // delegates to that, that will break consoleQuick by default. So...
    consoleQuick / initialCommands := {
      fullImports(List("cats", "cats.syntax.all"), wildcardImport.value)
    }
  )
  .nativeSettings(
    tlVersionIntroduced := List("2.12", "2.13", "3").map(_ -> "1.4.0").toMap
  )

lazy val testing = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("testing"))
  .settings(
    name := "case-insensitive-testing",
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % scalacheckV
    ),
    console / initialCommands := {
      fullImports(
        List(
          "cats",
          "cats.syntax.all",
          "org.typelevel.ci",
          "org.typelevel.ci.testing",
          "org.typelevel.ci.testing.arbitraries",
          "org.scalacheck"),
        wildcardImport.value
      )
    },
    consoleQuick / initialCommands := {
      fullImports(List("cats", "cats.syntax.all", "org.scalacheck"), wildcardImport.value)
    }
  )
  .platformsSettings(JSPlatform, NativePlatform)(
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-locales" % scalaJavaLocalesV
  )
  .nativeSettings(
    tlVersionIntroduced := List("2.12", "2.13", "3").map(_ -> "1.3.0").toMap
  )
  .dependsOn(core)

lazy val tests = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("tests"))
  .enablePlugins(NoPublishPlugin)
  .settings(
    name := "case-insensitive-tests",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-laws" % catsV,
      "org.scalameta" %%% "munit" % munitV,
      "org.typelevel" %%% "discipline-munit" % disciplineMunitV
    ).map(_ % Test),
    Test / console / initialCommands := {
      fullImports(
        List(
          "cats",
          "cats.syntax.all",
          "org.typelevel.ci",
          "org.typelevel.ci.testing",
          "org.typelevel.ci.testing.arbitraries",
          "org.scalacheck"),
        wildcardImport.value
      )
    },
    Test / consoleQuick / initialCommands := {
      fullImports(List("cats", "cats.syntax.all", "org.scalacheck"), wildcardImport.value)
    }
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
    name := "case-insensitive-bench",
    console / initialCommands := {
      fullImports(List("cats", "cats.syntax.all", "org.typelevel.ci"), wildcardImport.value)
    },
    consoleQuick / initialCommands := ""
  )
  .dependsOn(core.jvm)

lazy val docs = project
  .in(file("site"))
  .dependsOn(core.jvm, testing.jvm)
  .enablePlugins(TypelevelSitePlugin)

val Scala213 = "2.13.14"

// General Settings
inThisBuild(
  List(
    tlBaseVersion := "1.4",
    scalaVersion := Scala213,
    crossScalaVersions := Seq("2.12.19", Scala213, "3.3.3"),
    developers := List(
      tlGitHubDev("rossabaker", "Ross A. Baker")
    ),
    homepage := Some(url("https://typelevel.org/case-insensitive")),
    tlSiteApiUrl := Some(url(
      "https://www.javadoc.io/doc/org.typelevel/case-insensitive_2.13/latest/org/typelevel/ci/index.html")),
    startYear := Some(2020)
  ))
