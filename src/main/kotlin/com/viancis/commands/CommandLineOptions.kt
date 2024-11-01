package com.viancis.commands

import com.viancis.config.Config
import com.viancis.config.ConfigLoader
import joptsimple.OptionParser
import joptsimple.OptionSet
import java.io.File

class CommandLineOptions(private val args: Array<String>) {
    var verbose: Int = 0
        private set
    var help: Boolean = false
        private set
    var source: String = "."
        private set
    var configFile: String? = null
        private set
    lateinit var config: Config
        private set

    fun parse() {
        val parser = OptionParser()

        parser.accepts("help").forHelp()
        parser.accepts("verbose")
            .withRequiredArg()
            .ofType(Int::class.java)
            .defaultsTo(0)
            .describedAs("устанавливает уровень вывода (по умолчанию 0)")
        parser.accepts("source")
            .withRequiredArg()
            .ofType(String::class.java)
            .defaultsTo(".")
            .describedAs("устанавливает путь источника (по умолчанию текущая директория)")

        parser.accepts("conf")
            .withRequiredArg()
            .ofType(String::class.java)
            .describedAs("устанавливает путь к конфигурационному файлу")

        val options: OptionSet = parser.parse(*args)

        if (options.has("help")) {
            help = true
            printHelp(parser)
            return
        }

        verbose = options.valueOf("verbose") as Int
        source = options.valueOf("source") as String


        if (options.has("conf")) {
            configFile = options.valueOf("conf") as String
        } else {
            val homeConfigFile = File("${System.getProperty("user.home")}/nerd.conf")
            configFile = if (homeConfigFile.exists()) {
                homeConfigFile.absolutePath
            } else {
                null
            }
        }

        config = ConfigLoader(configFile).load()
    }

    private fun printHelp(parser: OptionParser) {
        println("Usage: [options]")
        parser.printHelpOn(System.out)
    }
}

