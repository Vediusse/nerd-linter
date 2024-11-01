package com.viancis.utils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes

class FileWalker {

    fun getSourceFiles(sourcePath: String, fileExtensions: List<String>): List<Path> {
        val rootPath = Paths.get(sourcePath)
        val filesList = mutableListOf<Path>()

        if (!Files.exists(rootPath)) {
            println("Указанный путь не существует: $sourcePath")
            return emptyList()
        }

        if (!Files.isDirectory(rootPath)) {
            println("Указанный путь не является директорией: $sourcePath")
            return emptyList()
        }

        println("Обход директории: $sourcePath")

        Files.walkFileTree(rootPath, object : java.nio.file.SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path?, attrs: BasicFileAttributes?): java.nio.file.FileVisitResult {
                file?.let {
                    if (isSourceFile(it, fileExtensions)) {
                        filesList.add(it)
                        println("Найден файл: ${file.fileName}")
                    }
                }
                return java.nio.file.FileVisitResult.CONTINUE
            }
        })
        return filesList
    }

    private fun isSourceFile(file: Path, fileExtensions: List<String>): Boolean {
        val fileName = file.fileName.toString()
        return fileExtensions.any { fileName.endsWith(it) }
    }
}