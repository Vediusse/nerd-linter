package com.viancis.utils

import java.io.BufferedReader


object Printer {
    fun printEnhanced(category: Category, message: String) {
        println("[${category.colorCode}${category.name}${Category.RESET.colorCode}] $message")
    }


    fun printEnhancedWithPhrase(category: Category, level: String,  message: String) {
        println("[${category.colorCode}${level}${Category.RESET.colorCode}] $message")
    }

    fun printNerdFaceWithMessage(message: String) {
        val nerdFace = this::class.java.getResourceAsStream("/nerd_face.txt")?.bufferedReader()?.use(BufferedReader::readText)
        println()

        if (nerdFace != null) {
            val nerdFaceLines = nerdFace.split("\n")
            val maxNerdFaceWidth = nerdFaceLines.maxOfOrNull { it.length } ?: 0


            val words = message.split(" ")


            val totalWords = words.size
            val wordsPerLine = (totalWords + 13) / 15


            val lines = mutableListOf<String>()
            for (i in 0..<14) {

                val start = i * wordsPerLine
                val end = minOf(start + wordsPerLine, totalWords)

                if (start < totalWords) {

                    lines.add(words.subList(start, end).joinToString(" "))
                } else {
                    lines.add("")
                }
            }
            for (i in nerdFaceLines.indices) {
                val paddedMessage = if (i < lines.size) {
                    lines[i].padStart(maxNerdFaceWidth + lines[i].length)
                } else {
                    " ".repeat(maxNerdFaceWidth + 1)
                }
                println("${nerdFaceLines[i]}$paddedMessage")
            }
        } else {
            println("Не удалось загрузить смайл из файла.")
        }
    }
}