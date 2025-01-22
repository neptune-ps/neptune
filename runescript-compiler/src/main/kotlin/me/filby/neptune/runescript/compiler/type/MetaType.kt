package me.filby.neptune.runescript.compiler.type

import me.filby.neptune.runescript.compiler.trigger.TriggerType
import me.filby.neptune.runescript.compiler.type.wrapped.WrappedType
import me.filby.neptune.runescript.compiler.type.Type as MainType

/**
 * A sealed class of types used internally in the compiler.
 */
public sealed class MetaType(private val name: String) : MainType {
    /**
     * A type that is comparable to all other types. This is different to
     * [Error] as it is not meant for expressions that had an error during
     * type checking, and is intended for more complex signatures that need
     * to define a placeholder type that allows anything.
     *
     * Example: `MetaType.Type(MetaType.Any)`
     */
    public data object Any : MetaType("any")

    /**
     * A type that says that nothing is returned. This is intended to be used
     * for `error`-like and `jump`-like commands that will not continue
     * execution in the script that called it. This type should not be exposed
     * to scripts directly except for `command` declarations.
     *
     * See Also: [Bottom type](https://en.wikipedia.org/wiki/Bottom_type)
     */
    public data object Nothing : MetaType("nothing")

    /**
     * A type used to specify the type resolution resulted into an error. This
     * type is comparable to **all** other types to prevent error propagation.
     */
    public data object Error : MetaType("error")

    /**
     * A type that signifies that nothing is returned.
     */
    public data object Unit : MetaType("unit")

    /**
     * A special type used when referencing other types.
     */
    public data class Type(override val inner: MainType) :
        MetaType("type"),
        WrappedType {
        override val representation: String = "type<${inner.representation}>"
    }

    /**
     * A special type that refers to some sort of script. The type includes the
     * script trigger type, parameter type(s), and return type(s).
     */
    public class Script(
        public val trigger: TriggerType,
        public val parameterType: MainType,
        public val returnType: MainType,
    ) : MetaType("script") {
        override val representation: String = trigger.identifier
    }

    /**
     * A special type used when referencing a script with a trigger type of `clientscript`.
     * The [transmitListType] is the type allowed in the transmit list, if transmit list isn't expected,
     * use [Unit].
     */
    public data class Hook(val transmitListType: MainType) : MetaType("hook") {
        override val representation: String = "hook<${transmitListType.representation}>"
    }

    override val representation: String
        get() = name.lowercase()

    override val code: Char
        get() = error("MetaType has no character representation.")

    // all meta types are represented as an integer
    override val baseType: BaseVarType = BaseVarType.INTEGER

    // all meta types have a default value of `null` (-1).
    override val defaultValue: kotlin.Any = -1

    // all meta types are unable to be accessed in scripts through normal means
    override val options: TypeOptions = MutableTypeOptions(
        allowSwitch = false,
        allowArray = false,
        allowDeclaration = false,
    )
}
