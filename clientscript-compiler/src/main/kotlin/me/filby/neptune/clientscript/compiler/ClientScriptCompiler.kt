package me.filby.neptune.clientscript.compiler

import me.filby.neptune.clientscript.compiler.command.CcCreateCommandHandler
import me.filby.neptune.clientscript.compiler.command.DbFindCommandHandler
import me.filby.neptune.clientscript.compiler.command.DbGetFieldCommandHandler
import me.filby.neptune.clientscript.compiler.command.EnumCommandHandler
import me.filby.neptune.clientscript.compiler.command.EnumGetInputsOutputsCommandHandler
import me.filby.neptune.clientscript.compiler.command.IfParamCommandHandler
import me.filby.neptune.clientscript.compiler.command.IfRunScriptCommandHandler
import me.filby.neptune.clientscript.compiler.command.IfSetParamCommandHandler
import me.filby.neptune.clientscript.compiler.command.ParamCommandHandler
import me.filby.neptune.clientscript.compiler.command.PlaceholderCommand
import me.filby.neptune.clientscript.compiler.command.array.ArrayCompareCommandHandler
import me.filby.neptune.clientscript.compiler.command.array.ArrayCopyCommandHandler
import me.filby.neptune.clientscript.compiler.command.array.ArrayCreateCommandHandler
import me.filby.neptune.clientscript.compiler.command.array.ArrayDeleteCommandHandler
import me.filby.neptune.clientscript.compiler.command.array.ArrayFillCommandHandler
import me.filby.neptune.clientscript.compiler.command.array.ArrayInsertCommandHandler
import me.filby.neptune.clientscript.compiler.command.array.ArrayInsertallCommandHandler
import me.filby.neptune.clientscript.compiler.command.array.ArrayMinMaxCommandHandler
import me.filby.neptune.clientscript.compiler.command.array.ArrayPushCommandHandler
import me.filby.neptune.clientscript.compiler.command.array.ArrayPushallCommandHandler
import me.filby.neptune.clientscript.compiler.command.array.ArraySearchCommandHandler
import me.filby.neptune.clientscript.compiler.command.debug.DumpCommandHandler
import me.filby.neptune.clientscript.compiler.command.debug.ScriptCommandHandler
import me.filby.neptune.clientscript.compiler.configuration.ClientScriptCompilerFeatureSet
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
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class ClientScriptCompiler(
    sourcePaths: List<Path>,
    libraryPaths: List<Path>,
    scriptWriter: ScriptWriter?,
    features: ClientScriptCompilerFeatureSet,
    private val symbolPaths: List<Path>,
    private val mapper: SymbolMapper,
) : ScriptCompiler(sourcePaths, libraryPaths, scriptWriter, features) {
    fun setup() {
        val features = features as ClientScriptCompilerFeatureSet

        triggers.registerAll<ClientTriggerType>()

        // register types
        types.registerAll<ScriptVarType>()
        types.register("param", ParamCommandHandler.PARAM_ANY)
        types.changeOptions("long") {
            allowDeclaration = false
        }

        // special types for commands
        types.register("hook", MetaType.Hook(MetaType.Unit))
        types.register("stathook", MetaType.Hook(ScriptVarType.STAT))
        types.register("invhook", MetaType.Hook(ScriptVarType.INV))
        types.register("varphook", MetaType.Hook(VarPlayerType(MetaType.Any)))
        types.register("dbcolumn", DbColumnType(MetaType.Any))
        types.register("clientopnpc", MetaType.Script(ClientTriggerType.CLIENTOPNPC, MetaType.Unit, MetaType.Unit))
        types.register("clientoploc", MetaType.Script(ClientTriggerType.CLIENTOPLOC, MetaType.Unit, MetaType.Unit))
        types.register("clientopobj", MetaType.Script(ClientTriggerType.CLIENTOPOBJ, MetaType.Unit, MetaType.Unit))
        types.register(
            "clientopplayer",
            MetaType.Script(ClientTriggerType.CLIENTOPPLAYER, MetaType.Unit, MetaType.Unit),
        )
        types.register("clientoptile", MetaType.Script(ClientTriggerType.CLIENTOPTILE, MetaType.Unit, MetaType.Unit))

        // allow assignment of namedobj to obj
        types.addTypeChecker { left, right -> left == ScriptVarType.OBJ && right == ScriptVarType.NAMEDOBJ }

        // treat varp as alias of varp<int>
        types.addTypeChecker { left, right ->
            (left is VarPlayerType && left.inner == PrimitiveType.INT && right == ScriptVarType.VARP) ||
                (left == ScriptVarType.VARP && right is VarPlayerType && right.inner == PrimitiveType.INT)
        }

        // register the dynamic command handlers
        if (features.ccCreateAssertNewArg) {
            addDynamicCommandHandler("cc_create", CcCreateCommandHandler(), dot = true)
        }
        addDynamicCommandHandler("enum", EnumCommandHandler())
        addDynamicCommandHandler("oc_param", ParamCommandHandler(ScriptVarType.OBJ))
        addDynamicCommandHandler("nc_param", ParamCommandHandler(ScriptVarType.NPC))
        addDynamicCommandHandler("lc_param", ParamCommandHandler(ScriptVarType.LOC))
        addDynamicCommandHandler("struct_param", ParamCommandHandler(ScriptVarType.STRUCT))
        addDynamicCommandHandler("if_param", IfParamCommandHandler(cc = false))
        addDynamicCommandHandler("cc_param", IfParamCommandHandler(cc = true), dot = true)
        addDynamicCommandHandler("if_setparam", IfSetParamCommandHandler(cc = false))
        addDynamicCommandHandler("cc_setparam", IfSetParamCommandHandler(cc = true), dot = true)

        addDynamicCommandHandler("if_runscript", IfRunScriptCommandHandler())

        if (features.dbFindReturnsCount) {
            addDynamicCommandHandler("db_find", DbFindCommandHandler(true))
            addDynamicCommandHandler("db_find_refine", DbFindCommandHandler(true))
        } else {
            addDynamicCommandHandler("db_find", DbFindCommandHandler(false))
            addDynamicCommandHandler("db_find_with_count", DbFindCommandHandler(true))
            addDynamicCommandHandler("db_find_refine", DbFindCommandHandler(false))
            addDynamicCommandHandler("db_find_refine_with_count", DbFindCommandHandler(true))
        }
        addDynamicCommandHandler("db_getfield", DbGetFieldCommandHandler())

        if (features.arraysV2) {
            addDynamicCommandHandler("array_compare", ArrayCompareCommandHandler())
            addDynamicCommandHandler("array_indexof", ArraySearchCommandHandler())
            addDynamicCommandHandler("array_lastindexof", ArraySearchCommandHandler())
            addDynamicCommandHandler("array_count", ArraySearchCommandHandler())
            addDynamicCommandHandler("array_min", ArrayMinMaxCommandHandler())
            addDynamicCommandHandler("array_max", ArrayMinMaxCommandHandler())
            addDynamicCommandHandler("array_fill", ArrayFillCommandHandler())
            addDynamicCommandHandler("array_copy", ArrayCopyCommandHandler())
            addDynamicCommandHandler("array_create", ArrayCreateCommandHandler())
            addDynamicCommandHandler("array_push", ArrayPushCommandHandler())
            addDynamicCommandHandler("array_insert", ArrayInsertCommandHandler())
            addDynamicCommandHandler("array_delete", ArrayDeleteCommandHandler())
            addDynamicCommandHandler("array_pushall", ArrayPushallCommandHandler())
            addDynamicCommandHandler("array_insertall", ArrayInsertallCommandHandler())
            addDynamicCommandHandler("enum_getinputs", EnumGetInputsOutputsCommandHandler())
            addDynamicCommandHandler("enum_getoutputs", EnumGetInputsOutputsCommandHandler())
        }

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

        addDynamicCommandHandler("dump", DumpCommandHandler())
        addDynamicCommandHandler("script", ScriptCommandHandler())

        // symbol loaders
        addSymConstantLoaders()

        addSymLoader("ai_queue", ScriptVarType.AI_QUEUE)
        addSymLoader("area", ScriptVarType.AREA)
        addSymLoader("bas", ScriptVarType.BAS)
        addSymLoader("bugtemplate", ScriptVarType.BUG_TEMPLATE)
        addSymLoader("category", ScriptVarType.CATEGORY)
        addSymLoader("chatcat", ScriptVarType.CHATCAT)
        addSymLoader("chatphrase", ScriptVarType.CHATPHRASE)
        addSymLoader("clientinterface", ScriptVarType.CLIENTINTERFACE)
        addSymLoader("component", ScriptVarType.COMPONENT)
        addSymLoader("controller", ScriptVarType.CONTROLLER)
        addSymLoader("cursor", ScriptVarType.CURSOR)
        addSymLoader("cutscene", ScriptVarType.CUTSCENE)
        addSymLoader("dbcolumn") { DbColumnType(it) }
        addSymLoader("dbrow", ScriptVarType.DBROW)
        addSymLoader("dbtable", ScriptVarType.DBTABLE)
        addSymLoader("enum", ScriptVarType.ENUM)
        addSymLoader("fontmetrics", ScriptVarType.FONTMETRICS)
        addSymLoader("graphic", ScriptVarType.GRAPHIC)
        addSymLoader("headbar", ScriptVarType.HEADBAR)
        addSymLoader("hitmark", ScriptVarType.HITMARK)
        addSymLoader("hunt", ScriptVarType.HUNT)
        addSymLoader("idkit", ScriptVarType.IDKIT)
        addSymLoader("interface", ScriptVarType.INTERFACE)
        addSymLoader("inv", ScriptVarType.INV)
        addSymLoader("jingle", ScriptVarType.JINGLE)
        addSymLoader("loc", ScriptVarType.LOC)
        addSymLoader("locshape", ScriptVarType.LOC_SHAPE)
        addSymLoader("mapelement", ScriptVarType.MAPELEMENT)
        addSymLoader("mapsceneicon", ScriptVarType.MAPSCENEICON)
        addSymLoader("material", ScriptVarType.MATERIAL)
        addSymLoader("midi", ScriptVarType.MIDI)
        addSymLoader("model", ScriptVarType.MODEL)
        addSymLoader("movespeed", ScriptVarType.MOVESPEED)
        addSymLoader("npc", ScriptVarType.NPC)
        addSymLoader("npc_mode", ScriptVarType.NPC_MODE)
        addSymLoader("npc_stat", ScriptVarType.NPC_STAT)
        addSymLoader("obj", ScriptVarType.NAMEDOBJ)
        addSymLoader("overlayinterface", ScriptVarType.OVERLAYINTERFACE)
        addSymLoader("param") { ParamType(it) }
        addSymLoader("quest", ScriptVarType.QUEST)
        addSymLoader("seq", ScriptVarType.SEQ)
        addSymLoader("gamelogevent", ScriptVarType.GAMELOGEVENT)
        addSymLoader("audiogroup", ScriptVarType.AUDIOGROUP)
        addSymLoader("skybox", ScriptVarType.SKYBOX)
        addSymLoader("skydecor", ScriptVarType.SKYDECOR)
        addSymLoader("social_network", ScriptVarType.SOCIAL_NETWORK)
        addSymLoader("spotanim", ScriptVarType.SPOTANIM)
        addSymLoader("stat", ScriptVarType.STAT)
        addSymLoader("stringvector", ScriptVarType.STRINGVECTOR)
        addSymLoader("struct", ScriptVarType.STRUCT)
        addSymLoader("synth", ScriptVarType.SYNTH)
        addSymLoader("texture", ScriptVarType.TEXTURE)
        addSymLoader("toplevelinterface", ScriptVarType.TOPLEVELINTERFACE)
        addSymLoader("varbit", VarBitType)
        addSymLoader("varc") { VarClientType(it) }
        addSymLoader("varclan") { VarClanType(it) }
        addSymLoader("varclansetting") { VarClanSettingsType(it) }
        addSymLoader("varcstr") { VarClientType(PrimitiveType.STRING) }
        addSymLoader("varp") { VarPlayerType(it) }
        addSymLoader("vorbis", ScriptVarType.VORBIS)
        addSymLoader("wma", ScriptVarType.MAPAREA)
        addSymLoader("writeinv", ScriptVarType.WRITEINV)
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
