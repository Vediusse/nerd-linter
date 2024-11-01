package com.viancis.checkers.language.universal.category.warnings

import com.viancis.checkers.BaseCodeCheck
import com.viancis.utils.Category
import com.viancis.checkers.CodeCheckInfo
import com.viancis.communication.CheckResponse
import com.viancis.communication.CheckerContext

@CodeCheckInfo(code = "WU01", level = Category.WARNING)
class WU01UCommentCheck : BaseCodeCheck() {
    override fun check(context: CheckerContext): CheckResponse {
        val errors = mutableListOf<String>()
        val rootNode = context.tree.rootNode
        checkNodeList(rootNode, context.file, errors, listOf("line_comment", "comment"), "Кажется я вижу комменатрий в ${context.file.fileName} ")

        return createCheckResponse(errors)
    }
}