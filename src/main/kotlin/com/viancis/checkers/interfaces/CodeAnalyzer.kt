package com.viancis.checkers.interfaces

import com.viancis.checkers.CodeEntity
import org.treesitter.TSTree
import java.nio.file.Path

interface CodeAnalyzer {
    fun analyzeCode(tree: TSTree, file: Path): CodeEntity.FileEntity
}