package com.viancis.checkers

import org.treesitter.TSNode

abstract class CodeEntity(
    open val name: String,
    open var type: String? = null,
    open val node: TSNode?,
) {
    abstract override fun toString(): String

    abstract class VariableEntity(
        override val name: String,
        override var type: String?,
        override val node: TSNode
    ) : CodeEntity(name, type, node) {
        override fun toString(): String {
            return "Variable: $name, Type: $type"
        }
    }

    abstract class FunctionEntity(
        override val name: String,
        override var type: String? = "function",
        override val node: TSNode,
        open val parameters: List<VariableEntity>? = null,
        open val localVariables: MutableList<out VariableEntity> = mutableListOf()
    ) : CodeEntity(name, type, node) {
        override fun toString(): String {
            val paramStr = parameters?.joinToString(", ") { "${it.name}: ${it.type}" } ?: "None"
            val varsStr = localVariables.joinToString(", ") { "${it.name}: ${it.type}" }
            return "Function: $name, Parameters: $paramStr, Variables: $varsStr"
        }
    }

    abstract class FieldEntity(
        override val name: String,
        override var type: String?,
        override val node: TSNode,
        open val initializer: Any? = null
    ) : CodeEntity(name, type, node) {
        override fun toString(): String {
            val initStr = initializer?.toString() ?: "No initialization"
            return "Field: $name, Type: $type, Initialization: $initStr"
        }
    }

    abstract class ConstantEntity(
        override val name: String,
        override var type: String? = "constant"
    ) : CodeEntity(name, type, null) {
        override fun toString(): String {
            return "Constant: $name, Type: $type"
        }
    }

    abstract class BinaryExpressionEntity(
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

    abstract class ModuleEntity(
        override var name: String = "unknown module",
        override val node: TSNode,
    ) : CodeEntity(name, "module", node) {}


    abstract class StructureEntity<F : FieldEntity, M : FunctionEntity, S : StructureEntity<F, M, S>>(
        override var name: String = "unknown structure",
        override val node: TSNode,
        override var type: String? = "structure",
        open val fields: MutableList<F> = mutableListOf(),
        open val functions: MutableList<M> = mutableListOf(),
        open val innerStructures: MutableList<S> = mutableListOf()
    ) : CodeEntity(name, type, node) {
        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("Structure: $name\n")
            if (fields.isNotEmpty()) {
                sb.append("Fields:\n")
                fields.forEach { sb.append("  $it\n") }
            }
            if (functions.isNotEmpty()) {
                sb.append("Functions:\n")
                functions.forEach { sb.append("  $it\n") }
            }
            return sb.toString()
        }
    }

    abstract class FileEntity(
        override var name: String = "unknown file",
        override val node: TSNode,
    ) : CodeEntity(name, "file", node)

    abstract class ImportEntity(
        override var name: String = "unknown file",
        override val node: TSNode,
    ): CodeEntity(name = name, "import", node) {


    }
}