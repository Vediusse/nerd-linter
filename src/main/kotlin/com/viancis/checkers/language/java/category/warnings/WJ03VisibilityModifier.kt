package com.viancis.checkers.language.java.category.warnings

import com.viancis.checkers.BaseCodeCheck
import com.viancis.checkers.CodeCheckInfo
import com.viancis.checkers.CodeEntity
import com.viancis.communication.CheckResponse
import com.viancis.communication.CheckerContext
import com.viancis.utils.Category

@CodeCheckInfo(code = "WJ03", level = Category.WARNING)
class WJ03VisibilityModifier : BaseCodeCheck() {

    override fun check(context: CheckerContext): CheckResponse {
        val errors = mutableListOf<String>()
        val classEntity = context.getClassEntity().mainClass

        for (field in classEntity.fields) {
            checkAccessModifiers(field, field.accessModifier, errors,context)
        }

        for (method in classEntity.functions) {
            checkAccessModifiers(method, method.accessModifier, errors, context)
        }

        return createCheckResponse(errors)
    }

    private fun checkAccessModifiers(entityName: CodeEntity, keywords: String, errors: MutableList<String>, context: CheckerContext ) {
        if (keywords.isBlank() || keywords == "null") {
            errors.add("Модификатор доступа для '${entityName.name}' явно не указан используется default. \n" +
                       "\t ${context.file.toAbsolutePath()}:${(entityName.node?.startPoint?.row ?: 0) + 1} ")
        }
    }
}