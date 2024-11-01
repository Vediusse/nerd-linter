package com.viancis.checkers.language.java.category.convention

import com.viancis.checkers.BaseCodeCheck
import com.viancis.checkers.CodeCheckInfo
import com.viancis.checkers.language.java.annotations.JavaClass
import com.viancis.communication.CheckResponse
import com.viancis.communication.CheckerContext
import com.viancis.utils.Category

@CodeCheckInfo(code = "CJ01", level = Category.CONVENTION)
class CJ01ImportPosition : BaseCodeCheck() {

    @JavaClass
    override fun check(context: CheckerContext): CheckResponse {
        val errors = mutableListOf<String>()
        val classEntity = context.getClassEntity()

        val projectPackagePrefix = classEntity?.packageName?.split(".")?.take(2)?.joinToString(".") ?: ""

        if (projectPackagePrefix.startsWith("java") || projectPackagePrefix.startsWith("javax")) {
            errors.add("Префикс пакета проекта не должен начинаться с 'java' или 'javax' ${context.file.toAbsolutePath()}:0")
        }

        val imports = classEntity.imports.distinct()
        val orderedGroups = listOf("builtin", "external", "project")
        var currentGroupIndex = 0

        for (importcheck in imports) {
            val group = when {
                importcheck.packageName.startsWith("java.") || importcheck.packageName.startsWith("javax.") -> orderedGroups[0]
                importcheck.packageName.startsWith(projectPackagePrefix) -> orderedGroups[2]
                else -> orderedGroups[1]
            }

            val groupIndex = orderedGroups.indexOf(group)
            if (groupIndex < currentGroupIndex) {
                errors.add("Нарушен порядок импортов в файле в импорте ${importcheck.className} \n" +
                        "\t${context.file.toAbsolutePath()}:${importcheck.node?.startPoint?.row?.plus(
                    1
                )} ")
            } else {
                currentGroupIndex = groupIndex
            }
        }

        return createCheckResponse(errors)
    }
}