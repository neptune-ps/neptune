package me.filby.neptune.runescript.compiler.type

/**
 * An enumeration of types used in our type system, including any that would normally be used in RuneScript or
 * ClientScript.
 */
public enum class PrimitiveType(
    public override val code: Char?,
    public override val baseType: BaseVarType = BaseVarType.INTEGER,
    public override val defaultValue: Any?,
    builder: TypeBuilder? = null,
) : Type {
    // verified script var types
    INT('i', defaultValue = 0),
    BOOLEAN('1', defaultValue = 0),
    COMPONENT('I', defaultValue = -1),
    NAMEDOBJ('O', defaultValue = -1),
    STAT('S', defaultValue = -1),
    COORD('c', defaultValue = -1),
    NPC_STAT('T', defaultValue = -1),
    GRAPHIC('d', defaultValue = -1),
    ENUM('g', defaultValue = -1),
    OBJ('o', defaultValue = -1),
    STRING(
        's',
        BaseVarType.STRING,
        defaultValue = "",
        {
            allowArray = false
            allowSwitch = false
        }
    ),
    MAPELEMENT('µ', defaultValue = -1),
    INV('v', defaultValue = -1),
    CHAR('z', defaultValue = -1),
    LONG(
        'Ï',
        BaseVarType.LONG,
        defaultValue = -1L,
        {
            allowArray = false
            allowSwitch = false
        }
    ),
    ;

    override val representation: String = name.lowercase()

    override val options: TypeOptions = MutableTypeOptions().apply { builder?.invoke(this) }

    /**
     * A [PrimitiveType] type that only defines the [baseType] and [defaultValue].
     */
    constructor(baseType: BaseVarType = BaseVarType.INTEGER, defaultValue: Any? = null) : this(
        null,
        baseType,
        defaultValue
    )
}
