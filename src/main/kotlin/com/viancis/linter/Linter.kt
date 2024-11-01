package com.viancis.linter

import com.viancis.checkers.builder.BuilderAnalyze
import com.viancis.checkers.builder.BuilderChecker
import com.viancis.utils.Category
import com.viancis.checkers.interfaces.CodeCheck
import com.viancis.checkers.language.java.entity.JavaCodeEntity
import com.viancis.communication.CheckerContext
import com.viancis.communication.Request
import com.viancis.utils.FileWalker
import com.viancis.utils.Printer
import org.treesitter.TSTree
import java.nio.file.Path



class Linter {

    private val fileWalker: FileWalker = FileWalker()
    private val sourceParser: SyntaxAnalyzer = SyntaxAnalyzer()
    private val builderChecker: BuilderChecker = BuilderChecker()
    private val analizeChecker: BuilderAnalyze = BuilderAnalyze()

    private lateinit var codeChecks: Map<String, Map<String, CodeCheck>>
    private lateinit var universalCheckers: Map<String, CodeCheck>
    private lateinit var analyze: Map<String, Map<String, CodeCheck>>


    fun lint(request: Request) {
        if (request.config.programmingLanguages.isEmpty()) {
            Printer.printEnhanced(Category.ERROR, "Не указали языки для линетра")
            return
        }
        codeChecks = buildCodeChecks(request)
        universalCheckers = builderChecker.buildCheckers("universal", request)
        val (parsedFiles, entityMap) = sourceParser.parseSourceFromRequest(request)

        if (parsedFiles.isEmpty()) {
            Printer.printEnhanced(Category.WARNING, "Не найдено файлов для анализа.")
            return
        }

        val fileErrors: MutableMap<Path, List<String>> = mutableMapOf()

        parsedFiles.forEach { (fullName, pair) ->
            val (file, tree) = pair
            val fileExtension = getFileExtension(file)
            val language = request.config.programmingLanguages.find { it == fileExtension }
            val languageCheckers = language?.let { codeChecks[it] } ?: emptyMap()


            val errors = mutableListOf<String>()
            applyCheckers(universalCheckers, tree, file, errors, entityMap, fullName)
            applyCheckers(languageCheckers, tree, file, errors, entityMap, fullName)
            if (errors.isNotEmpty()) {
                println("")
                Printer.printEnhanced(Category.INFO, "Анализ файла: ${file.fileName} ")
                fileErrors[file] = errors
            }
        }

        if (fileErrors.isEmpty()) {
            Printer.printEnhanced(Category.INFO, "Кажется, твой код очень даже не плох ")
        } else {
            Printer.printNerdFaceWithMessage("ээээмм, к сожалению, необходимо констатировать, что представленный код не удовлетворяет множеству критически важных требований, выдвигаемых к современным стандартам программирования. Это обстоятельство, безусловно, вызывает серьезные сомнения относительно его соответствия установленным нормативам и принципам, на которых основывается эффективная разработка программного обеспечения.  " +
                    " " +
                    "Рекомендую обратить внимание на важность согласованности в именах переменных и методов, а также на необходимость следования SOLID-принципам, которые, безусловно, являются краеугольным камнем устойчивого и поддерживаемого кода. В этом контексте, изучение литературы по программной инженерии, такой как “Совершенный код” и “Code Complete 2nd Edition”, будет весьма полезным, поскольку эти источники предоставляют исчерпывающие знания о наилучших практиках написания программного обеспечения и соблюдения принятых соглашений.")
        }
    }

    private fun buildCodeChecks(request: Request): Map<String, Map<String, CodeCheck>> {
        return request.config.programmingLanguages.associateWith { extension ->
            builderChecker.buildCheckers(extension , request )
        }
    }

    private fun getFileExtension(file: Path): String {
        return file.fileName.toString().substringAfterLast('.', "")
    }

    private fun applyCheckers(
        checkers: Map<String, CodeCheck>,
        tree: TSTree,
        file: Path,
        errors: MutableList<String>,
        parsedFilesByPackage: Map<String, JavaCodeEntity.JavaFileEntity>,
        packageName:String
    ) {
        val classEntity = parsedFilesByPackage[packageName]
        if (classEntity == null) {
            Printer.printEnhanced(Category.WARNING, "Класс не найден для файла: ${file.toString()}")
            return
        }
        checkers.forEach { (_, checker) ->
            val checkResponse = checker.check(CheckerContext(tree,file,parsedFilesByPackage,packageName))
            if (checkResponse.errors.isNotEmpty()) {
                checkResponse.errors.forEach { error ->
                    Printer.printEnhancedWithPhrase(checkResponse.level, checkResponse.code, error)
                    errors.add(error)
                }
            }
        }




    }
}

