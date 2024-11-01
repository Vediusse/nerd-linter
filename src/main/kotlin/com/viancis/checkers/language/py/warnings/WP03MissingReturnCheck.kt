package com.viancis.checkers.language.py.warnings

import com.viancis.checkers.BaseCodeCheck
import com.viancis.utils.Category
import com.viancis.checkers.CodeCheckInfo
import com.viancis.communication.CheckResponse
import com.viancis.communication.CheckerContext
import org.treesitter.TSNode
import java.nio.file.Path

@CodeCheckInfo(code = "WP03", level = Category.WARNING)
class WP03MissingReturnCheck : BaseCodeCheck() {

    override fun check(context: CheckerContext): CheckResponse {
        val errors = mutableListOf<String>()
        val rootNode = context.tree.rootNode

        checkNode(rootNode, context.file, errors)

        return createCheckResponse(errors)
    }

    private fun checkNode(node: TSNode, file: Path, errors: MutableList<String>) {
        if (node.type == "function_definition") {
            val returnType = getReturnType(node)
            if (returnType != "void" && !hasReturnStatement(node)) {
                val line = node.startPoint.row + 1
                errors.add("Метод с возвращаемым типом, но отсутствует инструкция return. Cтрока $line;")
            }
        }

        for (i in 0..<node.childCount) {
            val childNode = node.getChild(i)
            checkNode(childNode, file, errors)
        }
    }

    private fun getReturnType(node: TSNode): String {

        for (i in 0..<node.childCount) {
            val childNode = node.getChild(i)
            if (childNode.type == "type") {
                return "какой-то"
            }
        }
        return "void"
    }

    private fun hasReturnStatement(node: TSNode): Boolean {

        for (i in 0..<node.childCount) {
            val childNode = node.getChild(i)
            if (childNode.type == "return_statement") {
                return true
            }
        }
        return false
    }
}