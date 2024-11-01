package com.viancis.checkers.builder

import com.viancis.checkers.interfaces.CodeAnalyzer
import com.viancis.checkers.interfaces.CodeCheck
import com.viancis.checkers.language.java.entity.Analyzer
import org.reflections.Reflections
import kotlin.reflect.full.createInstance

class BuilderAnalyze {

    fun buildAnalyze(language: String): CodeAnalyzer?  {
        val checkers = mutableMapOf<String, CodeCheck>()
        val packageName = "com.viancis.checkers.language.$language"

        val reflections = Reflections(packageName)
        val checkerClasses = reflections.getTypesAnnotatedWith(Analyzer::class.java)

        for (checkerClass in checkerClasses) {
            try {
                val checkerInstance = checkerClass.kotlin.createInstance()
                return checkerInstance as CodeAnalyzer
            } catch (e: InstantiationException) {
                println("Не удалось создать экземпляр для класса ${checkerClass.simpleName}: ${e.message}")
            } catch (e: Exception) {
                println("Ошибка при создании экземпляра для класса ${checkerClass.simpleName}: ${e.message}")
            }
        }

        return null
    }

    fun buildAnalyzeForExtensions(fileExtensions: List<String>): Map<String, CodeAnalyzer?> {
        val analyzersMap = mutableMapOf<String, CodeAnalyzer?>()

        for (extension in fileExtensions) {
            val analyzer = buildAnalyze(extension)
            analyzersMap[extension] = analyzer
        }
        return analyzersMap
    }
}