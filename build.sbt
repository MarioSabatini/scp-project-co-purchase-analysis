import Dependencies._

ThisBuild / scalaVersion     := "2.12.18"
ThisBuild / organization     := "com.project"
ThisBuild / organizationName := "project"

lazy val root = (project in file("."))
  .settings(
    name := "co-purchase_analysis",
    libraryDependencies += munit % Test
  )

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.5.1"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.1"
