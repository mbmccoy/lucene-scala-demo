/*
   Copyright 2017 Michael McCoy

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

val dottyVersion = "0.3.0-RC2"

lazy val root = (project in file(".")).
  settings(
    name := "lucene-scala-demo",
    version := "0.1",

    scalaVersion := dottyVersion,

    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "org.apache.lucene" % "lucene-core" % "6.6.1",
      "org.apache.lucene" % "lucene-queryparser" % "6.6.1"
    )
  )
