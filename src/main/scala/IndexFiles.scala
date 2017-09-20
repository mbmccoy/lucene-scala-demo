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

import java.io.{BufferedReader, File, InputStreamReader}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.Date

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document._
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.store.FSDirectory

import scala.annotation.tailrec

/**
  * Index files in a directory. Can be searched using the SearchFiles class.
  */
object IndexFiles {

  val usage =
    "Usage: [-index INDEX_DIR=index] [-directory DIRECTORY=.] [-update]"
  val help =
    "Create or update the index in INDEX_DIR with files in DIRECTORY"

  // Command-line parameters
  private var indexPath = "index"
  private var docsDir = "."
  private var update = false

  @tailrec
  private def parseArgs(opts: List[String]): Unit = opts match {
    case Nil => Unit
    case "-index" :: value :: tail =>
      indexPath = value
      parseArgs(tail)
    case "-directory" :: value :: tail =>
      docsDir = value
      parseArgs(tail)
    case "-update" :: tail =>
      update = true
      parseArgs(tail)
    case ("-help" | "-h") :: tail =>
      println(s"$usage\n\n$help")
      sys.exit(0)
    case _ =>
      println(usage)
      sys.exit(-1)
  }

  /**
    * Get the last modified date of a file in epoch milliseconds
    */
  private def lastModified(file: File): Long =
    Files.getLastModifiedTime(Paths.get(file.getPath)).toMillis

  /**
    * Recursively find all files and directories
    */
  private def findFiles(file: File): Stream[File] =
    if (file.exists() && file.isDirectory) {
      file.listFiles().toStream.flatMap(findFiles)
    } else {
      file #:: Stream.empty
    }

  /**
    * Indexes the given file using the given writer, or if a directory is given,
    * recurses over files and directories found under the given directory.
    *
    * NOTE: This method indexes one document per input file.  This is slow.
    * For good throughput, put multiple documents into your input file(s).  An
    * example of this is in the benchmark module, which can create "line doc"
    * files, one document per line, using the WriteLineDocTask
    *
    * @param indexWriter Writer to the index where the given file/dir info
    *                    will be stored
    * @param indexPath The file to index, or the directory to recurse into to
    *                  find files to index
    */
  private def indexDocs(indexWriter: IndexWriter, indexPath: File): Unit =
    findFiles(indexPath)
      .filter(_.exists())
      .filter(_.isFile)
      .map(createDoc)
      .foreach(pair => (pair, indexWriter.getConfig.getOpenMode) match {
        case ((_, doc), OpenMode.CREATE) =>
          // New index, so we don't need to update an old doc.
          indexWriter.addDocument(doc)
        case ((docPath, doc), _) =>
          // Existing index (an old copy of this document may have been indexed)
          // so we use updateDocument instead to replace the old one matching
          // the exact path, if present.
          indexWriter.updateDocument(new Term("path", docPath), doc)
      })

  /**
    * Index a single document
    *
    * @param file the file to index. Must exist and be a file
    * @return the (path, document) pair to be indexed
    */
  private def createDoc(file: File): (String, Document) = {
    println(s"Indexing file $file")
    val fullPath = file.getAbsolutePath
    val stream = Files.newInputStream(Paths.get(file.getPath))

    val doc = new Document()

    // Add the path of the file as a field named "path".  Use a
    // field that is indexed (i.e. searchable), but don't tokenize
    // the field into separate words and don't index term frequency
    // or positional information:
    val pathField = new StringField("path", fullPath, Field.Store.YES)
    doc.add(pathField)

    // Add the last modified date of the file a field named "modified".
    // Use a LongPoint that is indexed (i.e. efficiently filterable with
    // PointRangeQuery).  This indexes to milli-second resolution, which
    // is often too fine.  You could instead create a number based on
    // year/month/day/hour/minutes/seconds, down the resolution you require.
    // For example the long value 2011021714 would mean
    // February 17, 2011, 2-3 PM.
    doc.add(new LongPoint("modified", lastModified(file)))

    // Add the contents of the file to a field named "contents".  Specify a
    // Reader, so that the text of the file is tokenized and indexed, but not
    // stored. Note that FileReader expects the file to be in UTF-8 encoding.
    // If that's not the case searching for special characters will fail.
    doc.add(new TextField("contents", new BufferedReader(
      new InputStreamReader(stream, StandardCharsets.UTF_8))))

    (fullPath, doc)
  }

  def main(args: Array[String]): Unit = {
    parseArgs(args.toList)
    println(s"indexPath = $indexPath, docsPath = $docsDir, update = $update")

    val start = new Date()

    if (update) {
      println(s"Updating index in directory $indexPath.")
    } else {
      println(s"Creating new index in directory $indexPath.")
    }

    val dir = FSDirectory.open(Paths.get(indexPath))
    val analyzer = new StandardAnalyzer()
    val indexWriterConfig = new IndexWriterConfig(analyzer)

    if (update) {
      indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND)
    } else {
      indexWriterConfig.setOpenMode(OpenMode.CREATE)
    }

    // Optional: for better indexing performance, if you
    // are indexing many documents, increase the RAM
    // buffer.  But if you do this, increase the max heap
    // size to the JVM (eg add -Xmx512m or -Xmx1g):
    //
    // indexWriterConfig.setRAMBufferSizeMB(256.0)
    val indexWriter = new IndexWriter(dir, indexWriterConfig)

    val docsFile = new File(docsDir)
    indexDocs(indexWriter, docsFile)
    indexWriter.close()

    val end = new Date()

    println(s"Indexing took ${end.getTime - start.getTime} milliseconds.")
  }

}
