package me.filby.neptune.clientscript.compiler

import cc.ekblad.toml.model.TomlValue
import cc.ekblad.toml.serialization.from
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
import me.filby.neptune.clientscript.compiler.configuration.ClientScriptCompilerFeatureSet
import me.filby.neptune.clientscript.compiler.writer.BinaryFileScriptWriter
import org.slf4j.LoggerFactory
import java.nio.file.Path
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
        val features = config.features

        val mapper = SymbolMapper()
        val writer = if (binaryWriterConfig != null) {
            val outputPath = basePath.resolve(binaryWriterConfig.outputPath)
            BinaryFileScriptWriter(outputPath, mapper)
        } else {
            null
        }

        // load commands and clientscript id mappings
        loadSpecialSymbols(symbolPaths, mapper)

        // setup compiler and execute it
        val compiler = ClientScriptCompiler(sourcePaths, libraryPaths, writer, features, symbolPaths, mapper)
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

    val document = TomlValue.from(configPath)
    val defaultFeatures = getDefaultFeaturesForVersion(document.properties["client_version"])
    val tomlMapper = tomlMapper {
        // these defaults are required for
        //  1) if features is not defined at all
        //  2) if some features are defined
        default(ClientScriptCompilerConfig(features = defaultFeatures))
        default(defaultFeatures)

        mapping<ClientScriptCompilerConfig>(
            "sources" to "sourcePaths",
            "symbols" to "symbolPaths",
            "libraries" to "libraryPaths",
            "excludes" to "excludePaths",
            "writer" to "writers",
        )
        mapping<ClientScriptCompilerFeatureSet>(
            "db_find_returns_count" to "dbFindReturnsCount",
            "cc_create_optional_assert_new" to "ccCreateAssertNewArg",
            "prefix_postfix_expressions" to "prefixPostfixExpressions",
        )
        mapping<BinaryFileWriterConfig>("output" to "outputPath")
    }
    logger.info { "Loading configuration from $configPath." }
    return tomlMapper.decode<ClientScriptCompilerConfig>(document)
}

private fun getDefaultFeaturesForVersion(versionProperty: TomlValue?): ClientScriptCompilerFeatureSet {
    val version = when (versionProperty) {
        is TomlValue.Integer -> versionProperty.value.toInt()
        null -> Integer.MAX_VALUE
        else -> {
            logger.error { "The 'client_version' value must be numeric." }
            exitProcess(1)
        }
    }

    return ClientScriptCompilerFeatureSet(
        dbFindReturnsCount = version >= 228,
        ccCreateAssertNewArg = version >= 230,
        prefixPostfixExpressions = false,
    )
}

private fun loadSpecialSymbols(symbolsPaths: List<Path>, mapper: SymbolMapper) {
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
