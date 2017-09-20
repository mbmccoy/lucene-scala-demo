<!--
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
-->

# Lucene demo with Scala / Dotty

This project implements the Lucene file indexing and search [demo][lucene-demo] 
in Scala / [Dotty][dotty].


## Compile / test / run

This project uses `sbt` as a build tool. To index a directory or file, use the 
command

    sbt runMain IndexFiles -directory path/to/files

To search over your newly-created index, try

    sbt runMain SearchFiles dotty

For more information on the sbt-dotty plugin, see the
[dotty-example-project][dotty-example-project]. 

You can compile using `sbt compile`, test with `sbt test`, enter interactive 
mode with `sbt shell`, or spin up a Dotty REPL with `sbt console`.


## Dotty, you say?

[Dotty][dotty] represents the future of Scala. While the two languages are not 
fully compatible, if you've programmed in Scala but haven't heard of Dotty, you 
probably won't notice any differences than a significantly faster build.

You can learn more about Dotty from [Dotty's documentation][dotty-docs], or 
just get an overview of the changes by perusing 
[Martin Odersky's slides][scala-road-ahead].


[lucene-demo]: https://github.com/apache/lucene-solr/tree/master/lucene/demo/src/java/org/apache/lucene/demo
[dotty]: http://dotty.epfl.ch/
[dotty-docs]: http://dotty.epfl.ch/docs/
[scala-dotty-differences]: http://dotty.epfl.ch/docs/internals/dotc-scalac.html
[scala-road-ahead]: https://www.slideshare.net/Odersky/scala-days-nyc-2016
[dotty-example-project]: https://github.com/lampepfl/dotty-example-project/blob/master/README.md
