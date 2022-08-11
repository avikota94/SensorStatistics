name := "statistics-sensor"

version := "0.1"

scalaVersion := "2.11.8"


libraryDependencies++=Seq(
 "org.apache.spark" %% "spark-core" % "1.6.0",
  "org.apache.spark" %% "spark-sql" % "1.6.0",
 "org.scalatest" %% "scalatest" % "2.2.2" % Test

)



