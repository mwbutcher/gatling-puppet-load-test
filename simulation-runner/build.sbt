name := "gatling-puppet-load-test"

version := "0.1.1-SNAPSHOT"

scalaVersion := "2.10.2"

net.virtualvoid.sbt.graph.Plugin.graphSettings

resolvers ++= Seq(
                  "Excilys" at "http://repository.excilys.com/content/groups/public"
                  )

libraryDependencies += "io.gatling" % "gatling-app" % "2.0.0-RC4"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.0.0-RC4" exclude("io.gatling", "gatling-recorder")

mainClass in (Compile, run) := Some("com.puppetlabs.gatling.runner.PuppetGatlingRunner")

unmanagedClasspath in Runtime <+= (baseDirectory) map { bd => Attributed.blank(bd / "config") }

fork := true

javaOptions in run ++= Seq("-server", "-XX:+UseThreadPriorities",
  "-XX:ThreadPriorityPolicy=42", "-Xms512M", "-Xmx512M", "-Xmn100M", "-Xss2M",
  "-XX:+HeapDumpOnOutOfMemoryError", "-XX:+AggressiveOpts",
  "-XX:+OptimizeStringConcat", "-XX:+UseFastAccessorMethods", "-XX:+UseParNewGC",
  "-XX:+UseConcMarkSweepGC", "-XX:+CMSParallelRemarkEnabled",
  "-XX:+CMSClassUnloadingEnabled", "-XX:SurvivorRatio=8",
  "-XX:MaxTenuringThreshold=1", "-XX:CMSInitiatingOccupancyFraction=75",
  "-XX:+UseCMSInitiatingOccupancyOnly",
  "-Dpuppet.gatling.config=./config/sample_scenario_config.json")
