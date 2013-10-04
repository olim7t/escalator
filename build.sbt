import AssemblyKeys._
import com.typesafe.startscript.StartScriptPlugin

name := "elevator"

version := "0.1-SNAPSHOT"

organization := "olim7t"

scalaVersion := "2.10.0"

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
                  "Sonatype snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots/",
                  "Spray Repository"    at "http://repo.spray.io",
                  "Spray Nightlies"     at "http://nightlies.spray.io/")

libraryDependencies ++= {
  val akkaVersion       = "2.1.2"
  val sprayVersion      = "1.1-20130123"
  Seq(
    "com.typesafe.akka" %% "akka-actor"      % akkaVersion,
    "io.spray"          %  "spray-can"       % sprayVersion,
    "io.spray"          %  "spray-routing"   % sprayVersion,
    "io.spray"          %% "spray-json"      % "1.2.3",
    "com.typesafe.akka" %% "akka-slf4j"      % akkaVersion,
    "ch.qos.logback"    %  "logback-classic" % "1.0.10",
    "com.typesafe.akka" %% "akka-testkit"    % akkaVersion   % "test",
    "org.scalatest"     %% "scalatest"       % "1.9.1"       % "test",
    "io.spray"          %  "spray-testkit"   % sprayVersion  % "test"
  )
}

// Assembly settings
mainClass in Global := Some("olim7t.Main")

jarName in assembly := "elevator-server.jar"

assemblySettings

// StartScript settings
seq(StartScriptPlugin.startScriptForClassesSettings: _*)
