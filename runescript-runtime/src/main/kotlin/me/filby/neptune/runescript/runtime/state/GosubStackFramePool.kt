package me.filby.neptune.runescript.runtime.state

/**
 * A simple pool for [GosubStackFrame]s to avoid unnecessary allocations.
 */
public class GosubStackFramePool(initialCapacity: Int = 64, private val maxCapacity: Int = 128) {
    /**
     * Contains all of the [GosubStackFrame] available within the pool.
     */
    private val pool = ArrayDeque<GosubStackFrame>(initialCapacity)

    init {
        repeat(initialCapacity) {
            pool.addLast(GosubStackFrame())
        }
    }

    /**
     * Returns an instance of [GosubStackFrame]. If the pool is empty, a new instance is created.
     */
    public fun pop(): GosubStackFrame {
        return pool.removeLastOrNull() ?: GosubStackFrame()
    }

    /**
     * Resets and pushed [frame] to the pool. If the pool is full then it is reset and discarded.
     */
    public fun push(frame: GosubStackFrame) {
        frame.reset()

        if (pool.size < maxCapacity) {
            pool.addLast(frame)
        }
    }
}
