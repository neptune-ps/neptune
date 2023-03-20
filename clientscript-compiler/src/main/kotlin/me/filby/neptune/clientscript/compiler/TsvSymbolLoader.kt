package me.filby.neptune.clientscript.compiler

import me.filby.neptune.runescript.compiler.ScriptCompiler
import me.filby.neptune.runescript.compiler.configuration.SymbolLoader
import me.filby.neptune.runescript.compiler.symbol.SymbolTable
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type
import java.nio.file.Path
import kotlin.io.path.useLines

class TsvSymbolLoader(
    private val path: Path,
    private val config: Boolean = false,
    private val typeSupplier: (subTypes: Type) -> Type,
) : SymbolLoader {
    constructor(path: Path, type: Type, config: Boolean = false) : this(path, config, { type })

    override fun SymbolTable.load(compiler: ScriptCompiler) {
        path.useLines { lines ->
            for (line in lines) {
                val split = line.split('\t')
                if (split.size < 2) {
                    continue
                }

                val id = split[0].toInt()
                val name = split[1]
                val subTypes = if (split.size >= 3) {
                    val typeSplit = split[2].split(',')
                    val types = typeSplit.map { typeName -> compiler.types.find(typeName) }
                    TupleType.fromList(types)
                } else {
                    MetaType.Unit
                }
                val type = typeSupplier(subTypes)

                if (config) {
                    addConfig(type, name)
                } else {
                    addBasic(type, name)
                }
            }
        }
    }
}
