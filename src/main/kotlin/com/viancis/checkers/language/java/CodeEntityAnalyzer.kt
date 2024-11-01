package com.viancis.checkers.language.java

import com.viancis.checkers.interfaces.CodeAnalyzer
import com.viancis.checkers.language.java.entity.Analyzer
import com.viancis.checkers.CodeEntity
import com.viancis.checkers.language.java.entity.JavaCodeEntity
import org.treesitter.TSTree
import org.treesitter.TSNode
import java.nio.file.Files
import java.nio.file.Path


@Analyzer
class CodeEntityAnalyzer: CodeAnalyzer {

    override fun analyzeCode(tree: TSTree, file: Path): JavaCodeEntity.JavaFileEntity {
        val sourceCode = readSourceCode(file)
        val fileEntity = JavaCodeEntity.JavaFileEntity(
            name = file.fileName.toString(),
            node = tree.rootNode,
            mainClass = JavaCodeEntity.JavaClassEntity(name = "Да я щас пиво пойду пить", node = tree.rootNode)
        )
        traverseNodes(tree.rootNode, fileEntity, sourceCode)
        return fileEntity
    }


    private fun readSourceCode(file: Path): String = Files.readString(file)


    private fun TSNode.children(): List<TSNode> = (0..<this.childCount).map{this.getChild(it)}


    private fun traverseNodes(node: TSNode, fileEntity: JavaCodeEntity.JavaFileEntity, sourceCode: String) {
        val nodeType = node.getType()

        when (nodeType) {
            "package_declaration" -> handlePackageDeclaration(node, fileEntity, sourceCode)
            "import_declaration" -> handleImportDeclaration(node, fileEntity, sourceCode)
            "interface_declaration", "class_declaration" -> {
                handleTypeDeclaration(node = node, sourceCode =  sourceCode, fileEntity = fileEntity, isInterface = nodeType == "interface_declaration")
                return
            }

        }

        for (i in 0..<node.childCount) {
            try {
                traverseNodes(node.getChild(i), fileEntity, sourceCode)
            } catch (e: Exception) {
                println("Ошибка при обработке узла: ${e.message}")
            }
        }
    }




    private fun handlePackageDeclaration(node: TSNode, fileEntity: JavaCodeEntity.JavaFileEntity, sourceCode: String) {
        val packageName = node.children()
            .firstOrNull { it.type == "scoped_identifier" }
            ?.let { extractNodeTextByPoints(it, sourceCode) }

        if (packageName != null) {
            fileEntity.packageName = packageName
        }
    }

    private fun handleImportDeclaration(node: TSNode, fileEntity: JavaCodeEntity.JavaFileEntity, sourceCode: String) {
        var isStatic = false
        var importName = ""

        node.children().forEach { child ->
            when (child.type) {
                "static" -> isStatic = true
                "scoped_identifier" -> {
                    importName = extractNodeTextByPoints(child, sourceCode)
                    val parts = importName.split(".")
                    val className = parts.last()
                    val packageName = parts.dropLast(1).joinToString(".")
                    fileEntity.imports.add(JavaCodeEntity.JavaImportEntity(className = className, static = isStatic, packageName = packageName, node = node))
                }
            }
        }
    }


    private fun handleTypeDeclaration(
        node: TSNode,
        fileEntity: JavaCodeEntity.JavaFileEntity,
        sourceCode: String,
        isInterface: Boolean
    ) : JavaCodeEntity.JavaClassEntity {
        var accessModifier = "null"
        var className = "UnnamedClass"
        val type = if (isInterface) "interface" else "class"
        val keywords = mutableListOf<String>()
        val annotations = mutableListOf<String>()
        var superClass: JavaCodeEntity.JavaClassEntity? = null
        val interfaceList = mutableListOf<JavaCodeEntity.JavaClassEntity>()

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            when (child.type) {
                "modifiers" -> processModifiers(child, annotations, keywords, sourceCode).also { accessModifier = it }
                "identifier" -> className = extractNodeTextByPoints(child, sourceCode)
                "superclass" -> superClass = JavaCodeEntity.JavaClassEntity(name = extractNodeTextByPoints(child.getChild(1), sourceCode), node = child)
                "extends_interfaces", "super_interfaces" -> processInterfaces(child, interfaceList, sourceCode)
            }
        }


