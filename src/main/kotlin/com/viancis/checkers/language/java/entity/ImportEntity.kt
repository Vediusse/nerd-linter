package com.viancis.checkers.language.java.entity

data class ImportEntity(
    val isStatic: Boolean,
    val packageName: String,
    val className: String
)