package me.filby.neptune.runescript.compiler.trigger

import kotlin.reflect.KClass

/**
 * Handles mapping from name to [TriggerType].
 */
public class TriggerManager {
    /**
     * A map of trigger names to the [TriggerType].
     */
    private val nameToTrigger = mutableMapOf<String, TriggerType>()

    /**
     * Registers [trigger] using [name] for lookup.
     */
    public fun register(name: String, trigger: TriggerType) {
        val existingTrigger = nameToTrigger.putIfAbsent(name, trigger)
        if (existingTrigger != null) {
            error("Trigger '$name' is already registered.")
        }
    }

    /**
     * Registers [trigger] using [TriggerType.identifier] for lookup.
     */
    public fun register(trigger: TriggerType) {
        register(trigger.identifier, trigger)
    }

    /**
     * Registers all values within [enum] to the name lookup.
     */
    public fun <T> registerAll(enum: KClass<T>) where T : Enum<T>, T : TriggerType {
        for (value in enum.java.enumConstants) {
            register(value)
        }
    }

    /**
     * Registers all values within [T] to the name lookup.
     */
    public inline fun <reified T> registerAll() where T : Enum<T>, T : TriggerType {
        registerAll(T::class)
    }

    /**
     * Finds a trigger by [name]. If a trigger was not found an error is thrown.
     */
    public fun find(name: String): TriggerType {
        return nameToTrigger[name] ?: error("Unable to find trigger '$name'.")
    }

    /**
     * Finds a trigger by [name].
     *
     * If a trigger with the name was not found, `null` is returned.
     */
    public fun findOrNull(name: String): TriggerType? {
        return nameToTrigger[name]
    }
}
