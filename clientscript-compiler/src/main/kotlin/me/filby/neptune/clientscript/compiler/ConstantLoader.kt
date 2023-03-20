package me.filby.neptune.clientscript.compiler

import me.filby.neptune.runescript.compiler.ScriptCompiler
import me.filby.neptune.runescript.compiler.configuration.SymbolLoader
import me.filby.neptune.runescript.compiler.symbol.SymbolTable
import java.nio.file.Path
import kotlin.io.path.useLines

class ConstantLoader(private val path: Path) : SymbolLoader {
    override fun SymbolTable.load(compiler: ScriptCompiler) {
        path.useLines {
            for (line in it) {
                val split = line.split('\t', limit = 2)
                if (split.size != 2) {
                    continue
                }

                addConstant(split[0], split[1])
            }
        }
    }
}
