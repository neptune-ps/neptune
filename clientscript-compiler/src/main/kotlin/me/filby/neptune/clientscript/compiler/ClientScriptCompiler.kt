package me.filby.neptune.clientscript.compiler

import me.filby.neptune.clientscript.compiler.command.DbFindCommandHandler
import me.filby.neptune.clientscript.compiler.command.DbGetFieldCommandHandler
import me.filby.neptune.clientscript.compiler.command.EnumCommandHandler
import me.filby.neptune.clientscript.compiler.command.ParamCommandHandler
import me.filby.neptune.clientscript.compiler.trigger.ClientTriggerType
import me.filby.neptune.clientscript.compiler.type.DbColumnType
import me.filby.neptune.clientscript.compiler.type.ParamType
import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.compiler.ScriptCompiler
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.wrapped.VarBitType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClanSettingsType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClanType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClientType
import me.filby.neptune.runescript.compiler.type.wrapped.VarPlayerType
import me.filby.neptune.runescript.compiler.writer.ScriptWriter
import java.nio.file.Path
import kotlin.io.path.Path

class ClientScriptCompiler(sourcePath: Path, scriptWriter: ScriptWriter) : ScriptCompiler(sourcePath, scriptWriter) {
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

        // symbol loaders
        addSymbolLoader(ConstantLoader(Path("symbols/constants.tsv")))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/graphics.tsv"), ScriptVarType.GRAPHIC))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/fontmetrics.tsv"), ScriptVarType.FONTMETRICS))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/stats.tsv"), ScriptVarType.STAT))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/synths.tsv"), ScriptVarType.SYNTH))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/locshapes.tsv"), ScriptVarType.LOC_SHAPE))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/models.tsv"), ScriptVarType.MODEL))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/interfaces.tsv"), ScriptVarType.INTERFACE))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/toplevelinterfaces.tsv"), ScriptVarType.TOPLEVELINTERFACE))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/overlayinterfaces.tsv"), ScriptVarType.OVERLAYINTERFACE))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/components.tsv"), ScriptVarType.COMPONENT))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/categories.tsv"), ScriptVarType.CATEGORY))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/wmas.tsv"), ScriptVarType.MAPAREA))

        addSymbolLoader(TsvSymbolLoader(Path("symbols/locs.tsv"), ScriptVarType.LOC, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/npcs.tsv"), ScriptVarType.NPC, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/objs.tsv"), ScriptVarType.NAMEDOBJ, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/invs.tsv"), ScriptVarType.INV, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/enums.tsv"), ScriptVarType.ENUM, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/structs.tsv"), ScriptVarType.STRUCT, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/seqs.tsv"), ScriptVarType.SEQ, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/dbtables.tsv"), ScriptVarType.DBTABLE, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/dbrows.tsv"), ScriptVarType.DBROW, config = true))
        addSymbolLoader(TsvSymbolLoader(Path("symbols/dbcolumns.tsv")) { DbColumnType(it) })
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
}
