name := "gatling-puppet-agent-capture"

version := "0.1.1-SNAPSHOT"

scalaVersion := "2.10.2"

libraryDependencies += "io.gatling" % "gatling-recorder" % "2.0.0-RC4" exclude("org.scala-lang", "scala-compiler")

libraryDependencies += "io.gatling" % "gatling-app" % "2.0.0-RC4"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.0.0-RC4"

resolvers += "Excilys" at "http://repository.excilys.com/content/groups/public"

mainClass in (Compile, run) := Some("io.gatling.recorder.GatlingRecorder")

fork := true

javaOptions in run ++= Seq("-server", "-XX:+UseThreadPriorities",
  "-XX:ThreadPriorityPolicy=42", "-Xms512M", "-Xmx512M", "-Xmn100M", "-Xss2M",
  "-XX:+HeapDumpOnOutOfMemoryError", "-XX:+AggressiveOpts",
  "-XX:+OptimizeStringConcat", "-XX:+UseFastAccessorMethods", "-XX:+UseParNewGC",
  "-XX:+UseConcMarkSweepGC", "-XX:+CMSParallelRemarkEnabled",
  "-XX:+CMSClassUnloadingEnabled", "-XX:SurvivorRatio=8",
  "-XX:MaxTenuringThreshold=1", "-XX:CMSInitiatingOccupancyFraction=75",
  "-XX:+UseCMSInitiatingOccupancyOnly",
  "-Dgatling.recorder.keystore.path=./target/tmp/ssl/gatling-proxy-keystore.jks",
  "-Dgatling.recorder.keystore.passphrase=puppet")
