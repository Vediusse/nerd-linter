package com.viancis.communication

import com.viancis.utils.Category

data class CheckResponse(
    val code: String,
    val level: Category,
    val errors: List<String>
)
