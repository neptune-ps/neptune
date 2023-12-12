package me.filby.neptune.clientscript.compiler.trigger

import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.compiler.pointer.PointerType
import me.filby.neptune.runescript.compiler.trigger.SubjectMode
import me.filby.neptune.runescript.compiler.trigger.TriggerType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type
import java.util.EnumSet

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
    override val pointers: Set<PointerType>? = null,
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
        parameters = TupleType(PrimitiveType.INT, PrimitiveType.INT)
    ),
    WORLDMAPELEMENTMOUSELEAVE(
        16,
        subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT),
        allowParameters = true,
        parameters = TupleType(PrimitiveType.INT, PrimitiveType.INT)
    ),
    WORLDMAPELEMENTMOUSEREPEAT(
        17,
        subjectMode = SubjectMode.Type(ScriptVarType.MAPELEMENT),
        allowParameters = true,
        parameters = TupleType(PrimitiveType.INT, PrimitiveType.INT)
    ),
    LOADNPC(35, subjectMode = SubjectMode.Type(ScriptVarType.NPC), pointers = EnumSet.of(PointerType.ACTIVE_NPC)),
    LOADLOC(37, subjectMode = SubjectMode.Type(ScriptVarType.LOC), pointers = EnumSet.of(PointerType.ACTIVE_LOC)),
    TRIGGER_45(45, pointers = EnumSet.of(PointerType.ACTIVE_TILE)),
    TRIGGER_47(47, pointers = EnumSet.of(PointerType.ACTIVE_PLAYER, PointerType.ACTIVE_TILE)),
    TRIGGER_48(48, pointers = EnumSet.of(PointerType.ACTIVE_TILE)),
    TRIGGER_49(49, pointers = EnumSet.of(PointerType.ACTIVE_PLAYER)),
    PROC(73, allowParameters = true, allowReturns = true, pointers = EnumSet.allOf(PointerType::class.java)),
    CLIENTSCRIPT(76, allowParameters = true),
    TRIGGER_78(78, pointers = EnumSet.of(PointerType.ACTIVE_LOC)),
    TRIGGER_79(79, pointers = EnumSet.of(PointerType.ACTIVE_OBJ)),
    TRIGGER_80(80, pointers = EnumSet.of(PointerType.ACTIVE_NPC)),
    TRIGGER_81(81, pointers = EnumSet.of(PointerType.ACTIVE_PLAYER)),
    TRIGGER_82(82),
    SHIFTOPNPC(-1, pointers = EnumSet.of(PointerType.ACTIVE_NPC)),
    SHIFTOPLOC(-1, pointers = EnumSet.of(PointerType.ACTIVE_LOC)),
    SHIFTOPOBJ(-1, pointers = EnumSet.of(PointerType.ACTIVE_OBJ)),
    SHIFTOPPLAYER(-1, pointers = EnumSet.of(PointerType.ACTIVE_PLAYER)),
    SHIFTOPTILE(-1, pointers = EnumSet.of(PointerType.ACTIVE_TILE)),
    ;

    override val identifier: String get() = name.lowercase()
}
