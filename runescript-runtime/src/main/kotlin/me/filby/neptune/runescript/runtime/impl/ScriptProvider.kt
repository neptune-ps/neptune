package me.filby.neptune.runescript.runtime.impl

import me.filby.neptune.runescript.runtime.Script

/**
 * An interface that is used to look up a [Script] by its id.
 */
public interface ScriptProvider {
    /**
     * Gets a [Script] by [id].
     */
    public fun get(id: Int): Script
}
