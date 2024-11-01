package com.viancis.checkers.interfaces

import com.viancis.communication.CheckResponse
import com.viancis.communication.CheckerContext

interface CodeCheck {
    fun check(
        context:CheckerContext
    ): CheckResponse
}


