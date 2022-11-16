package me.filby.neptune.runescript.compiler.type

import me.filby.neptune.runescript.compiler.type.wrapped.ArrayType
import kotlin.reflect.KClass

public typealias TypeChecker = (left: Type, right: Type) -> Boolean
public typealias TypeBuilder = MutableTypeOptions.() -> Unit

/**
 * Handles the mapping from name to [Type] along with centralized location for comparing types.
 */
public class TypeManager {

    /**
     * A map of type names to the [Type].
     */
    private val nameToType = mutableMapOf<String, Type>()

    /**
     * A list of possible checkers to run against types.
     */
    private val checkers = mutableListOf<TypeChecker>()

    /**
     * Registers [type] using [name] for lookup.
     */
    public fun register(name: String, type: Type) {
        val existingType = nameToType.putIfAbsent(name, type)
        if (existingType != null) {
            error("Type '$name' is already registered.")
        }
    }

    /**
     * Registers [type] using [Type.representation] for lookup.
     */
    public fun register(type: Type) {
        register(type.representation, type)
    }

    /**
     * Creates and registers a new type.
     */
    public fun register(
        name: String,
        code: Char? = null,
        baseType: BaseVarType = BaseVarType.INTEGER,
        defaultValue: Any = -1,
        builder: TypeBuilder? = null
    ): Type {
        val options = MutableTypeOptions()
        builder?.invoke(options)

        val newType = object : Type {
            override val representation = name
            override val code = code
            override val baseType = baseType
            override val defaultValue = defaultValue
            override val options = options
        }
        register(newType)
        return newType
    }

    /**
     * Registers all values within [enum] to the name lookup.
     */
    public fun <T> registerAll(enum: KClass<T>) where T : Enum<T>, T : Type {
        for (value in enum.java.enumConstants) {
            register(value)
        }
    }

    /**
     * Registers all values within [T] to the name lookup.
     */
    public inline fun <reified T> registerAll() where T : Enum<T>, T : Type {
        registerAll(T::class)
    }

    /**
     * Searches for [name] and allows changing the [TypeOptions] for the type. If a
     * type wasn't found with the given name an error is thrown.
     */
    public fun changeOptions(name: String, builder: TypeBuilder) {
        val type = nameToType[name] ?: error("$name was not found")
        val options = type.options as MutableTypeOptions
        options.builder()
    }

    /**
     * Finds a type by [name]. If [allowArray] is enabled, names ending with `array`
     * will attempt to find and wrap the type with [ArrayType].
     *
     * If the type doesn't exist, [MetaType.Error] is returned.
     */
    public fun find(name: String, allowArray: Boolean = false): Type {
        if (allowArray && name.endsWith("array")) {
            // substring before last "array" to prevent requesting intarrayarray (or deeper)
            val baseType = name.substringBeforeLast("array")
            val type = find(baseType)
            if (type == MetaType.Error || !type.options.allowArray) {
                return MetaType.Error
            }
            return ArrayType(type)
        }
        return nameToType[name] ?: MetaType.Error
    }

    /**
     * Adds [checker] to be called when calling [check].
     *
     * A checker should aim to only do simple checks and avoid covering a wide range of
     * types unless you really know what you're doing.
     *
     * The follow example would allow `namedobj` to be assigned to `obj` but not vice-versa.
     * ```
     * addTypeChecker { left, right -> left == OBJ && right == NAMEDOBJ }
     * ```
     */
    public fun addTypeChecker(checker: TypeChecker) {
        checkers += checker
    }

    /**
     * Checks to see if [right] is assignable to [left].
     */
    public fun check(left: Type, right: Type): Boolean {
        return checkers.any { checker -> checker(left, right) }
    }
}
