package com.viancis.checkers.language.java.category.convention

import com.viancis.checkers.BaseCodeCheck
import com.viancis.checkers.CodeCheckInfo
import com.viancis.checkers.language.java.annotations.JavaClass
import com.viancis.communication.CheckResponse
import com.viancis.communication.CheckerContext
import com.viancis.utils.Category
import java.util.regex.Pattern

@CodeCheckInfo(code = "CJ02", level = Category.CONVENTION)
class CJ02EntityNamingConvention : BaseCodeCheck() {

    enum class NamingConvention(val pattern: Pattern, val message: String) {
        CONSTANT(Pattern.compile("^[A-Z_]+\$"), "Поле помечено как final и должно быть в верхнем регистре (UPPER_SNAKE_CASE)."),
        CLASS(Pattern.compile("^[A-Z][a-zA-Z0-9]*\$"), "Класс не соответствует соглашению об именовании (CamelCase)."),
        METHOD_FIELD(Pattern.compile("^[a-z][a-zA-Z0-9]*\$"), "Поле/метод не соответствует соглашению об именовании (должно быть camelCase)."),
    }

    @JavaClass
    override fun check(context: CheckerContext): CheckResponse {
        val errors = mutableListOf<String>()
        val classEntity = context.getClassEntity().mainClass


        if (!NamingConvention.CLASS.pattern.matcher(classEntity.name).matches()) {
            errors.add("Класс '${classEntity.name}' ${NamingConvention.CLASS.message}")
        }


        for (field in classEntity.fields) {
            if ("final" in field.keywords) {
                if (!NamingConvention.CONSTANT.pattern.matcher(field.name).matches()) {
                    errors.add("Поле '${field.name}' ${NamingConvention.CONSTANT.message}")
                }
            } else {
                if (!NamingConvention.METHOD_FIELD.pattern.matcher(field.name).matches()) {
                    errors.add("Поле '${field.name}' ${NamingConvention.METHOD_FIELD.message}")
                }
            }
        }


        for (method in classEntity.functions) {
            if (!NamingConvention.METHOD_FIELD.pattern.matcher(method.name).matches()) {
                errors.add("Метод '${method.name}' ${NamingConvention.METHOD_FIELD.message}")
            }
        }


        return createCheckResponse(errors)
    }
}