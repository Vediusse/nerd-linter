package com.viancis.checkers.language.java.category.warnings

import com.viancis.checkers.BaseCodeCheck
import com.viancis.checkers.CodeCheckInfo
import com.viancis.checkers.CodeEntity
import com.viancis.checkers.language.java.entity.JavaCodeEntity
import com.viancis.communication.CheckResponse
import com.viancis.communication.CheckerContext
import com.viancis.utils.Category


@CodeCheckInfo(code = "WJ01", level = Category.WARNING)
class WJ01OverrideAbstract : BaseCodeCheck() {
    override fun check( context:  CheckerContext): CheckResponse {
        val classEntity = context.getClassEntity()
        val errors = mutableListOf<String>()
        val parentMethods = mutableListOf<JavaCodeEntity.JavaMethodEntity>()
        classEntity.mainClass.superClass?.let { superClass: JavaCodeEntity.JavaClassEntity ->
            parentMethods.addAll(superClass.functions)
        }
        for (interfaceClass in classEntity.mainClass.interfaceList) {
            parentMethods.addAll(interfaceClass.functions)
        }
        for (method in classEntity.mainClass.functions) {
            checkMethodOverride(method, parentMethods, classEntity.name, errors, context)

        }
        return createCheckResponse(errors)
    }

    private fun checkMethodOverride(method: JavaCodeEntity.JavaMethodEntity, parentMethods: List<JavaCodeEntity.JavaMethodEntity>, className: String, errors: MutableList<String>, context: CheckerContext) {
        val hasOverride = method.annotations?.contains("Override") == true
        if (!hasOverride) {
            for (parentMethod in parentMethods) {
                if (isMethodSignatureEqual(method, parentMethod)) {
                    errors.add("Метод '${method.name}' в классе '$className' ОБЯЗАН иметь аннотацию @Override.\n\t ${context.file.toAbsolutePath()}:${method.node.startPoint.row + 1} ")
                    break
                }
            }
        }
    }


    private fun isMethodSignatureEqual(method: JavaCodeEntity.JavaMethodEntity, parentMethod: JavaCodeEntity.JavaMethodEntity): Boolean {
        if (method.name != parentMethod.name || method.returnType != parentMethod.returnType) {
            return false
        }
        val methodParams = method.parameters ?: emptyList()
        val parentParams = parentMethod.parameters ?: emptyList()
        if (methodParams.size != parentParams.size) {
            return false
        }
        return methodParams.zip(parentParams).all { (param, parentParam) ->
            param.type == parentParam.type
        }
    }


}