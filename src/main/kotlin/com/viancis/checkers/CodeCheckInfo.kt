package com.viancis.checkers

import com.viancis.utils.Category

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CodeCheckInfo(val code: String, val level: Category)
