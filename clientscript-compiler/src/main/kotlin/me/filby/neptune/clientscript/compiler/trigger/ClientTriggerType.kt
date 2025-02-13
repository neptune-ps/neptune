package me.filby.neptune.clientscript.compiler.trigger

import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.compiler.trigger.SubjectMode
import me.filby.neptune.runescript.compiler.trigger.TriggerType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type

/**
 * An enumeration of valid trigger types for use in ClientScript.
 */
enum class ClientTriggerType(
    override val id: Int,
    override val subjectMode: SubjectMode = SubjectMode.Name,
    override val allowParameters: Boolean = false,
    override val parameters: Type? = null,
    override val allowReturns: Boolean = false,
    override val returns: Type? = null,
) : TriggerType {
    OPWORLDMAPELEMENT1(10, subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT)),
    OPWORLDMAPELEMENT2(11, subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT)),
    OPWORLDMAPELEMENT3(12, subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT)),
    OPWORLDMAPELEMENT4(13, subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT)),
    OPWORLDMAPELEMENT5(14, subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT)),
    WORLDMAPELEMENTMOUSEOVER(
        15,
        subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT),
        allowParameters = true,
        parameters = TupleType(PrimitiveType.INT, PrimitiveType.INT),
    ),
    WORLDMAPELEMENTMOUSELEAVE(
        16,
        subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT),
        allowParameters = true,
        parameters = TupleType(PrimitiveType.INT, PrimitiveType.INT),
    ),
    WORLDMAPELEMENTMOUSEREPEAT(
        17,
        subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT),
        allowParameters = true,
        parameters = TupleType(PrimitiveType.INT, PrimitiveType.INT),
    ),
    CLIENTOPNPC(30),
    CLIENTOPLOC(31),
    CLIENTOPOBJ(32),
    CLIENTOPPLAYER(33),
    CLIENTOPTILE(34),
    LOADNPC(35, subjectMode = SubjectMode.Type(ScriptVarType.NPC)),
    UNLOADNPC(36, subjectMode = SubjectMode.Type(ScriptVarType.NPC)),
    LOADLOC(37, subjectMode = SubjectMode.Type(ScriptVarType.LOC)),
    UNLOADLOC(38, subjectMode = SubjectMode.Type(ScriptVarType.LOC)),
    LOADOBJ(39, subjectMode = SubjectMode.Type(ScriptVarType.NAMEDOBJ)),
    UNLOADOBJ(40, subjectMode = SubjectMode.Type(ScriptVarType.NAMEDOBJ)),
    LOADPLAYER(41, subjectMode = SubjectMode.None),
    UNLOADPLAYER(42, subjectMode = SubjectMode.None),
    UPDATEOBJSTACK(45, subjectMode = SubjectMode.None),
    UPDATEOBJCOUNT(46, subjectMode = SubjectMode.Type(ScriptVarType.NAMEDOBJ)),
    PLAYER_DESTINATION(47, subjectMode = SubjectMode.None),
    PLAYER_HOVER(48, subjectMode = SubjectMode.None),
    PLAYER_MOVE(49, subjectMode = SubjectMode.None),
    PROC(73, allowParameters = true, allowReturns = true),
    CLIENTSCRIPT(76, allowParameters = true),
    ONCLICKLOC(78, subjectMode = SubjectMode.None),
    ONCLICKOBJ(79, subjectMode = SubjectMode.None),
    ONCLICKNPC(80, subjectMode = SubjectMode.None),
    ONCLICKPLAYER(81, subjectMode = SubjectMode.None),
    MINIMENU_OPENED(82, subjectMode = SubjectMode.None),
    ;

    override val identifier: String get() = name.lowercase()
}
