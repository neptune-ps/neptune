package me.filby.neptune.clientscript.compiler

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import com.github.michaelbull.logging.InlineLogger
import me.filby.neptune.clientscript.compiler.configuration.BinaryFileWriterConfig
import me.filby.neptune.clientscript.compiler.configuration.ClientScriptCompilerConfig
import me.filby.neptune.clientscript.compiler.writer.BinaryFileScriptWriter
import me.filby.neptune.runescript.compiler.pointer.PointerHolder
import me.filby.neptune.runescript.compiler.pointer.PointerType
import java.nio.file.Path
import java.util.EnumSet
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
    val commandPointers = hashMapOf<String, PointerHolder>()
    loadSpecialSymbols(symbolPaths, mapper, commandPointers)

    // setup compiler and execute it
    val compiler = ClientScriptCompiler(sourcePaths, excludePaths, writer, commandPointers, symbolPaths, mapper)
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

private fun loadSpecialSymbols(
    symbolsPaths: List<Path>,
    mapper: SymbolMapper,
    commandPointers: MutableMap<String, PointerHolder>,
) {
    for (symbolPath in symbolsPaths) {
        val commandMappings = symbolPath.resolve("commands.sym")
        if (commandMappings.exists()) {
            for (line in commandMappings.readLines()) {
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
