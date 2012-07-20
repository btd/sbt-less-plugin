sbtPlugin    := true

organization := "com.github.btd"

name         := "sbt-style-plugin"

version      := "0.0.3"

licenses     += ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

seq(ScriptedPlugin.scriptedSettings: _*)

scalacOptions           ++= DefaultOptions.scalac

scalacOptions in Compile += Opts.compile.deprecation

scalacOptions in Compile += Opts.compile.unchecked

publishArtifact in Test := false

libraryDependencies += "org.mozilla" % "rhino" % "1.7R4"

libraryDependencies += "com.yahoo.platform.yui" % "yuicompressor" % "2.4.7"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

publishMavenStyle := true

scriptedBufferLog := false

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}


pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/btd/sbt-less-plugin</url>
  <scm>
    <url>git@github.com:btd/sbt-less-plugin.git</url>
    <connection>scm:git:git@github.com:btd/sbt-less-plugin.git</connection>
  </scm>
  <developers>
    <developer>
      <id>btd</id>
      <name>Bardadym Denis</name>
    </developer>
  </developers>)