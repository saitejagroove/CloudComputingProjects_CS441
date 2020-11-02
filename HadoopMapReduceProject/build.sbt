name := "Saiteja_Karnati_hw2"

version := "0.1"

scalaVersion := "2.13.1"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.4"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe" % "config" % "1.3.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.4.0" exclude("org.slf4j", "slf4j-log4j12")
libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "2.4.0" exclude("org.slf4j", "slf4j-log4j12")
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.2.0"

mainClass in (Compile, run) := Some("com.skarna3.hw2.MapReduceDriver")
mainClass in assembly := Some("com.skarna3.hw2.MapReduceDriver")

assemblyJarName in assembly := "saiteja_karnati_hw2.jar"
