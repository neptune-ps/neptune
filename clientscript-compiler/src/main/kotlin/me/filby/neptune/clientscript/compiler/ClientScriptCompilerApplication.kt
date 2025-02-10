package me.filby.neptune.clientscript.compiler

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import ch.qos.logback.classic.LoggerContext
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import com.github.michaelbull.logging.InlineLogger
import com.github.michaelbull.logging.Logger
import com.google.gson.GsonBuilder
import me.filby.neptune.clientscript.compiler.configuration.BinaryFileWriterConfig
import me.filby.neptune.clientscript.compiler.configuration.ClientScriptCompilerConfig
import me.filby.neptune.clientscript.compiler.writer.BinaryFileScriptWriter
import me.filby.neptune.runescript.compiler.pointer.PointerHolder
import me.filby.neptune.runescript.compiler.pointer.PointerType
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.EnumSet
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.io.path.readLines
import kotlin.system.exitProcess

private const val VERSION = "1.0.1-SNAPSHOT"
private val logger = InlineLogger()

class ClientScriptCommand : CliktCommand(name = "cs2") {
    private val configPath by option("--config-path", help = "Path to the config file.")
        .path(mustExist = true, canBeDir = false, mustBeReadable = true)
        .default(Path("neptune.toml"))

    private val print by option("--print", help = "Print the configuration and exit.")
        .flag()

    private val logLevelName by option("--log-level", help = "Set the log level.")
        .choice("off", "error", "warn", "info", "debug", "trace", "all", ignoreCase = true)
        .default("info")

    private val version by option("--version", help = "Print the version of the compiler.")
        .flag()

    init {
        context {
            helpFormatter = { MordantHelpFormatter(it, showDefaultValues = true) }
        }
    }

    override fun run() {
        configureLogLevel(logLevelName)

        if (version) {
            echo("Neptune ClientScript 2 Compiler")
            echo("Version $VERSION")
            exitProcess(0)
        }

        val config = loadConfig(configPath)
        if (print) {
            val json = GsonBuilder()
                .create()
                .toJson(config)
            println(json)
            exitProcess(0)
        }

        val basePath = configPath.absolute().parent
        val sourcePaths = config.sourcePaths.map { basePath.resolve(it) }
        val symbolPaths = config.symbolPaths.map { basePath.resolve(it) }
        val libraryPaths = config.libraryPaths.map { basePath.resolve(it) }
        val (binaryWriterConfig) = config.writers

        val mapper = SymbolMapper()
        val writer = if (binaryWriterConfig != null) {
            val outputPath = basePath.resolve(binaryWriterConfig.outputPath)
            BinaryFileScriptWriter(outputPath, mapper)
        } else {
            null
        }

        // load commands and clientscript id mappings
        val commandPointers = hashMapOf<String, PointerHolder>()
        loadSpecialSymbols(symbolPaths, mapper, commandPointers)

        // setup compiler and execute it
        val compiler = ClientScriptCompiler(sourcePaths, libraryPaths, writer, commandPointers, symbolPaths, mapper)
        compiler.setup()
        compiler.run()
    }
}

fun main(args: Array<String>) = ClientScriptCommand().main(args)

private fun configureLogLevel(levelName: String) {
    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    val root = context.getLogger(Logger.ROOT_LOGGER_NAME)
    val level = when (levelName) {
        "off" -> ch.qos.logback.classic.Level.OFF
        "error" -> ch.qos.logback.classic.Level.ERROR
        "warn" -> ch.qos.logback.classic.Level.WARN
        "info" -> ch.qos.logback.classic.Level.INFO
        "debug" -> ch.qos.logback.classic.Level.DEBUG
        "trace" -> ch.qos.logback.classic.Level.TRACE
        "all" -> ch.qos.logback.classic.Level.ALL
        else -> error("Unknown log level: $levelName")
    }
    root.level = level
}

private fun loadConfig(configPath: Path): ClientScriptCompilerConfig {
    if (configPath.notExists()) {
        logger.error { "Unable to locate configuration file: $configPath." }
        exitProcess(1)
    }

    val tomlMapper = tomlMapper {
        mapping<ClientScriptCompilerConfig>(
            "sources" to "sourcePaths",
            "symbols" to "symbolPaths",
            "libraries" to "libraryPaths",
            "excludes" to "excludePaths",
            "writer" to "writers",
        )
        mapping<BinaryFileWriterConfig>("output" to "outputPath")
    }
    logger.info { "Loading configuration from $configPath." }
    return tomlMapper.decode<ClientScriptCompilerConfig>(configPath)
}

private fun loadSpecialSymbols(
    symbolsPaths: List<Path>,
    mapper: SymbolMapper,
    commandPointers: MutableMap<String, PointerHolder>,
) {
    for (symbolPath in symbolsPaths) {
        val commandMappings = symbolPath.resolve("commands.sym")
        if (commandMappings.exists()) {
            for (line in commandMappings.readLines()) {
                if (line.isBlank()) {
                    continue
                }

                val split = line.split("\t")
                val id = split[0].toInt()
                val name = split[1]

                if (split.size > 2) {
                    val requiredText = split.getOrNull(2)
                    val setTextTemp = split.getOrNull(3)
                    val setText = setTextTemp?.substringAfter("CONDITIONAL:")
                    val corruptedText = split.getOrNull(4)

                    val required = parsePointerList(requiredText?.substringBefore(':'))
                    val required2 = parsePointerList(requiredText?.substringAfter(':'))

                    val set = parsePointerList(setText?.substringBefore(':'))
                    val set2 = parsePointerList(setText?.substringAfter(':'))
                    val conditionalSet = setTextTemp != setText

                    val corrupted = parsePointerList(corruptedText?.substringBefore(':'))
                    val corrupted2 = parsePointerList(corruptedText?.substringAfter(':'))

                    commandPointers[name] = PointerHolder(required, set, conditionalSet, corrupted)
                    if (required2.isNotEmpty() || set2.isNotEmpty() || corrupted2.isNotEmpty()) {
                        val dotName = ".$name"
                        commandPointers[dotName] = PointerHolder(required2, set2, conditionalSet, corrupted2)
                    }
                }
                mapper.putCommand(id, name)
            }
        }

        // TODO move somewhere else?
        val scriptMappings = symbolPath.resolve("clientscript.sym")
        if (scriptMappings.exists()) {
            for (line in scriptMappings.readLines()) {
                if (line.isBlank()) {
                    continue
                }

                val split = line.split("\t")
                val id = split[0].toInt()
                val name = split[1]
                mapper.putScript(id, name)
            }
        }
    }
}

private fun parsePointerList(text: String?): Set<PointerType> {
    if (text.isNullOrEmpty() || text == "none") {
        return emptySet()
    }

    val pointers = EnumSet.noneOf(PointerType::class.java)
    val pointerNames = text.split(',')
    for (pointerName in pointerNames) {
        val pointer = PointerType.forName(pointerName)
        if (pointer != null) {
            pointers += pointer
        } else {
            error("Invalid pointer name: $pointerName")
        }
    }
    return pointers
}
