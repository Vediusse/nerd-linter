package com.viancis.linter

import com.viancis.checkers.builder.BuilderAnalyze
import com.viancis.checkers.language.java.ProcessEntity
import com.viancis.checkers.language.java.entity.JavaCodeEntity
import com.viancis.communication.Request
import org.treesitter.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes

class SyntaxAnalyzer {

    private val analizeChecker: BuilderAnalyze = BuilderAnalyze()
    private val entityMap = mutableMapOf<String, JavaCodeEntity.JavaFileEntity>()

    fun parseSourceFromRequest(request: Request): Pair<Map<String, Pair<Path, TSTree>>, Map<String, JavaCodeEntity.JavaFileEntity>> {
        val rootPath = Paths.get(request.source)
        val parsedFiles = mutableListOf<Pair<Path, TSTree>>()
        val treedFiles = mutableMapOf<String, Pair<Path, TSTree>>()

        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            println("Указанный путь не существует или не является директорией: ${request.source}")
            return Pair(emptyMap(), emptyMap())
        }

        val analyzersMap = analizeChecker.buildAnalyzeForExtensions(request.config.programmingLanguages)

        Files.walkFileTree(rootPath, object : java.nio.file.SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path?, attrs: BasicFileAttributes?): java.nio.file.FileVisitResult {
                file?.let {
                    val fileExtension = getFileExtension(it.fileName.toString())
                    if (request.config.programmingLanguages.contains(fileExtension)) {

                        val analyzer = analyzersMap[fileExtension]
                        val tree = parseFile(it)
                        tree?.let { t ->
                            parsedFiles.add(it to t)
                            analyzer?.let { codeAnalyzer ->
                                val entity = codeAnalyzer.analyzeCode(t, it)
                                if (entity is JavaCodeEntity.JavaFileEntity) {
                                    val fullName = "${entity.packageName}.${entity.mainClass.name}"
                                    entityMap[fullName] = entity
                                    treedFiles[fullName] = Pair(it, t)
                                }
                            } ?: run {
                                println("Анализатор для расширения $fileExtension не найден")
                            }
                        }
                    }
                }
                return java.nio.file.FileVisitResult.CONTINUE
            }
        })
        if(entityMap is HashMap<String, JavaCodeEntity.JavaFileEntity>){
            ProcessEntity.processEntityMap(entityMap)
        }


        return Pair(treedFiles, entityMap)
    }

    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast(".", "")
    }

    fun parseFile(file: Path): TSTree? {
        val parser = TSParser()
        val language = detectLanguage(file)
        parser.setLanguage(language)

        val sourceCode = Files.readString(file)

        return parser.parseString(null, sourceCode)
    }

    private fun detectLanguage(file: Path): TSLanguage {
        val fileName = file.fileName.toString()
        return when {
            fileName.endsWith(".json") -> TreeSitterJson()
            fileName.endsWith(".java") -> TreeSitterJava()
            fileName.endsWith(".js") -> TreeSitterJavascript()
            fileName.endsWith(".py") -> TreeSitterPython()
            else -> throw IllegalArgumentException("Unsupported file type: $fileName")
        }
    }
}