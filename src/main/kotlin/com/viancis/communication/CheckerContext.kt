package com.viancis.communication


import com.viancis.checkers.language.java.entity.JavaCodeEntity
import org.treesitter.TSTree
import java.nio.file.Path

data class CheckerContext(
    val tree: TSTree,
    val file: Path,
    val parsedFilesByPackage: Map<String, JavaCodeEntity.JavaFileEntity>,
    val packageName: String
){
    fun getClassEntity(): JavaCodeEntity.JavaFileEntity {
        return this.parsedFilesByPackage[this.packageName]
            ?: throw IllegalArgumentException("Не нашли пакета ${this.packageName}")
    }
}