        val classEntity = JavaCodeEntity.JavaClassEntity(
            name = className,
            type = type,
            modifiers = accessModifier,
            keywords = keywords,
            annotations = annotations,
            superClass = superClass,
            interfaceList = interfaceList,
            node = node
        )


        fileEntity.mainClass = classEntity


        for (i in 0..<node.childCount) {
            if (node.getChild(i).type == "class_body" || node.getChild(i).type == "interface_body") {
                processClassBody(node.getChild(i), classEntity, sourceCode, isInterface)
            }
        }
        return classEntity;
    }

    private fun handleTypeDeclaration(
        node: TSNode,
        classEntity: JavaCodeEntity.JavaClassEntity,
        sourceCode: String,
        isInterface: Boolean
    ) : JavaCodeEntity.JavaClassEntity {
        var accessModifier = "null"
        var className = "UnnamedClass"
        val type = if (isInterface) "interface" else "class"
        val keywords = mutableListOf<String>()
        val annotations = mutableListOf<String>()
        var superClass: JavaCodeEntity.JavaClassEntity? = null
        val interfaceList = mutableListOf<JavaCodeEntity.JavaClassEntity>()

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            when (child.type) {
                "modifiers" -> processModifiers(child, annotations, keywords, sourceCode).also { accessModifier = it }
                "identifier" -> className = extractNodeTextByPoints(child, sourceCode)
                "superclass" -> superClass = JavaCodeEntity.JavaClassEntity(name = extractNodeTextByPoints(child.getChild(1), sourceCode), node = child)
                "extends_interfaces", "super_interfaces" -> processInterfaces(child, interfaceList, sourceCode)
            }
        }


        val classEntity = JavaCodeEntity.JavaClassEntity(
            name = className,
            type = type,
            modifiers = accessModifier,
            keywords = keywords,
            annotations = annotations,
            superClass = superClass,
            interfaceList = interfaceList,
            node = node
        )


        classEntity.interfaceList = interfaceList


        for (i in 0..<node.childCount) {
            if (node.getChild(i).type == "class_body" || node.getChild(i).type == "interface_body") {
                processClassBody(node.getChild(i), classEntity, sourceCode, isInterface)
            }
        }
        return classEntity;
    }

    private fun processModifiers(
        node: TSNode,
        annotations: MutableList<String>,
        keywords: MutableList<String>,
        sourceCode: String
    ): String {
        var accessModifier = "null"
        for (j in 0..<node.childCount) {
            val modifierNode = node.getChild(j)
            when (modifierNode.type) {
                "marker_annotation" -> {
                    extractNodeTextByPoints(modifierNode.getChild(1), sourceCode)?.let { annotations.add(it) }
                }
                "public", "private", "protected", "default" -> {
                    accessModifier = extractNodeTextByPoints(modifierNode, sourceCode)
                }
                "static", "final", "abstract" -> {
                    keywords.add(extractNodeTextByPoints(modifierNode, sourceCode))
                }
            }
        }
        return accessModifier
    }

    private fun processInterfaces(node: TSNode, interfaceList: MutableList<JavaCodeEntity.JavaClassEntity>, sourceCode: String) {
        for (j in 0..<node.getChild(1).childCount) {
            val interfaceNode = node.getChild(1).getChild(j)
            if (interfaceNode.type != ",") {
                val interfaceClass = JavaCodeEntity.JavaClassEntity(name = extractNodeTextByPoints(interfaceNode, sourceCode), node = node)
                interfaceList.add(interfaceClass)
            }
        }
    }




    private fun processClassBody(node: TSNode, classEntity: JavaCodeEntity.JavaClassEntity, sourceCode: String, isInterface: Boolean) {
        for (j in 0 until node.childCount) {
            when (node.getChild(j).type) {
                "method_declaration" -> handleMethodDeclaration(node.getChild(j), classEntity, sourceCode, isInterface)
                "field_declaration" -> handleFieldDeclaration(node.getChild(j), classEntity, sourceCode)
                "interface_declaration", "class_declaration" -> {
                    handleTypeDeclaration(node = node, sourceCode =  sourceCode, classEntity = classEntity, isInterface = node.getChild(j).type == "interface_declaration")
                }
            }
        }
    }

    private fun handleMethodDeclaration(
        node: TSNode,
        classEntity: JavaCodeEntity.JavaClassEntity,
        sourceCode: String,
        isInterface: Boolean
    ) {
        var accessModifier = "null"
        var returnType: String? = "void"
        var methodName: String = "unnamedMethod"
        val keywords = mutableListOf<String>()
        val annotations = mutableListOf<String>()
        val parameters = mutableListOf<JavaCodeEntity.JavaVariableEntity>()
        val vars = mutableListOf<JavaCodeEntity.JavaVariableEntity>()

        node.children().forEach { child ->
            when (child.type) {
                "modifiers" -> {
                    child.children().forEach { modifierChild ->
                        when (modifierChild.type) {
                            "marker_annotation" -> {
                                modifierChild.getChild(1)?.let { annotationNode ->
                                    extractNodeTextByPoints(annotationNode, sourceCode)?.let { annotations.add(it) }
                                }
                            }
                            "public", "private", "protected", "default" -> {
                                accessModifier = extractNodeTextByPoints(modifierChild, sourceCode)
                            }
                            "static", "final", "abstract", "synchronized" -> {
                                keywords.add(extractNodeTextByPoints(modifierChild, sourceCode))
                            }
                        }
                    }
                }
                "integral_type", "type_identifier" -> {
                    returnType = extractNodeTextByPoints(child, sourceCode)
                }
                "identifier" -> {
                    methodName = extractNodeTextByPoints(child, sourceCode)
                }
                "formal_parameters" -> {
                    child.children().forEach { paramNode ->
                        if (paramNode.type == "formal_parameter") {
                            val paramType = extractNodeTextByPoints(paramNode.getChild(0), sourceCode)
                            val paramName = extractNodeTextByPoints(paramNode.getChild(1), sourceCode)
                            if (paramType != null && paramName != null) {
                                parameters.add(JavaCodeEntity.JavaVariableEntity(paramName, paramType, paramNode))
                            }
                        }
                    }
                }
                "block" -> {
                    handleVariableDeclarator(child, vars, sourceCode)
                }
            }
        }


        if (isInterface && !keywords.contains("abstract")) {
            keywords.add("abstract")
        }

        classEntity.functions.add(
            JavaCodeEntity.JavaMethodEntity(
                name = methodName,
                annotations = if (annotations.isEmpty()) null else annotations,
                returnType = returnType ?: "void",
                accessModifier = accessModifier,
                parameters = parameters,
                keywords = keywords,
                localVariables = vars,
                node = node
            )
        )
    }

    private fun handleVariableDeclarator(
        node: TSNode,
        vars: MutableList<JavaCodeEntity.JavaVariableEntity>,
        sourceCode: String
    ) {
        var typeVar = "Unknown"
        var nameVar = "Unknown"
        node.children().forEach { childNode ->
            when (childNode.type) {
                "local_variable_declaration" -> {
                    childNode.children().forEach { subChildNode ->
                        when (subChildNode.type) {
                            "integral_type", "type_identifier" -> {
                                typeVar = extractNodeTextByPoints(subChildNode, sourceCode)
                            }
                            "variable_declarator" -> {
                                nameVar = extractNodeTextByPoints(subChildNode.getChild(0), sourceCode)
                            }
                        }
                    }
                    vars.add(JavaCodeEntity.JavaVariableEntity(typeVar, nameVar, childNode))
                }
                "block", "if_statement", "else" -> {
                    handleVariableDeclarator(childNode, vars, sourceCode)
                }
            }
        }
    }

    private fun parseInitializer(node: TSNode, sourceCode: String): CodeEntity? {
        return when (node.type) {
            "method_invocation" -> handleMethodInvocationEntity(node, sourceCode)
            "decimal_integer_literal", "string_literal", "boolean_literal" -> {
                JavaCodeEntity.JavaConstantEntity(name = extractNodeTextByPoints(node, sourceCode))
            }

            "binary_expression" -> {
                val left = parseInitializer(node.getChild(0), sourceCode)
                val right = parseInitializer(node.getChild(2), sourceCode)
                JavaCodeEntity.JavaBinaryExpressionEntity(left, right, extractNodeTextByPoints(node.getChild(1), sourceCode))
            }

            "identifier" -> {
                JavaCodeEntity.JavaVariableEntity(name = extractNodeTextByPoints(node, sourceCode), type = node.type, node = node)
            }
            else -> null
        }
    }


    private fun handleMethodInvocationEntity(node: TSNode, sourceCode: String): JavaCodeEntity.JavaCallbackEntity {
        val methodName = extractNodeTextByPoints(node.getChild(0), sourceCode)
        val argumentsNode = node.getChild(1)
        val parameters = mutableListOf<CodeEntity>()

        argumentsNode.children().forEach { argNode ->
            if (argNode.type != ",") {
                val param: CodeEntity? = when (argNode.type) {
                    "method_invocation" -> handleMethodInvocationEntity(argNode, sourceCode)
                    "decimal_integer_literal", "string_literal", "boolean_literal" -> {
                        JavaCodeEntity.JavaConstantEntity(name = extractNodeTextByPoints(argNode, sourceCode))
                    }
                    "identifier" -> {
                        JavaCodeEntity.JavaVariableEntity(
                            name = extractNodeTextByPoints(argNode, sourceCode),
                            type = "identifier",
                            node = argNode
                        )
                    }
                    else -> null
                }

                param?.let { parameters.add(it) }
            }
        }

        return JavaCodeEntity.JavaCallbackEntity(
            name = methodName,
            returnType = null,
            parameters = parameters
        )
    }

    private fun handleFieldDeclaration(node: TSNode, classEntity: JavaCodeEntity.JavaClassEntity, sourceCode: String) {
        var accessModifier = "null"
        var variableType = "void"
        var variableName = "unnamedVar"
        val keywords = mutableListOf<String>()
        val annotations = mutableListOf<String>()
        var initializer: CodeEntity? = null
        fun handleModifiers(modifiersNode: TSNode) {
            modifiersNode.children().forEach { modifierChild ->
                when (modifierChild.type) {
                    "marker_annotation" -> {
                        modifierChild.getChild(1)?.let { extractNodeTextByPoints(it, sourceCode) }?.let {
                            annotations.add(it)
                        }
                    }
                    "public", "private", "protected" -> {
                        accessModifier = extractNodeTextByPoints(modifierChild, sourceCode)
                    }
                    "static", "final", "abstract", "synchronized" -> {
                        keywords.add(extractNodeTextByPoints(modifierChild, sourceCode))
                    }
                }
            }
        }

        node.children().forEach { child ->
            when (child.type) {
                "modifiers" -> handleModifiers(child)
                "integral_type", "type_identifier" -> {
                    variableType = extractNodeTextByPoints(child, sourceCode)
                }
                "variable_declarator" -> {
                    variableName = extractNodeTextByPoints(child.getChild(0), sourceCode)
                    if (child.childCount > 2) {
                        val initializerNode = child.getChild(2)
                        initializer = parseInitializer(initializerNode, sourceCode)
                    }
                }
            }
        }

        classEntity.fields.add(
            JavaCodeEntity.JavaFieldEntity(
                accessModifier = accessModifier,
                keywords = keywords,
                type = variableType,
                name = variableName,
                initializer = initializer,
                node = node
            )
        )
    }

    private fun extractNodeTextByPoints(node: TSNode, sourceCode: String): String {
        val startPoint = node.startPoint
        val endPoint = node.endPoint

        val lines = sourceCode.lines()
        val startLine = lines.getOrNull(startPoint.row)
        val endLine = lines.getOrNull(endPoint.row)

        if (startLine == null || endLine == null) {
            throw IllegalArgumentException("Invalid line range for node")
        }

        return if (startPoint.row == endPoint.row) {
            startLine.substring(startPoint.column, endPoint.column)
        } else {
            val startText = startLine.substring(startPoint.column)
            val endText = endLine.substring(0, endPoint.column)
            startText + lines.subList(startPoint.row + 1, endPoint.row).joinToString("\n") + endText
        }
    }
}