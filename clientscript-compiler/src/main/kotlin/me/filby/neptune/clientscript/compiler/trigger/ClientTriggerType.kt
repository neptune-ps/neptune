package me.filby.neptune.clientscript.compiler.trigger

import me.filby.neptune.runescript.compiler.trigger.TriggerType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type

/**
 * An enumeration of valid trigger types for use in ClientScript.
 */
enum class ClientTriggerType(
    override val id: Int,
    override val subjectType: Type? = null,
    override val allowParameters: Boolean = false,
    override val parameters: Type? = null,
    override val allowReturns: Boolean = false,
    override val returns: Type? = null,
) : TriggerType {
    OPWORLDMAPELEMENT1(10, subjectType = PrimitiveType.MAPELEMENT),
    OPWORLDMAPELEMENT2(11, subjectType = PrimitiveType.MAPELEMENT),
    OPWORLDMAPELEMENT3(12, subjectType = PrimitiveType.MAPELEMENT),
    OPWORLDMAPELEMENT4(13, subjectType = PrimitiveType.MAPELEMENT),
    OPWORLDMAPELEMENT5(14, subjectType = PrimitiveType.MAPELEMENT),
    WORLDMAPELEMENTMOUSEOVER(
        15,
        subjectType = PrimitiveType.MAPELEMENT,
        allowParameters = true,
        parameters = TupleType(PrimitiveType.INT, PrimitiveType.INT)
    ),
    WORLDMAPELEMENTMOUSELEAVE(
        16,
        subjectType = PrimitiveType.MAPELEMENT,
        allowParameters = true,
        parameters = TupleType(PrimitiveType.INT, PrimitiveType.INT)
    ),
    WORLDMAPELEMENTMOUSEREPEAT(
        17,
        subjectType = PrimitiveType.MAPELEMENT,
        allowParameters = true,
        parameters = TupleType(PrimitiveType.INT, PrimitiveType.INT)
    ),
    LOADNPC(35),
    LOADLOC(37),
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
