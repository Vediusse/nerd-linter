package com.viancis.checkers.language.java.category.warnings

import com.viancis.checkers.BaseCodeCheck
import com.viancis.checkers.CodeCheckInfo
import com.viancis.checkers.CodeEntity
import com.viancis.checkers.language.java.entity.JavaCodeEntity
import com.viancis.communication.CheckResponse
import com.viancis.communication.CheckerContext
import com.viancis.utils.Category

@CodeCheckInfo(code = "WJ02", level = Category.WARNING)
class WJ02OrderClassFields : BaseCodeCheck() {

    private enum class FieldCategory {
        STATIC_FINAL,
        STATIC,
        FINAL,
        INSTANCE,
        TRANSIENT_OR_VOLATILE
    }

    override fun check(context: CheckerContext): CheckResponse {
        val errors = mutableListOf<String>()
        val classEntity = context.getClassEntity().mainClass

        val categorizedFields = mutableMapOf<FieldCategory, MutableList<String>>().apply {
            FieldCategory.values().forEach { this[it] = mutableListOf() }
        }

        classEntity.fields.forEach { field: JavaCodeEntity.JavaFieldEntity ->
            categorizeField(field).let { category ->
                categorizedFields[category]?.add(field.name)
            }
        }
        val orderedFields = orderedFieldList(categorizedFields)
        classEntity.fields.forEachIndexed { index: Int, field: JavaCodeEntity.JavaFieldEntity ->
            if (field.name != orderedFields[index]) {
                val line = field.node.startPoint.row + 1
                errors.add("Поле '${field.name}' в классе '${classEntity.name}' находится не в том порядке.\n\t ${context.file.toAbsolutePath()}:${line} ")
            }
        }

        return createCheckResponse(errors)
    }

    private fun categorizeField(field: JavaCodeEntity.JavaFieldEntity): FieldCategory {
        val keywords = field.keywords
        return when {
            "static" in keywords && "final" in keywords -> FieldCategory.STATIC_FINAL
            "static" in keywords -> FieldCategory.STATIC
            "final" in keywords -> FieldCategory.FINAL
            "transient" in keywords || "volatile" in keywords -> FieldCategory.TRANSIENT_OR_VOLATILE
            else -> FieldCategory.INSTANCE
        }
    }

    private fun orderedFieldList(categorizedFields: Map<FieldCategory, List<String>>): List<String> {
        return FieldCategory.values().flatMap { categorizedFields[it] ?: emptyList() }
    }
}