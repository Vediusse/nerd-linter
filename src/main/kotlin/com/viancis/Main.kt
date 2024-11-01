package com.viancis

import com.viancis.commands.CommandLineOptions
import com.viancis.linter.Linter
import com.viancis.communication.Request

fun main(args: Array<String>) {
    val options = CommandLineOptions(args)
    options.parse()
    if (options.help) return
    val request = Request(options.source, options.config)
    val linter = Linter()
    linter.lint(request)
}