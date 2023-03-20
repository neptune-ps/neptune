package me.filby.neptune.clientscript.compiler

import me.filby.neptune.clientscript.compiler.command.DbFindCommandHandler
import me.filby.neptune.clientscript.compiler.command.DbGetFieldCommandHandler
import me.filby.neptune.clientscript.compiler.command.EnumCommandHandler
import me.filby.neptune.clientscript.compiler.command.ParamCommandHandler
import me.filby.neptune.clientscript.compiler.trigger.ClientTriggerType
import me.filby.neptune.clientscript.compiler.type.ParamType
import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.compiler.ScriptCompiler
import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.wrapped.VarBitType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClanSettingsType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClanType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClientType
import me.filby.neptune.runescript.compiler.type.wrapped.VarPlayerType
import me.filby.neptune.runescript.compiler.writer.ScriptWriter
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

class ClientScriptCompiler(sourcePath: Path, scriptWriter: ScriptWriter) : ScriptCompiler(sourcePath, scriptWriter) {
    fun setup() {
        triggers.registerAll<ClientTriggerType>()

        // register types
        types.registerAll<ScriptVarType>()

        // TODO convert to ScriptVarType
        types.register("entityoverlay", '-')
        val model = types.register("model", 'm')
        val seq = types.register("seq", 'A')
        val fontmetrics = types.register("fontmetrics", 'f')
        val npc = types.register("npc", 'n')
        val wma = types.register("wma", '`')
        val category = types.register("category", 'y')
        types.register("npc_uid", 'u')
        val loc = types.register("loc", 'l')
        types.register("player_uid", 'p')
        val synth = types.register("synth", 'P')
        val dbrow = types.register("dbrow", 'Ð')
        val dbtable = types.register("dbtable")
        val struct = types.register("struct", 'J')
        val locshape = types.register("locshape", 'H')
        val `interface` = types.register("interface", 'a')
        val toplevelinterface = types.register("toplevelinterface", 'F')
        val overlayinterface = types.register("overlayinterface", 'L')

        // special types for commands
        types.register("clientscript", MetaType.ClientScript(MetaType.Unit))
        types.register("clientscript_stat", MetaType.ClientScript(ScriptVarType.STAT))
        types.register("clientscript_inv", MetaType.ClientScript(ScriptVarType.INV))
        types.register("clientscript_varp", MetaType.ClientScript(VarPlayerType(MetaType.Any)))
        types.register("dbcolumn", DbFindCommandHandler.DbColumnType(MetaType.Any))

        // allow assignment of namedobj to obj
        types.addTypeChecker { left, right -> left == ScriptVarType.OBJ && right == ScriptVarType.NAMEDOBJ }

        // allow assignment of graphic to fontmetrics
        types.addTypeChecker { left, right -> left == fontmetrics && right == ScriptVarType.GRAPHIC }

        // register the dynamic command handlers
        addDynamicCommandHandler("enum", EnumCommandHandler())
        addDynamicCommandHandler("oc_param", ParamCommandHandler(ScriptVarType.OBJ))
        addDynamicCommandHandler("nc_param", ParamCommandHandler(npc))
        addDynamicCommandHandler("lc_param", ParamCommandHandler(loc))
        addDynamicCommandHandler("struct_param", ParamCommandHandler(struct))
        addDynamicCommandHandler("db_find", DbFindCommandHandler(false))
        addDynamicCommandHandler("db_find_with_count", DbFindCommandHandler(true))
        addDynamicCommandHandler("db_find_refine", DbFindCommandHandler(false))
        addDynamicCommandHandler("db_find_refine_with_count", DbFindCommandHandler(true))
        addDynamicCommandHandler("db_getfield", DbGetFieldCommandHandler())

        // symbol loaders
        addSymbolLoader(ConstantLoader(Path("symbols/constants.tsv")))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/graphics.tsv"), ScriptVarType.GRAPHIC))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/fontmetrics.tsv"), fontmetrics))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/stats.tsv"), ScriptVarType.STAT))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/synths.tsv"), synth))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/locshapes.tsv"), locshape))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/models.tsv"), model))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/interfaces.tsv"), `interface`))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/toplevelinterfaces.tsv"), toplevelinterface))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/overlayinterfaces.tsv"), overlayinterface))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/components.tsv"), ScriptVarType.COMPONENT))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/categories.tsv"), category))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/wmas.tsv"), wma))

        addSymbolLoader(TsvSymbolLoader(Path("symbols/locs.tsv"), loc, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/npcs.tsv"), npc, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/objs.tsv"), ScriptVarType.NAMEDOBJ, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/invs.tsv"), ScriptVarType.INV, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/enums.tsv"), ScriptVarType.ENUM, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/structs.tsv"), struct, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/seqs.tsv"), seq, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/dbtables.tsv"), dbtable, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/dbrows.tsv"), dbrow, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/dbcolumns.tsv")) { DbFindCommandHandler.DbColumnType(it) })
        addSymbolLoader(TsvSymbolLoader(Path("symbols/params.tsv"), config = true) { ParamType(it) })
        addSymbolLoader(TsvSymbolLoader(Path("symbols/vars.tsv"), config = true) { VarPlayerType(it) })
        addSymbolLoader(TsvSymbolLoader(Path("symbols/varcints.tsv"), config = true) { VarClientType(it) })
        addSymbolLoader(TsvSymbolLoader(Path("symbols/varcstrings.tsv"), config = true) { VarClientType(it) })
        addSymbolLoader(TsvSymbolLoader(Path("symbols/varbits.tsv"), VarBitType, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/varclans.tsv"), config = true) { VarClanType(it) })
        addSymbolLoader(
            TsvSymbolLoader(Path("symbols/varclansettings.tsv"), config = true) { VarClanSettingsType(it) }
        )
    }

    companion object {
        @JvmStatic
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
    }
}
