/*
   Copyright 2017 Michael McCoy and the Apache Software Foundation

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

import java.nio.file.Paths
import java.util.Date

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{IndexSearcher, ScoreDoc}
import org.apache.lucene.store.FSDirectory

import scala.annotation.tailrec

/**
  * Search files in an index. Companion to IndexFiles class
  */
object SearchFiles {

  val usage =
    "Usage: [-i INDEX=index] [-m MAX_HITS=10] [-f FIELD=contents] QUERY"
  val help =
    "Query INDEX for up to MAX_HITS results"

  // Command-line parameters
  private var index = "index"
  private var maxHits = 10
  private var field = "contents"

  /**
    * Parse arguments, returning the required query string
    */
  @tailrec
  def parseArgs(args: List[String]): String = args match {
    case ("-index" | "-i") :: value :: tail =>
      index = value
      parseArgs(tail)
    case ("-max-hits" | "-m") :: value :: tail =>
      try {
        maxHits = value.toInt
      } catch {
        case e: Exception =>
          println(s"Unable to parse max-hits $value: $e")
          println(usage)
          sys.exit(-1)
      }
      parseArgs(tail)
    case ("-field" | "-f") :: value :: tail =>
      field = value
      parseArgs(tail)
    case ("-help" | "-h") :: _ =>
      println(s"$usage\n\n$help")
      sys.exit()
    case queryStr :: Nil =>
      queryStr
    case _ =>
      println(usage)
      sys.exit(-1)
  }

  def main(args: Array[String]): Unit = {
    val queryStr = parseArgs(args.toList)

    println(
      s"Searching index '$index' over field '$field' with query '$queryStr'")

    val reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)))
    val searcher = new IndexSearcher(reader)
    val analyzer = new StandardAnalyzer()
    val queryParser = new QueryParser(field, analyzer)

    // Time the query
    val start = new Date()

    val query = queryParser.parse(queryStr)
    val topDocs = searcher.search(query, maxHits)
    val hits: List[ScoreDoc] = topDocs.scoreDocs.toList

    val end = new Date()

    // Print results and scores
    hits.foreach(hit => {
      val doc = searcher.doc(hit.doc)
      val path: String = Option(doc.get("path")).getOrElse("(No path?)")
      println(s"doc=${hit.doc}, score=${hit.score}, path=$path")
    })

    println(s"Found ${topDocs.totalHits} matching documents " +
      s"in ${start.getTime - end.getTime} milliseconds")
  }

}
