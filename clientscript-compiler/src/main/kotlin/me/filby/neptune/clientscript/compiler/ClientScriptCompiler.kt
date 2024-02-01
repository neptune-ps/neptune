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
import me.filby.neptune.runescript.compiler.pointer.PointerHolder
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
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class ClientScriptCompiler(
    sourcePaths: List<Path>,
    excludePaths: List<Path>,
    scriptWriter: ScriptWriter,
    commandPointers: Map<String, PointerHolder>,
    private val symbolPaths: List<Path>,
    private val mapper: SymbolMapper,
) : ScriptCompiler(sourcePaths, excludePaths, scriptWriter, commandPointers) {
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
        types.register("shiftopnpc", MetaType.Script(ClientTriggerType.SHIFTOPNPC, MetaType.Unit, MetaType.Unit))
        types.register("shiftoploc", MetaType.Script(ClientTriggerType.SHIFTOPLOC, MetaType.Unit, MetaType.Unit))
        types.register("shiftopobj", MetaType.Script(ClientTriggerType.SHIFTOPOBJ, MetaType.Unit, MetaType.Unit))
        types.register("shiftopplayer", MetaType.Script(ClientTriggerType.SHIFTOPPLAYER, MetaType.Unit, MetaType.Unit))
        types.register("shiftoptile", MetaType.Script(ClientTriggerType.SHIFTOPTILE, MetaType.Unit, MetaType.Unit))

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
        addSymConstantLoaders()

        addSymLoader("graphic", ScriptVarType.GRAPHIC)
        addSymLoader("fontmetrics", ScriptVarType.FONTMETRICS)
        addSymLoader("stat", ScriptVarType.STAT)
        addSymLoader("synth", ScriptVarType.SYNTH)
        addSymLoader("locshape", ScriptVarType.LOC_SHAPE)
        addSymLoader("model", ScriptVarType.MODEL)
        addSymLoader("interface", ScriptVarType.INTERFACE)
        addSymLoader("toplevelinterface", ScriptVarType.TOPLEVELINTERFACE)
        addSymLoader("overlayinterface", ScriptVarType.OVERLAYINTERFACE)
        addSymLoader("component", ScriptVarType.COMPONENT)
        addSymLoader("category", ScriptVarType.CATEGORY)
        addSymLoader("wma", ScriptVarType.MAPAREA)
        addSymLoader("mapelement", ScriptVarType.MAPELEMENT)
        addSymLoader("loc", ScriptVarType.LOC)
        addSymLoader("npc", ScriptVarType.NPC)
        addSymLoader("obj", ScriptVarType.NAMEDOBJ)
        addSymLoader("inv", ScriptVarType.INV)
        addSymLoader("enum", ScriptVarType.ENUM)
        addSymLoader("struct", ScriptVarType.STRUCT)
        addSymLoader("seq", ScriptVarType.SEQ)
        addSymLoader("dbtable", ScriptVarType.DBTABLE)
        addSymLoader("dbrow", ScriptVarType.DBROW)
        addSymLoader("dbcolumn") { DbColumnType(it) }
        addSymLoader("param") { ParamType(it) }
        addSymLoader("varp") { VarPlayerType(it) }
        addSymLoader("varc") { VarClientType(it) }
        addSymLoader("varbit", VarBitType)
        addSymLoader("varclan") { VarClanType(it) }
        addSymLoader("varclansetting") { VarClanSettingsType(it) }
        addSymLoader("stringvector", ScriptVarType.STRINGVECTOR)
    }

    /**
     * Looks for `constant.sym` and all `sym` files in `/constant` and registers them
     * with a [ConstantLoader].
     */
    private fun addSymConstantLoaders() {
        for (symbolPath in symbolPaths) {
            // look for {symbol_path}/constant.sym
            val constantsFile = symbolPath.resolve("constant.sym")
            if (constantsFile.exists()) {
                addSymbolLoader(ConstantLoader(constantsFile))
            }

            // look for {symbol_path}/constant/**.sym
            val constantDir = symbolPath.resolve("constant")
            if (constantDir.exists() && constantDir.isDirectory()) {
                val files = constantDir
                    .toFile()
                    .walkTopDown()
                    .filter { it.isFile && it.extension == "sym" }
                for (file in files) {
                    addSymbolLoader(ConstantLoader(file.toPath()))
                }
            }
        }
    }

    /**
     * Helper for loading external symbols from `sym` files with a specific [type].
     */
    private fun addSymLoader(name: String, type: Type) {
        addSymLoader(name) { type }
    }

    /**
     * Helper for loading external symbols from `sym` files with subtypes.
     */
    private fun addSymLoader(name: String, typeSuppler: (subTypes: Type) -> Type) {
        for (symbolPath in symbolPaths) {
            // look for {symbol_path}/{name}.sym
            val typeFile = symbolPath.resolve("$name.sym")
            if (typeFile.exists()) {
                addSymbolLoader(TsvSymbolLoader(mapper, typeFile, typeSuppler))
            }

            // look for {symbol_path}/{name}/**.sym
            val typeDir = symbolPath.resolve(name)
            if (typeDir.exists() && typeDir.isDirectory()) {
                val files = typeDir
                    .toFile()
                    .walkTopDown()
                    .filter { it.isFile && it.extension == "sym" }
                for (file in files) {
                    addSymbolLoader(TsvSymbolLoader(mapper, file.toPath(), typeSuppler))
                }
            }
        }
    }
}
