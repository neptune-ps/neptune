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
    private val mapper: SymbolMapper,
    private val path: Path,
    private val typeSupplier: (subTypes: Type) -> Type,
    private val idSupplier: (str: String) -> Int = { str -> str.toInt() },
) : SymbolLoader {
    constructor(mapper: SymbolMapper, path: Path, type: Type) : this(mapper, path, { type })

    override fun SymbolTable.load(compiler: ScriptCompiler) {
        path.useLines { lines ->
            for (line in lines) {
                val split = line.split('\t')
                if (split.size < 2) {
                    continue
                }

                val id = idSupplier(split[0])
                val name = split[1]
                val subTypes = if (split.size >= 3 && split[2].isNotBlank()) {
                    val typeSplit = split[2].split(',')
                    val types = typeSplit.map { typeName -> compiler.types.find(typeName, allowArray = true) }
                    TupleType.fromList(types)
                } else {
                    MetaType.Unit
                }
                val type = typeSupplier(subTypes)

                val symbol = addBasic(type, name)
                mapper.putSymbol(id, symbol)
            }
        }
    }
}
