package com.viancis.checkers.language.universal.category.warnings

import com.viancis.checkers.BaseCodeCheck
import com.viancis.utils.Category
import com.viancis.checkers.CodeCheckInfo
import com.viancis.communication.CheckResponse
import com.viancis.communication.CheckerContext
import java.nio.file.Files

@CodeCheckInfo(code = "WU02", level = Category.WARNING)
class WU02ULongLineCheck : BaseCodeCheck() {
    override fun check(context: CheckerContext): CheckResponse {
        val errors = mutableListOf<String>()
        val sourceCode = Files.readString(context.file)

        sourceCode.lines().forEachIndexed { index, line ->
            if (line.length > 80) {
                errors.add("Слишком много кода на одной строке \n" +
                        "\t${context.file.toAbsolutePath()}:${index} ")
            }
        }

        return createCheckResponse(errors)
    }
}