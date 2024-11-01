package com.viancis.checkers.language.java.category.errors

import com.viancis.checkers.BaseCodeCheck
import com.viancis.utils.Category
import com.viancis.checkers.CodeCheckInfo
import com.viancis.checkers.language.java.annotations.JavaClass
import com.viancis.communication.CheckResponse
import com.viancis.communication.CheckerContext
import com.viancis.checkers.CodeEntity
import com.viancis.checkers.language.java.entity.JavaCodeEntity
import org.treesitter.TSNode

@CodeCheckInfo(code = "EJ01", level = Category.ERROR)
class EJ01MissingReturnCheck : BaseCodeCheck() {

    @JavaClass
    override fun check(context: CheckerContext): CheckResponse {
        val errors = mutableListOf<String>()
        val classEntity = context.getClassEntity()

        classEntity.mainClass.functions.forEach { method ->
            if ("abstract" in method.keywords) return@forEach
            val returnType = method.returnType ?: "void"
            if (returnType != "void" && !hasReturnStatement(method)) {
                val line = method.node.startPoint.row + 1
                errors.add("Метод '${method.name}' с возвращаемым типом '$returnType', но отсутствует инструкция return.\n\t ${context.file.toAbsolutePath()}:$line ")
            }
        }
        return createCheckResponse(errors)
    }

    private fun hasReturnStatement(method: JavaCodeEntity.JavaMethodEntity): Boolean {
        return checkReturnCoverage(method.node)
    }

    private fun checkReturnCoverage(node: TSNode): Boolean {

        for (i in 0..<node.childCount) {
            val childNode = node.getChild(i)

            when (childNode.type) {
                "return_statement" -> return true
                "block" -> {
                    if (checkReturnCoverage(childNode)) return true
                }
                "if_statement", "else" -> {
                    val consequence = childNode.getChild(2)
                    val consequenceHasReturn = consequence?.let { checkReturnCoverage(it) } ?: false
                    val alternativeIndex = 2

                    val alternativeHasReturn = if (childNode.childCount > alternativeIndex) {
                        checkReturnCoverage(childNode.getChild(alternativeIndex))
                    } else false
                    if (consequenceHasReturn && alternativeHasReturn) return true
                }
                "for_statement", "while_statement" -> {
                    val loopBody = childNode.getChild(1)
                    if (loopBody != null && checkReturnCoverage(loopBody)) return true
                }
            }
        }
        return false
    }
}