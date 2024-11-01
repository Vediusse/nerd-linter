package com.viancis.config

import java.io.File

data class Config(
    val author: String = "Unknown Author",
    val programmingLanguages: List<String> = listOf("java"),
    val ignoredChecks: List<String> = emptyList()
)
class ConfigLoader(private val filePath: String?) {
    fun load(): Config {

        if (filePath.isNullOrEmpty() || !File(filePath).exists()) {
            println("Файл конфигурации не найден или путь не указан. ${filePath}")
            return Config()
        }

        val file = File(filePath)
        val lines = file.readLines()

        var author = "Unknown Author"
        var programmingLanguages = emptyList<String>()
        var ignoredChecks = emptyList<String>()

        for (line in lines) {
            when {
                line.startsWith("author:", ignoreCase = true) -> {
                    author = line.substringAfter("author:").trim().removeSuffix(";")
                }
                line.startsWith("lang:", ignoreCase = true) -> {
                    programmingLanguages = line.substringAfter("lang:").trim()
                        .removeSuffix(";")
                        .removeSurrounding("[", "]")
                        .split(",")
                        .map { it.trim().removeSurrounding("\"") }
                }
                line.startsWith("ignore:", ignoreCase = true) -> {
                    ignoredChecks = line.substringAfter("ignore:").trim()
                        .removeSuffix(";")
                        .removeSurrounding("[", "]")
                        .split(",")
                        .map { it.trim().removeSurrounding("\"") }
                }
            }
        }

        return Config(author, programmingLanguages, ignoredChecks)
    }
}