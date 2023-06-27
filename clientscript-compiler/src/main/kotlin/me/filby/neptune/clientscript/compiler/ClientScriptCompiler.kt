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
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

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
        types.register("hook", MetaType.Hook(MetaType.Unit))
        types.register("stathook", MetaType.Hook(ScriptVarType.STAT))
        types.register("invhook", MetaType.Hook(ScriptVarType.INV))
        types.register("varphook", MetaType.Hook(VarPlayerType(MetaType.Any)))
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
        addDynamicCommandHandler("event_op", PlaceholderCommand(PrimitiveType.INT, Int.MIN_VALUE + 4))
        addDynamicCommandHandler("event_comsubid", PlaceholderCommand(PrimitiveType.INT, Int.MIN_VALUE + 5))
        addDynamicCommandHandler("event_com2", PlaceholderCommand(ScriptVarType.COMPONENT, Int.MIN_VALUE + 6))
        addDynamicCommandHandler("event_comsubid2", PlaceholderCommand(PrimitiveType.INT, Int.MIN_VALUE + 7))
        addDynamicCommandHandler("event_keycode", PlaceholderCommand(PrimitiveType.INT, Int.MIN_VALUE + 8))
        addDynamicCommandHandler("event_keychar", PlaceholderCommand(PrimitiveType.CHAR, Int.MIN_VALUE + 9))

        // symbol loaders
        addTsvConstantLoaders()

        addTsvLoader("graphic", ScriptVarType.GRAPHIC)
        addTsvLoader("fontmetrics", ScriptVarType.FONTMETRICS)
        addTsvLoader("stat", ScriptVarType.STAT)
        addTsvLoader("synth", ScriptVarType.SYNTH)
        addTsvLoader("locshape", ScriptVarType.LOC_SHAPE)
        addTsvLoader("model", ScriptVarType.MODEL)
        addTsvLoader("interface", ScriptVarType.INTERFACE)
        addTsvLoader("toplevelinterface", ScriptVarType.TOPLEVELINTERFACE)
        addTsvLoader("overlayinterface", ScriptVarType.OVERLAYINTERFACE)
        addTsvLoader("component", ScriptVarType.COMPONENT)
        addTsvLoader("category", ScriptVarType.CATEGORY)
        addTsvLoader("wma", ScriptVarType.MAPAREA)
        addTsvLoader("mapelement", ScriptVarType.MAPELEMENT)
        addTsvLoader("loc", ScriptVarType.LOC)
        addTsvLoader("npc", ScriptVarType.NPC)
        addTsvLoader("obj", ScriptVarType.NAMEDOBJ)
        addTsvLoader("inv", ScriptVarType.INV)
        addTsvLoader("enum", ScriptVarType.ENUM)
        addTsvLoader("struct", ScriptVarType.STRUCT)
        addTsvLoader("seq", ScriptVarType.SEQ)
        addTsvLoader("dbtable", ScriptVarType.DBTABLE)
        addTsvLoader("dbrow", ScriptVarType.DBROW)
        addTsvLoader("dbcolumn") { DbColumnType(it) }
        addTsvLoader("param") { ParamType(it) }
        addTsvLoader("varp") { VarPlayerType(it) }
        addTsvLoader("varcint") { VarClientType(it) }
        addTsvLoader("varcstring") { VarClientType(it) }
        addTsvLoader("varbit", VarBitType)
        addTsvLoader("varclan") { VarClanType(it) }
        addTsvLoader("varclansetting") { VarClanSettingsType(it) }
        addTsvLoader("stringvector", ScriptVarType.STRINGVECTOR)
    }

    /**
     * Looks for `constant.tsv` and all `tsv` files in `/constant` and registers them
     * with a [ConstantLoader].
     */
    private fun addTsvConstantLoaders() {
        // look for {symbol_path}/constant.tsv
        val constantsFile = SYMBOLS_PATH.resolve("constant.tsv")
        if (constantsFile.exists()) {
            addSymbolLoader(ConstantLoader(constantsFile))
        }

        // look for {symbol_path}/constant/**.tsv
        val constantDir = SYMBOLS_PATH.resolve("constant")
        if (constantDir.exists() && constantDir.isDirectory()) {
            val files = constantDir
                .toFile()
                .walkTopDown()
                .filter { it.isFile && it.extension == "tsv" }
            for (file in files) {
                addSymbolLoader(ConstantLoader(file.toPath()))
            }
        }
    }

    /**
     * Helper for loading external symbols from `tsv` files with a specific [type].
     */
    private fun addTsvLoader(name: String, type: Type) {
        addTsvLoader(name) { type }
    }

    /**
     * Helper for loading external symbols from `tsv` files with subtypes.
     */
    private fun addTsvLoader(name: String, typeSuppler: (subTypes: Type) -> Type) {
        // look for {symbol_path}/{name}.tsv
        val typeFile = SYMBOLS_PATH.resolve("$name.tsv")
        if (typeFile.exists()) {
            addSymbolLoader(TsvSymbolLoader(mapper, typeFile, typeSuppler))
        }

        // look for {symbol_path}/{name}/**.tsv
        val typeDir = SYMBOLS_PATH.resolve(name)
        if (typeDir.exists() && typeDir.isDirectory()) {
            val files = typeDir
                .toFile()
                .walkTopDown()
                .filter { it.isFile && it.extension == "tsv" }
            for (file in files) {
                addSymbolLoader(TsvSymbolLoader(mapper, file.toPath(), typeSuppler))
            }
        }
    }

    private companion object {
        val SYMBOLS_PATH = Path("symbols")
    }
}
