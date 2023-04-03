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
    OPWORLDMAPELEMENT1(10, subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT, global = false)),
    OPWORLDMAPELEMENT2(11, subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT, global = false)),
    OPWORLDMAPELEMENT3(12, subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT, global = false)),
    OPWORLDMAPELEMENT4(13, subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT, global = false)),
    OPWORLDMAPELEMENT5(14, subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT, global = false)),
    WORLDMAPELEMENTMOUSEOVER(
        15,
        subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT, global = false),
        allowParameters = true,
        parameters = TupleType(PrimitiveType.INT, PrimitiveType.INT)
    ),
    WORLDMAPELEMENTMOUSELEAVE(
        16,
        subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT, global = false),
        allowParameters = true,
        parameters = TupleType(PrimitiveType.INT, PrimitiveType.INT)
    ),
    WORLDMAPELEMENTMOUSEREPEAT(
        17,
        subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT, global = false),
        allowParameters = true,
        parameters = TupleType(PrimitiveType.INT, PrimitiveType.INT)
    ),
    LOADNPC(35, subjectMode = SubjectMode.Type(ScriptVarType.NPC)),
    LOADLOC(37, subjectMode = SubjectMode.Type(ScriptVarType.LOC)),
    TRIGGER_47(47),
    TRIGGER_48(48),
    TRIGGER_49(49),
    PROC(73, allowParameters = true, allowReturns = true),
    CLIENTSCRIPT(76, allowParameters = true),
    TRIGGER_78(78),
    TRIGGER_79(79),
    TRIGGER_80(80),
    TRIGGER_81(81),
    TRIGGER_82(82),
    ;

    override val identifier: String get() = name.lowercase()
}
