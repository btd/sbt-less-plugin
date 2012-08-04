package com.github.btd

import java.nio.charset.Charset
import java.util.Properties
import sbt._
import sbt.Keys._
import sbt.Project.Initialize
import collection.JavaConverters._

object Plugin extends sbt.Plugin {

  object StyleKeys {
    val less                = TaskKey[Seq[File]]("less", "Compile Less CSS sources.")
    val combine             = TaskKey[Seq[File]]("combine", "Combine and minify if needed all files from .imports file.")
    val minify              = SettingKey[Boolean]("minify", "Whether to pretty print CSS (default false)")
  }

  import StyleKeys._

  def compileTask =
    (streams, sourceDirectories in less, resourceManaged in less, includeFilter in less, excludeFilter in less, minify in less) map {
      (out, sourceDirs, cssDir, includeFilter, excludeFilter, compress) =>
        
        val compiler = new org.lesscss.LessCompiler

        compiler.setCompress(!compress)

        for {
          sourceDir <- sourceDirs
          src <- sourceDir.descendentsExcept(includeFilter, excludeFilter).get
          lessSrc = new LessSourceFile(src, sourceDir, cssDir)
          if lessSrc.changed
        } yield {
          compiler.compile(lessSrc, lessSrc.cssFile)
          lessSrc.cssFile
        }
    }

  def combineTask =
    (streams, sourceDirectories in less, resourceManaged in less, minify in combine) map {
      (out, sourceDirs, cssDir, compress) =>
        for {
          sourceDir <- sourceDirs
          src <- sourceDir.descendentsExcept("*.imports", HiddenFileFilter).get
          importsFile = new ImportsFile(src, sourceDir, cssDir)

        } yield {
          val dataToWrite = if(!compress) importsFile.normalizedContent else {
            val reader = new java.io.StringReader(importsFile.normalizedContent)
            val compressor = new com.yahoo.platform.yui.compressor.CssCompressor(reader)
            reader.close
            
            val writer = new java.io.StringWriter
            compressor.compress(writer, -1)
            writer.toString
          }
          IO.write(importsFile.outFile, dataToWrite)
          importsFile.outFile
        }
    }

  def cleanTask =
    (streams, sourceDirectories in less, resourceManaged in less, includeFilter in less, excludeFilter in less) map {
      (out, sourceDirs, cssDir, includeFilter, excludeFilter) =>
        for {
          sourceDir <- sourceDirs
          src <- sourceDir.descendentsExcept(includeFilter, excludeFilter).get
          lessSrc = new LessSourceFile(src, sourceDir, cssDir)
        } {
          lessSrc.cssFile.delete
        }
    }

  def watchSourcesTask: Initialize[Task[Seq[File]]] =
    (streams, sourceDirectories in less, resourceManaged in less, includeFilter in less, excludeFilter in less) map {
      (out, sourceDirs, cssDir, includeFilter, excludeFilter) =>
        val lists: Seq[Seq[File]] = (for {
                                sourceDir <- sourceDirs
                                src <- sourceDir.descendentsExcept(includeFilter, excludeFilter).get
                                lessSrc = new LessSourceFile(src, sourceDir, cssDir)
                              } yield {
                                lessSrc.getAllFiles.asScala
                              })
        lists.flatten
    }

  def unmanagedSourcesTask = watchSourcesTask

  def styleSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      minify                       :=  false,
      includeFilter in less        :=  "*.less",
      excludeFilter in less        :=  (".*" - ".") || "_*" || HiddenFileFilter,
      sourceDirectory in less      <<= (sourceDirectory in conf),
      sourceDirectories in less    <<= (sourceDirectory in conf) { Seq(_) },
      resourceManaged in less      <<= (resourceManaged in conf),
      clean in less                <<= cleanTask,
      less                         <<= compileTask,
      combine                      <<= combineTask,
      unmanagedSources in less     <<= unmanagedSourcesTask,
      sources in less              <<= watchSourcesTask,
      watchSources in less         <<= watchSourcesTask
    ))

  def styleSettings: Seq[Setting[_]] =
    styleSettingsIn(Compile) ++
    styleSettingsIn(Test)

}
