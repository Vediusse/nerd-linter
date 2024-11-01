package com.viancis.checkers.language.java.entity

import com.viancis.checkers.CodeEntity
import org.treesitter.TSNode

open class JavaCodeEntity : CodeEntity("JavaCode", "Java", null) {
    open class JavaVariableEntity(
        override val name: String,
        override var type: String?,
        override val node: TSNode
    ) : CodeEntity.VariableEntity(name, type, node)

    open class JavaMethodEntity(
        override val name: String,
        override var type: String? = "method",
        override val node: TSNode,
        val returnType: String? = null,
        val annotations: List<String>? = null,
        val accessModifier: String = "null",
        val keywords: List<String> = emptyList(),
        override val parameters: MutableList<JavaVariableEntity>,
        override val localVariables: MutableList<JavaVariableEntity> = mutableListOf()
    ) : CodeEntity.FunctionEntity(name, type, node, parameters, localVariables)

    open class JavaFieldEntity(
        override val name: String,
        override var type: String?,
        override val node: TSNode,
        val keywords: List<String> = emptyList(),
        val accessModifier: String = "public",
        override val initializer: Any? = null
    ) : CodeEntity.FieldEntity(name, type, node)

    class JavaConstantEntity(
        override val name: String,
        override var type: String? = "constant"
    ) : CodeEntity.ConstantEntity(name, type) {
        override fun toString(): String {
            return "Constant: $name, Type: $type"
        }
    }

    open class JavaCallbackEntity(
        override val name: String,
        override var type: String? = "callback",
        val returnType: String? = null,
        val accessModifier: String = "null",
        val parameters: List<CodeEntity>? = null,
        val keywords: List<String> = emptyList()
    ) : CodeEntity(name, returnType, null) {
        override fun toString(): String {
            val paramStr = parameters?.joinToString(", ") {
                when (it) {
                    is VariableEntity -> "${it.name}: ${it.type}"
                    is JavaMethodEntity -> "Method: ${it.name}(${it.parameters?.joinToString(", ") { p -> "${p.name}: ${p.type}" }})"
                    else -> "Unknown"
                }
            } ?: "Нет"
            return "Callback: $name, Возврат: $returnType, Доступ: $accessModifier, Параметры: $paramStr, Ключевые слова: ${keywords.joinToString(", ")}"
        }
    }

    class JavaBinaryExpressionEntity(
        open val leftOperand: CodeEntity?,
        open val rightOperand: CodeEntity?,
        open val operator: String
    ) : CodeEntity("binary_expression", "expression", null) {
        override fun toString(): String {
            val leftStr = leftOperand?.toString() ?: "None"
            val rightStr = rightOperand?.toString() ?: "None"
            return "($leftStr $operator $rightStr)"
        }
    }


    open class JavaImportEntity(
        override val node: TSNode,
        val static: Boolean,
        val packageName: String,
        val className: String
    ) : CodeEntity.ImportEntity(className,  node) {
        override fun toString(): String {
            return "Импорт: $packageName.$className, Статический: $static"
        }
    }

    open class JavaFileEntity(
        override var name: String = "unknown file",
        override val node: TSNode,
        var packageName: String = "",
        val imports: MutableList<JavaImportEntity> = mutableListOf(),
        var classes: MutableList<JavaClassEntity> = mutableListOf(),
        var mainClass: JavaClassEntity
    ) : CodeEntity.FileEntity(name, node) {
        override fun toString(): String {
            val importDescriptions = imports.joinToString("\n") { it.toString() }
            val classDescriptions = classes.joinToString("\n") { it.toString() }

            return """
                FileEntity(name='$name', packageName='$packageName'):
                Imports:
                $importDescriptions
                
                Classes:
                $classDescriptions
            """.trimIndent()
        }
    }

    open class JavaClassEntity(
        override var name: String = "unknown class",
        override val node: TSNode,
        override var type: String? = "class",
        var modifiers: String = "null",
        var keywords: List<String> = mutableListOf(),
        override val fields: MutableList<JavaFieldEntity> = mutableListOf(),
        override val functions: MutableList<JavaMethodEntity> = mutableListOf(),
        override val innerStructures: MutableList<JavaClassEntity> = mutableListOf(), // Исправлено здесь
        var superClass: JavaClassEntity? = null,
        val annotations: List<String>? = null,
        var interfaceList: MutableList<JavaClassEntity> = mutableListOf(),
    ) : CodeEntity.StructureEntity<JavaFieldEntity, JavaMethodEntity, JavaClassEntity>(name, node, type, fields, functions, innerStructures) {
        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("Класс: $name\n")
            if (interfaceList.isNotEmpty()) {
                sb.append("Интерфейсы: ${interfaceList.joinToString(", ") { it.name }}\n")
            }
            superClass?.let {
                sb.append("Родительский класс: ${it.name}\n")
            }
            if (fields.isNotEmpty()) {
                sb.append("Поля:\n")
                fields.forEach { sb.append("  $it\n") }
            }
            if (functions.isNotEmpty()) {
                sb.append("Методы:\n")
                functions.forEach { sb.append("  $it\n") }
            }
            return sb.toString()
        }
    }

    override fun toString(): String {
        TODO("Not yet implemented")
    }
}