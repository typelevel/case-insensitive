import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import sbt.ForkOptions
import sbt.Tests._

val catsV = "2.7.0"
val scalacheckV = "1.15.4"
val munitV = "0.7.29"
val disciplineMunitV = "1.0.9"

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

lazy val core = crossProject(JSPlatform, JVMPlatform)
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

lazy val testing = crossProject(JSPlatform, JVMPlatform)
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
    libraryDependencies ++= List(
      "org.scalacheck" %% "scalacheck" % scalacheckV
    ),
    console / initialCommands := {
      fullImports(List("cats", "cats.syntax.all", "org.typelevel.ci"), wildcardImport.value)
    },
    consoleQuick / initialCommands := ""
  )
  .dependsOn(core.jvm, testing.jvm)

lazy val docs = project
  .in(file("site"))
  .dependsOn(core.jvm, testing.jvm)
  .enablePlugins(TypelevelSitePlugin)

val Scala213 = "2.13.8"
val Scala213Cond = s"matrix.scala == '$Scala213'"

// General Settings
inThisBuild(
  List(
    tlBaseVersion := "1.2",
    crossScalaVersions := Seq("2.12.15", Scala213, "3.0.2"),
    homepage := Some(url("https://typelevel.org/case-insensitive")),
    tlSiteApiUrl := Some(url(
      "https://www.javadoc.io/doc/org.typelevel/case-insensitive_2.13/latest/org/typelevel/ci/index.html")),
    startYear := Some(2020)
  ))
