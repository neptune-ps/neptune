package me.filby.neptune.clientscript.compiler

import me.filby.neptune.clientscript.compiler.writer.BinaryFileScriptWriter
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines

fun main(args: Array<String>) {
    if (args.size != 2) {
        error("usage: compiler.jar [src path] [output path]")
    }

    val srcPath = Path(args[0])
    if (!srcPath.exists()) {
        error("$srcPath does not exist.")
    }

    val outputPath = Path(args[1])

    val mapper = SymbolMapper()

    // temporary until attributes are added, just makes it easier to add commands without new compiler build
    val commandMappings = Path("symbols/commands.sym")
    if (commandMappings.exists()) {
        for (line in commandMappings.readLines()) {
            val split = line.split("\t")
            val id = split[0].toInt()
            val name = split[1]
            mapper.putCommand(id, name)
        }
    }

    // TODO move somewhere else?
    val scriptMappings = Path("symbols/clientscript.sym")
    if (scriptMappings.exists()) {
        for (line in scriptMappings.readLines()) {
            val split = line.split("\t")
            val id = split[0].toInt()
            val name = split[1]
            mapper.putScript(id, name)
        }
    }

    val writer = BinaryFileScriptWriter(outputPath, mapper)
    val compiler = ClientScriptCompiler(srcPath, writer, mapper)
    compiler.setup()
    compiler.run()
}
