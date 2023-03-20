package me.filby.neptune.clientscript.compiler

import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.writer.ScriptWriter
import kotlin.io.path.Path
import kotlin.io.path.exists

fun main(args: Array<String>) {
    if (args.size != 1) {
        error("usage: compiler.jar [src path]")
    }

    val srcPath = Path(args[0])
    if (!srcPath.exists()) {
        error("$srcPath does not exist.")
    }

    val compiler = ClientScriptCompiler(
        srcPath,
        // writer,
        object : ScriptWriter {
            override fun write(script: RuneScript) {
            }
        }
    )
    compiler.setup()
    compiler.run()
}
