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

// Projects
lazy val root = tlCrossRootProject.aggregate(core, testing, tests, bench)

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
      "org.scalameta" %%% "munit" % munitV,
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
    crossScalaVersions := Seq("2.12.15", Scala213, "3.1.1"),
    homepage := Some(url("https://typelevel.org/case-insensitive")),
    tlSiteApiUrl := Some(url(
      "https://www.javadoc.io/doc/org.typelevel/case-insensitive_2.13/latest/org/typelevel/ci/index.html")),
    startYear := Some(2020)
  ))
