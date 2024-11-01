package com.viancis.checkers.builder

import com.viancis.checkers.CodeCheckInfo
import com.viancis.checkers.interfaces.CodeCheck
import com.viancis.communication.Request
import com.viancis.utils.Category
import com.viancis.utils.Printer
import org.reflections.Reflections
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.createInstance

class BuilderChecker {

    fun buildCheckers(language: String, request: Request): Map<String, CodeCheck> {
        val checkers = mutableMapOf<String, CodeCheck>()
        val packageName = "com.viancis.checkers.language.$language.category"

        val reflections = Reflections(packageName)
        val checkerClasses = reflections.getTypesAnnotatedWith(CodeCheckInfo::class.java)


        val config = request.config

        for (checkerClass in checkerClasses) {
            try {
                val checkerInstance = checkerClass.kotlin.createInstance()
                val annotation = checkerClass.kotlin.findAnnotation<CodeCheckInfo>()
                if (annotation != null) {
                    if (config.ignoredChecks.contains(annotation.code)) {
                        continue
                    }
                    checkers[annotation.code] = checkerInstance as CodeCheck
                    val message = "Найден проверочный класс: ${checkerClass.simpleName}, code: ${annotation.code}, level: ${annotation.level}"
                    Printer.printEnhanced(Category.DEBUG, message)
                }
            } catch (e: InstantiationException) {
                println("Не удалось создать экземпляр для класса ${checkerClass.simpleName}: ${e.message}")
            } catch (e: Exception) {
                println("Ошибка при создании экземпляра для класса ${checkerClass.simpleName}: ${e.message}")
            }
        }

        return checkers
    }
}