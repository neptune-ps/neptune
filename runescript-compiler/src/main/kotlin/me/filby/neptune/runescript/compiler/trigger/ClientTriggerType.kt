package me.filby.neptune.runescript.compiler.trigger

import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type

/**
 * An enumeration of valid trigger types for use in ClientScript.
 */
public enum class ClientTriggerType(
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
    PROC(73, allowParameters = true, allowReturns = true),
    CLIENTSCRIPT(76, allowParameters = true),
    ;

    override val identifier: String get() = name.lowercase()

    public companion object {
        /**
         * A map of identifiers to the trigger type.
         */
        private val LOOKUP = values().associateBy { it.identifier }

        /**
         * Looks up a a [ClientTriggerType] using the unique string identifier. If one wasn't found `null` is returned.
         */
        public fun lookup(identifier: String): ClientTriggerType? = LOOKUP[identifier]
    }
}
