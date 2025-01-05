package me.filby.neptune.runescript.runtime.state

import me.filby.neptune.runescript.runtime.Script
import me.filby.neptune.runescript.runtime.ScriptRunner
import java.io.Closeable

/**
 * A callback that is run after a script finishes execution gracefully.
 */
public typealias ScriptFinishHandler<T> = T.() -> Unit

/**
 * Containers all state that is required for [ScriptRunner].
 */
public open class ScriptState : Closeable {
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

    /**
     * A callback that is executed when a script finishes executing fully. This
     * can be a graceful finish or aborted.
     */
    public var onComplete: ScriptFinishHandler<ScriptState>? = null

    /**
     * Returns the scripts opcodes.
     */
    public val opcodes: IntArray get() = script.opcodes

    /**
     * The program counter. Keeps track of the current instruction index.
     */
    public var pc: Int = -1

    /**
     * The number of instructions ran throughout the duration of the state.
     */
    public var opcount: Long = 0

    /**
     * The execution state of this script state.
     */
    public var execution: ExecutionState = ExecutionState.RUNNING

    /**
     * The stack of gosub frames allowing for restoring script state when a `return` is hit.
     */
    public val frames: ArrayDeque<GosubStackFrame> = ArrayDeque()

    /**
     * Local int variables.
     */
    public var intLocals: IntArray = IntArray(0)

    /**
     * Local object variables.
     */
    public var objLocals: Array<Any?> = emptyArray()

    /**
     * Local long variables.
     */
    public var longLocals: LongArray = LongArray(0)

    /**
     * Returns the `int` operand for the current instruction.
     */
    public val intOperand: Int get() = script.intOperands[pc]

    /**
     * Returns the `Object` operand for the current instruction.
     */
    public val objOperand: Any? get() = script.objOperands[pc]

    /**
     * The int stack.
     */
    private var ints: IntArray = IntArray(DEFAULT_STACK_SIZE)

    /**
     * The pointer to the end of the int stack ([ints]).
     */
    internal var intPointer: Int = 0

    /**
     * The object stack.
     */
    private var objs: Array<Any?> = arrayOfNulls(DEFAULT_STACK_SIZE)

    /**
     * The pointer to the end of the object stack ([objs]).
     */
    internal var objPointer: Int = 0

    /**
     * The long stack.
     */
    private var longs: LongArray = LongArray(DEFAULT_STACK_SIZE)

    /**
     * The pointer to the end of the long stack ([longs]).
     */
    internal var longPointer: Int = 0

    /**
     * Removes an [Int] from the end of the stack and returns it.
     */
    public fun popInt(): Int = ints[--intPointer]

    /**
     * Adds [value] to the end of the int stack.
     */
    public fun pushInt(value: Int) {
        checkIntStackSize(intPointer + 1)
        ints[intPointer++] = value
    }

    private fun checkIntStackSize(newSize: Int) {
        if (newSize > MAX_STACK_SIZE) {
            error("int stack exceeding $MAX_STACK_SIZE in size!")
        } else if (newSize > ints.size) {
            ints = ints.copyOf(ints.size * 2)
        }
    }

    // object stack

    /**
     * Removes an object from the end of the stack and returns it. `Int`s and
     * `Long`s should be popped via [popInt] and [popLong].
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T> popObj(): T = objs[--objPointer] as T

    /**
     * Adds [value] to the end of the stack. `Int`s and `Long`s should be
     * pushed via [pushInt] and [pushLong].
     */
    public fun <T> pushObj(value: T) {
        checkObjStackSize(objPointer + 1)
        objs[objPointer++] = value
    }

    private fun checkObjStackSize(newSize: Int) {
        if (newSize > MAX_STACK_SIZE) {
            error("obj stack exceeding $MAX_STACK_SIZE in size!")
        } else if (newSize > ints.size) {
            objs = objs.copyOf(ints.size * 2)
        }
    }

    // long stack

    /**
     * Removes a [Long] from the end of the stack and returns it.
     */
    public fun popLong(): Long = longs[--longPointer]

    /**
     * Adds [value] to the end of the long stack.
     */
    public fun pushLong(value: Long) {
        checkLongStackSize(longPointer + 1)
        longs[longPointer++] = value
    }

    private fun checkLongStackSize(newSize: Int) {
        if (newSize > MAX_STACK_SIZE) {
            error("int stack exceeding $MAX_STACK_SIZE in size!")
        } else if (newSize > ints.size) {
            longs = longs.copyOf(ints.size * 2)
        }
    }

    /**
     * Handles configuring the state based on the [script].
     */
    public open fun setup(script: Script) {
        _script = script
        intLocals = IntArray(script.intLocalCount)
        objLocals = arrayOfNulls(script.objLocalCount)
        longLocals = LongArray(script.longLocalCount)
    }

    /**
     * Handles resetting to default state for reuse later (e.g. pooling).
     */
    public open fun reset() {
        _script = null
        onComplete = null
        pc = -1
        opcount = 0
        execution = ExecutionState.RUNNING
        intLocals = IntArray(0)
        objLocals = emptyArray()
        longLocals = LongArray(0)
        ints = IntArray(DEFAULT_STACK_SIZE)
        intPointer = 0
        objs = arrayOfNulls(DEFAULT_STACK_SIZE)
        objPointer = 0
        longs = LongArray(DEFAULT_STACK_SIZE)
        longPointer = 0
    }

    /**
     * Function that is called when the script state is no longer needed by anything.
     * This can be used to reset and push the state back into a pool.
     */
    override fun close() {
        reset()
    }

    /**
     * The execution state of a script.
     */
    public enum class ExecutionState {
        /**
         * The script is actively running.
         */
        RUNNING,

        /**
         * The script has been suspended by something and is waiting to be resumed.
         */
        SUSPENDED,

        /**
         * The script has finished executing successfully. There may be left over information
         * in the stack from the script returning values.
         */
        FINISHED,

        /**
         * The script has been aborted. This can be used to stop execution at any point. This
         * is different from [FINISHED] because this does not necessarily mean a script exited
         * gracefully.
         */
        ABORTED,
    }

    private companion object {
        const val DEFAULT_STACK_SIZE = 16
        const val MAX_STACK_SIZE = 1024
    }
}
