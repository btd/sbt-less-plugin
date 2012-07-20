package com.github.btd

import sbt._

import java.io.{FileReader, BufferedReader}

class LessSourceFile(val lessFile: File, sourcesDir: File, cssDir: File) extends org.lesscss.LessSource(lessFile) {
  val relPath = IO.relativize(sourcesDir, lessFile).get

  lazy val cssFile = new File(cssDir, relPath.replaceFirst("\\.less$",".css"))
  //lazy val importsFile = new File(targetDir, relPath + ".imports")
  lazy val parentDir = lessFile.getParentFile

  def changed = this.getLastModifiedIncludingImports > cssFile.lastModified
  def path = lessFile.getPath.replace('\\', '/')

  override def toString = lessFile.toString
}

class ImportsFile(val file: File, sourcesDir: File,  outDir: File)  {
  val relPath = IO.relativize(sourcesDir, file).get

  lazy val outFile = new File(outDir, relPath.replaceFirst("\\.imports$",".css"))

  def normalizedContent = {
    (for {
          line <- IO.readLines(new BufferedReader(new FileReader(file)))
          lineTrimmed = line.trim
          if !lineTrimmed.isEmpty
        } yield {
          val f = new File(file.getParentFile, lineTrimmed)
    
          lineTrimmed.split("\\.").toList.last match {
            case "css" => IO.read(f)
            case "less" => {
              val compiler = new org.lesscss.LessCompiler
              val ls = new org.lesscss.LessSource(f)
              compiler.compile(ls.getNormalizedContent)
            }
          }
        }).mkString(IO.Newline)
  }
}
