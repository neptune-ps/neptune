package me.filby.neptune.runescript.runtime.state

import me.filby.neptune.runescript.runtime.Script

/**
 * A frame used to store a scripts local variables and pc used to restore previous
 * state after a `gosub` call is finished.
 */
public class GosubStackFrame {
    /**
     * Holds the current active script, if there is one.
     *
     * @see script
     */
    private var _script: Script? = null

    /**
     * The current active [Script].
     */
    public var script: Script
        get() = _script ?: error("script isn't set")
        set(value) {
            _script = value
        }

    public var pc: Int = -1
    public var intLocals: IntArray = IntArray(0)
    public var objLocals: Array<Any?> = emptyArray()
    public var longLocals: LongArray = LongArray(0)

    /**
     * Copies [ScriptState.pc] and local values for restoration in the future.
     */
    public fun setup(state: ScriptState) {
        _script = state.script
        pc = state.pc
        intLocals = state.intLocals.copyOf()
        objLocals = state.objLocals.copyOf()
        longLocals = state.longLocals.copyOf()
    }

    /**
     * Resets the values so that the instance can be reused (e.g. from within a pool).
     */
    public fun reset() {
        _script = null
        pc = -1
        intLocals = IntArray(0)
        objLocals = emptyArray()
        longLocals = LongArray(0)
    }
}
