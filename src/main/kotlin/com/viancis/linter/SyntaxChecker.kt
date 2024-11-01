package com.viancis.linter

import org.treesitter.TSNode
import org.treesitter.TSTree
import java.nio.file.Path

class SyntaxChecker {

    fun checkSyntax(tree: TSTree, file: Path): List<String> {
        val rootNode = tree.rootNode
        val errorMessages = mutableListOf<String>()

        if (rootNode.childCount == 0) {
            errorMessages.add("Файл ${file.fileName} пустой или содержит синтаксические ошибки.")
        } else {
            val errorNodes = findErrorNodes(rootNode)
            if (errorNodes.isNotEmpty()) {
                errorMessages.add("Обнаружены синтаксические ошибки в файле ${file.fileName}:")
                errorNodes.forEach { node ->
                    errorMessages.add("Ошибка на узле: ${node.type}, текст:")
                }
            }
        }
        return errorMessages
    }

    private fun findErrorNodes(rootNode: TSNode): List<TSNode> {
        val errorNodes = mutableListOf<TSNode>()

        for (i in 0..<rootNode.childCount) {
            val childNode = rootNode.getChild(i)
            if (childNode.type == "ERROR") {
                errorNodes.add(childNode)
            }
            errorNodes.addAll(findErrorNodes(childNode))
        }
        return errorNodes
    }
}