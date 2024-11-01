package com.viancis.checkers.language.universal.category.errors

import com.viancis.checkers.BaseCodeCheck
import com.viancis.utils.Category
import com.viancis.checkers.CodeCheckInfo
import com.viancis.communication.CheckResponse
import com.viancis.communication.CheckerContext

@CodeCheckInfo(code = "EU01", level = Category.ERROR)
class EU01USyntaxErrorCheck : BaseCodeCheck() {
    override fun check(context: CheckerContext): CheckResponse {
        val errors = mutableListOf<String>()
        val rootNode = context.tree.rootNode
        checkNode(rootNode, context.file, errors, "ERROR", "Синтаксическая ошибка.")

        return createCheckResponse(errors)
    }
}