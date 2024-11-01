package com.viancis.checkers

import com.viancis.checkers.interfaces.CodeCheck
import com.viancis.communication.CheckResponse
import org.treesitter.TSNode
import java.nio.file.Path
import kotlin.reflect.full.findAnnotation

abstract class BaseCodeCheck : CodeCheck {

    fun checkNode(node: TSNode, file: Path, errors: MutableList<String>, errorType: String, errorMessage: String) {
        if (node.type == errorType) {
            val line = node.startPoint.row + 1
            errors.add("$errorMessage Cтрока $line;")
        }

        for (i in 0..<node.childCount) {
            val childNode = node.getChild(i)
            checkNode(childNode, file, errors, errorType, errorMessage)
        }
    }

    fun checkNodeList(
        node: TSNode,
        file: Path,
        errors: MutableList<String>,
        errorTypes: List<String>,
        errorMessage: String
    ) {

        if (errorTypes.contains(node.type)) {
            val line = node.startPoint.row + 1
            errors.add("$errorMessage \n" +
                    "\t ${file.toAbsolutePath()}:$line:${node.startPoint.column + 1} ")
        }


        for (i in 0..<node.childCount) {
            val childNode = node.getChild(i)
            checkNodeList(childNode, file, errors, errorTypes, errorMessage)
        }
    }

    private fun TSNode.children(): List<TSNode> = (0..<this.childCount).map{this.getChild(it)}

    fun createCheckResponse(errors: List<String>): CheckResponse {
        val annotation = this::class.findAnnotation<CodeCheckInfo>()
            ?: throw IllegalStateException("Чекер ${this::class.simpleName} должен быть аннотирован @CodeCheckInfo")

        return CheckResponse(
            code = annotation.code,
            level = annotation.level,
            errors = errors
        )
    }



}