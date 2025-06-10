package me.filby.neptune.runescript.compiler.type

/**
 * The main types used in the compiler.
 *
 * These types are the ones that are expected to exist to use the compiler.
 * There are other types the compiler will reference dynamically, but those
 * types are counted as optional and will error if they are not defined when
 * trying to access them.
 */
public enum class PrimitiveType(
    public override val code: Char?,
    public override val baseType: BaseVarType = BaseVarType.INTEGER,
    public override val defaultValue: Any?,
    builder: TypeBuilder? = null,
) : Type {
    INT('i', defaultValue = 0),
    BOOLEAN('1', defaultValue = 0),
    COORD('c', defaultValue = -1),
    STRING('s', BaseVarType.STRING, defaultValue = "", {
        allowArray = true
        allowSwitch = false
    }),
    CHAR('z', defaultValue = -1),
    LONG('Ï', BaseVarType.LONG, defaultValue = -1L, {
        allowArray = false
        allowSwitch = false
    }),
    ;

    override val representation: String = name.lowercase()

    override val options: TypeOptions = MutableTypeOptions().apply { builder?.invoke(this) }

    /**
     * A [PrimitiveType] type that only defines the [baseType] and [defaultValue].
     */
    constructor(baseType: BaseVarType = BaseVarType.INTEGER, defaultValue: Any? = null) : this(
        null,
        baseType,
        defaultValue,
    )
}
