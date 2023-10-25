package me.filby.neptune.clientscript.compiler

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import com.github.michaelbull.logging.InlineLogger
import me.filby.neptune.clientscript.compiler.configuration.BinaryFileWriterConfig
import me.filby.neptune.clientscript.compiler.configuration.ClientScriptCompilerConfig
import me.filby.neptune.clientscript.compiler.writer.BinaryFileScriptWriter
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.io.path.readLines
import kotlin.system.exitProcess

private val logger = InlineLogger()

fun main(args: Array<String>) {
    val configPath = if (args.isNotEmpty()) Path(args[0]) else Path("neptune.toml")
    val config = loadConfig(configPath.absolute())

    val sourcePaths = config.sourcePaths.map { Path(it) }
    val symbolPaths = config.symbolPaths.map { Path(it) }
    val excludePaths = config.excludePaths.map { Path(it) }
    val (binaryWriterConfig) = config.writers

    val mapper = SymbolMapper()
    val writer = if (binaryWriterConfig != null) {
        val outputPath = Path(binaryWriterConfig.outputPath)
        BinaryFileScriptWriter(outputPath, mapper)
    } else {
        logger.error { "No writer configured." }
        exitProcess(1)
    }

    // load commands and clientscript id mappings
    loadSpecialSymbols(symbolPaths, mapper)

    // setup compiler and execute it
    val compiler = ClientScriptCompiler(sourcePaths, excludePaths, writer, symbolPaths, mapper)
    compiler.setup()
    compiler.run()
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
            "excluded" to "excludePaths",
            "writer" to "writers",
        )
        mapping<BinaryFileWriterConfig>("output" to "outputPath")
    }
    logger.info { "Loading configuration from $configPath." }
    return tomlMapper.decode<ClientScriptCompilerConfig>(configPath)
}

private fun loadSpecialSymbols(symbolsPaths: List<Path>, mapper: SymbolMapper) {
    for (symbolPath in symbolsPaths) {
        val commandMappings = symbolPath.resolve("commands.sym")
        if (commandMappings.exists()) {
            for (line in commandMappings.readLines()) {
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
                val split = line.split("\t")
                val id = split[0].toInt()
                val name = split[1]
                mapper.putScript(id, name)
            }
        }
    }
}
