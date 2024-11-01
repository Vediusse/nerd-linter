package com.viancis.checkers.language.java

import com.viancis.checkers.language.java.entity.JavaCodeEntity
import com.viancis.utils.Category
import com.viancis.utils.Printer

object ProcessEntity {

    fun processEntityMap(entityMap: MutableMap<String, JavaCodeEntity.JavaFileEntity>) {
        entityMap.forEach { (_, javaFileEntity) ->
            resolveSuperClassAndInterfaces(javaFileEntity, entityMap)
        }
    }

    private fun resolveSuperClassAndInterfaces(javaFileEntity: JavaCodeEntity.JavaFileEntity, entityMap: MutableMap<String, JavaCodeEntity.JavaFileEntity>) {
        val resolvedEntities = mutableSetOf<String>()
        val visited = mutableSetOf<String>()
        resolveSuperClass(javaFileEntity, resolvedEntities, visited, entityMap)
        resolveInterfaces(javaFileEntity, resolvedEntities, visited, entityMap)
    }

    private fun resolveSuperClass(javaFileEntity: JavaCodeEntity.JavaFileEntity, resolvedEntities: MutableSet<String>, visited: MutableSet<String>, entityMap: MutableMap<String, JavaCodeEntity.JavaFileEntity>) {
        javaFileEntity.mainClass.superClass?.let { superClass ->
            val superClassName = superClass.name
            if (!visited.contains(superClassName)) {
                visited.add(superClassName)
                if (resolvedEntities.add(superClassName)) {
                    findEntityInImportsOrMap(superClassName, javaFileEntity, entityMap)?.let { resolvedSuperClass ->
                        javaFileEntity.mainClass.superClass = resolvedSuperClass.mainClass
                        resolveSuperClass(resolvedSuperClass, resolvedEntities, visited, entityMap)
                    } ?: run {
                        Printer.printEnhanced(Category.ERROR, "Класс $superClassName не найден ни в импортах, ни в текущем пакете")
                    }
                } else {
                    Printer.printEnhanced(Category.ERROR, "Обнаружена циклическая зависимость с классом $superClassName")
                }
                visited.remove(superClassName)
            } else {
                Printer.printEnhanced(Category.ERROR, "Обнаружена циклическая зависимость с классом $superClassName")
            }
        }
    }

    private fun resolveInterfaces(javaFileEntity: JavaCodeEntity.JavaFileEntity, resolvedEntities: MutableSet<String>, visited: MutableSet<String>, entityMap: MutableMap<String, JavaCodeEntity.JavaFileEntity>) {
        javaFileEntity.mainClass.interfaceList.forEachIndexed { index, interfaceEntity ->
            val fullInterfaceName = interfaceEntity.name
            if (!visited.contains(fullInterfaceName)) {
                visited.add(fullInterfaceName)
                if (resolvedEntities.add(fullInterfaceName)) {
                    findEntityInImportsOrMap(fullInterfaceName, javaFileEntity, entityMap)?.let { resolvedInterface ->
                        javaFileEntity.mainClass.interfaceList[index] = resolvedInterface.mainClass
                        resolveExtendedInterfaces(resolvedInterface, resolvedEntities, visited, entityMap)
                    } ?: run {
                        Printer.printEnhanced(Category.ERROR, "Интерфейс $fullInterfaceName не найден ни в импортах, ни в текущем пакете")
                    }
                } else {
                    Printer.printEnhanced(Category.ERROR, "Обнаружена циклическая зависимость с интерфейсом $fullInterfaceName")
                }
                visited.remove(fullInterfaceName)
            } else {
                Printer.printEnhanced(Category.ERROR, "Обнаружена циклическая зависимость с интерфейсом $fullInterfaceName")
            }
        }
    }

    private fun resolveExtendedInterfaces(interfaceEntity: JavaCodeEntity.JavaFileEntity, resolvedEntities: MutableSet<String>, visited: MutableSet<String>, entityMap: MutableMap<String, JavaCodeEntity.JavaFileEntity>) {
        interfaceEntity.mainClass.interfaceList.forEachIndexed { index, superInterface ->
            val superInterfaceName = superInterface.name
            if (!visited.contains(superInterfaceName)) {
                visited.add(superInterfaceName)
                if (resolvedEntities.add(superInterfaceName)) {
                    findEntityInImportsOrMap(superInterfaceName, interfaceEntity, entityMap)?.let { resolvedSuperInterface ->
                        interfaceEntity.mainClass.interfaceList[index] = resolvedSuperInterface.mainClass
                        resolveExtendedInterfaces(resolvedSuperInterface, resolvedEntities, visited, entityMap)
                    } ?: run {
                        Printer.printEnhanced(Category.ERROR, "Суперинтерфейс $superInterfaceName не найден ни в импортах, ни в текущем пакете")
                    }
                } else {
                    Printer.printEnhanced(Category.ERROR, "Обнаружена циклическая зависимость с суперинтерфейсом $superInterfaceName")
                }
                visited.remove(superInterfaceName)
            } else {
                Printer.printEnhanced(Category.ERROR, "Обнаружена циклическая зависимость с суперинтерфейсом $superInterfaceName")
            }
        }
    }

    private fun findEntityInImportsOrMap(name: String, javaFileEntity: JavaCodeEntity.JavaFileEntity, entityMap: MutableMap<String, JavaCodeEntity.JavaFileEntity>): JavaCodeEntity.JavaFileEntity? {
        val classNameWithoutPackage = name.substringAfterLast('.')
        val samePackageEntity = entityMap["${javaFileEntity.packageName}.$classNameWithoutPackage"]
        if (samePackageEntity != null) {
            return samePackageEntity
        }
        val matchingImports = javaFileEntity.imports.filter { it.name.endsWith(classNameWithoutPackage) }
        if (matchingImports.size > 1) {
            Printer.printEnhanced(Category.ERROR, "Найдено несколько импортов с именем $name")
        }
        if (matchingImports.size == 1) {
            val fullClassName = "${matchingImports.first().packageName}.$classNameWithoutPackage"
            return entityMap[fullClassName]
        }

        Printer.printEnhanced(Category.ERROR, "Класс $name не найден ни в импортах, ни в текущем пакете")
        return null
    }
}