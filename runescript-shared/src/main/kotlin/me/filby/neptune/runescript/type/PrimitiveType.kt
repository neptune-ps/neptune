package me.filby.neptune.runescript.type

/**
 * An enumeration of types used in our type system, including any that would normally be used in RuneScript or
 * ClientScript.
 */
public enum class PrimitiveType(
    public override val code: Char?,
    public override val baseType: BaseVarType = BaseVarType.INTEGER,
    public override val defaultValue: Any?
) : Type {
    // custom types
    UNDEFINED,
    NULL,

    // verified script var types
    INT('i', defaultValue = 0),
    BOOLEAN('1', defaultValue = 0),
    STRING('s', BaseVarType.STRING, defaultValue = ""),
    MAPELEMENT('µ', defaultValue = -1),
    LONG('Ï', BaseVarType.LONG, defaultValue = 1L),
    ;

    override val representation: String = name.lowercase()

    /**
     * A [PrimitiveType] type that only defines the [baseType] and [defaultValue].
     */
    constructor(baseType: BaseVarType = BaseVarType.INTEGER, defaultValue: Any? = null) : this(
        null,
        baseType,
        defaultValue
    )

    public companion object {
        /**
         * A map of [representation] to [PrimitiveType].
         */
        private val lookupMap = values().associateBy { it.representation }

        /**
         * Finds a [PrimitiveType] by its [name].
         */
        public fun lookup(name: String): PrimitiveType? = lookupMap[name]
    }
}
