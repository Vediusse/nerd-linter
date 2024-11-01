package com.viancis.checkers.language.java.category.warnings

import com.viancis.checkers.BaseCodeCheck
import com.viancis.checkers.CodeCheckInfo
import com.viancis.checkers.CodeEntity
import com.viancis.checkers.language.java.annotations.JavaClass
import com.viancis.checkers.language.java.entity.JavaCodeEntity
import com.viancis.communication.CheckResponse
import com.viancis.communication.CheckerContext
import com.viancis.utils.Category

@CodeCheckInfo(code = "WJ04", level = Category.WARNING)
class WJ04GetterInsteadPublic : BaseCodeCheck() {
    @JavaClass
    override fun check(context: CheckerContext): CheckResponse {
        val errors = mutableListOf<String>()
        val classEntity = context.getClassEntity().mainClass
        for (field in classEntity.fields) {
            if (field.accessModifier == "public") {
                val fieldName = field.name
                val fieldType = field.type
                val hasGetter = classEntity.functions.any { method:JavaCodeEntity.JavaMethodEntity ->
                    method.name == "get${fieldName.capitalize()}" && method.returnType == fieldType
                }
                val hasSetter = if (!field.keywords.contains("final")) {
                    classEntity.functions.any { method:JavaCodeEntity.JavaMethodEntity ->
                        method.name == "set${fieldName.capitalize()}" &&
                                method.parameters?.size == 1 &&
                                method.parameters[0].type == fieldType
                    }
                } else {
                    true
                }
                if (!hasGetter || !hasSetter) {
                    val errorMessage = buildErrorMessage(context, field, hasGetter, hasSetter)
                    errors.add(errorMessage)
                }
            }
        }

        return createCheckResponse(errors)
    }

    private fun buildErrorMessage(context: CheckerContext, fieldName: JavaCodeEntity.JavaFieldEntity, hasGetter: Boolean, hasSetter: Boolean): String {
        val missingParts = mutableListOf<String>()
        if (!hasGetter) missingParts.add("не найден геттер")
        if (!hasSetter) missingParts.add("не найден сеттер")

        return "Для поля '${fieldName.name}': ${missingParts.joinToString(" и ")}. \n\t${context.file.toAbsolutePath()}:${fieldName.node.startPoint.row + 1} "
    }
}