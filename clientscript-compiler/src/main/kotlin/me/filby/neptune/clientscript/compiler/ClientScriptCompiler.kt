package me.filby.neptune.clientscript.compiler

import me.filby.neptune.clientscript.compiler.command.DbFindCommandHandler
import me.filby.neptune.clientscript.compiler.command.DbGetFieldCommandHandler
import me.filby.neptune.clientscript.compiler.command.EnumCommandHandler
import me.filby.neptune.clientscript.compiler.command.ParamCommandHandler
import me.filby.neptune.clientscript.compiler.command.PlaceholderCommand
import me.filby.neptune.clientscript.compiler.trigger.ClientTriggerType
import me.filby.neptune.clientscript.compiler.type.DbColumnType
import me.filby.neptune.clientscript.compiler.type.ParamType
import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.compiler.ScriptCompiler
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.Type
import me.filby.neptune.runescript.compiler.type.wrapped.VarBitType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClanSettingsType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClanType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClientType
import me.filby.neptune.runescript.compiler.type.wrapped.VarPlayerType
import me.filby.neptune.runescript.compiler.writer.ScriptWriter
import java.nio.file.Path
import kotlin.io.path.Path

class ClientScriptCompiler(
    sourcePath: Path,
    scriptWriter: ScriptWriter,
    private val mapper: SymbolMapper,
) : ScriptCompiler(sourcePath, scriptWriter) {
    fun setup() {
        triggers.registerAll<ClientTriggerType>()

        // register types
        types.registerAll<ScriptVarType>()
        types.changeOptions("long") {
            allowDeclaration = false
        }

        // special types for commands
        types.register("clientscript", MetaType.ClientScript(MetaType.Unit))
        types.register("clientscript_stat", MetaType.ClientScript(ScriptVarType.STAT))
        types.register("clientscript_inv", MetaType.ClientScript(ScriptVarType.INV))
        types.register("clientscript_varp", MetaType.ClientScript(VarPlayerType(MetaType.Any)))
        types.register("dbcolumn", DbColumnType(MetaType.Any))

        // allow assignment of namedobj to obj
        types.addTypeChecker { left, right -> left == ScriptVarType.OBJ && right == ScriptVarType.NAMEDOBJ }

        // allow assignment of graphic to fontmetrics
        types.addTypeChecker { left, right -> left == ScriptVarType.FONTMETRICS && right == ScriptVarType.GRAPHIC }

        // register the dynamic command handlers
        addDynamicCommandHandler("enum", EnumCommandHandler())
        addDynamicCommandHandler("oc_param", ParamCommandHandler(ScriptVarType.OBJ))
        addDynamicCommandHandler("nc_param", ParamCommandHandler(ScriptVarType.NPC))
        addDynamicCommandHandler("lc_param", ParamCommandHandler(ScriptVarType.LOC))
        addDynamicCommandHandler("struct_param", ParamCommandHandler(ScriptVarType.STRUCT))
        addDynamicCommandHandler("db_find", DbFindCommandHandler(false))
        addDynamicCommandHandler("db_find_with_count", DbFindCommandHandler(true))
        addDynamicCommandHandler("db_find_refine", DbFindCommandHandler(false))
        addDynamicCommandHandler("db_find_refine_with_count", DbFindCommandHandler(true))
        addDynamicCommandHandler("db_getfield", DbGetFieldCommandHandler())

        addDynamicCommandHandler("event_opbase", PlaceholderCommand(PrimitiveType.STRING, "event_opbase"))
        addDynamicCommandHandler("event_mousex", PlaceholderCommand(PrimitiveType.INT, Int.MIN_VALUE + 1))
        addDynamicCommandHandler("event_mousey", PlaceholderCommand(PrimitiveType.INT, Int.MIN_VALUE + 2))
        addDynamicCommandHandler("event_com", PlaceholderCommand(ScriptVarType.COMPONENT, Int.MIN_VALUE + 3))
        addDynamicCommandHandler("event_opindex", PlaceholderCommand(PrimitiveType.INT, Int.MIN_VALUE + 4))
        addDynamicCommandHandler("event_comsubid", PlaceholderCommand(PrimitiveType.INT, Int.MIN_VALUE + 5))
        addDynamicCommandHandler("event_drop", PlaceholderCommand(ScriptVarType.COMPONENT, Int.MIN_VALUE + 6))
        addDynamicCommandHandler("event_dropsubid", PlaceholderCommand(PrimitiveType.INT, Int.MIN_VALUE + 7))
        addDynamicCommandHandler("event_key", PlaceholderCommand(PrimitiveType.INT, Int.MIN_VALUE + 8))
        addDynamicCommandHandler("event_keychar", PlaceholderCommand(PrimitiveType.CHAR, Int.MIN_VALUE + 9))

        // symbol loaders
        addSymbolLoader(ConstantLoader(Path("symbols/constants.tsv")))

        addTsvLoader("graphics", ScriptVarType.GRAPHIC)
        addTsvLoader("fontmetrics", ScriptVarType.FONTMETRICS)
        addTsvLoader("stats", ScriptVarType.STAT)
        addTsvLoader("synths", ScriptVarType.SYNTH)
        addTsvLoader("locshapes", ScriptVarType.LOC_SHAPE)
        addTsvLoader("models", ScriptVarType.MODEL)
        addTsvLoader("interfaces", ScriptVarType.INTERFACE)
        addTsvLoader("toplevelinterfaces", ScriptVarType.TOPLEVELINTERFACE)
        addTsvLoader("overlayinterfaces", ScriptVarType.OVERLAYINTERFACE)
        addTsvLoader("components", ScriptVarType.COMPONENT)
        addTsvLoader("categories", ScriptVarType.CATEGORY)
        addTsvLoader("wmas", ScriptVarType.MAPAREA)

        addTsvLoader("locs", ScriptVarType.LOC, config = true)
        addTsvLoader("npcs", ScriptVarType.NPC, config = true)
        addTsvLoader("objs", ScriptVarType.NAMEDOBJ, config = true)
        addTsvLoader("invs", ScriptVarType.INV, config = true)
        addTsvLoader("enums", ScriptVarType.ENUM, config = true)
        addTsvLoader("structs", ScriptVarType.STRUCT, config = true)
        addTsvLoader("seqs", ScriptVarType.SEQ, config = true)
        addTsvLoader("dbtables", ScriptVarType.DBTABLE, config = true)
        addTsvLoader("dbrows", ScriptVarType.DBROW, config = true)
        addTsvLoader("dbcolumns") { DbColumnType(it) }
        addTsvLoader("params", config = true) { ParamType(it) }
        addTsvLoader("vars", config = true) { VarPlayerType(it) }
        addTsvLoader("varcints", config = true) { VarClientType(it) }
        addTsvLoader("varcstrings", config = true) { VarClientType(it) }
        addTsvLoader("varbits", VarBitType, config = true)
        addTsvLoader("varclans", config = true) { VarClanType(it) }
        addTsvLoader("varclansettings", config = true) { VarClanSettingsType(it) }
    }

    /**
     * Helper for loading external symbols from `tsv` files with a specific [type].
     */
    private fun addTsvLoader(name: String, type: Type, config: Boolean = false) {
        val path = SYMBOLS_PATH.resolve("$name.tsv")
        addSymbolLoader(TsvSymbolLoader(mapper, path, type, config))
    }

    /**
     * Helper for loading external symbols from `tsv` files with subtypes.
     */
    private fun addTsvLoader(name: String, config: Boolean = false, typeSuppler: (subTypes: Type) -> Type) {
        val path = SYMBOLS_PATH.resolve("$name.tsv")
        addSymbolLoader(TsvSymbolLoader(mapper, path, config, typeSuppler))
    }

    private companion object {
        val SYMBOLS_PATH = Path("symbols")
    }
}
