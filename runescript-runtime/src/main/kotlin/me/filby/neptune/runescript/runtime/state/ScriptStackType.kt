package me.filby.neptune.runescript.runtime.state

/**
 * A script stack type defines which stack within [ScriptState] that a value is stored in.
 */
public enum class ScriptStackType {
    /**
     * A stack type that stores [Int] based types. This is the most common type.
     *
     * @see ScriptState.pushInt
     * @see ScriptState.popInt
     */
    INTEGER,

    /**
     * A stack type that is used to store any types of objects besides [Int] and [Long].
     *
     * @see ScriptState.pushObj
     * @see ScriptState.popObj
     */
    OBJECT,

    /**
     * A stack type that stores [Long] based types.
     *
     * @see ScriptState.pushLong
     * @see ScriptState.popLong
     */
    LONG,
    ;

    public companion object {
        /**
         * Looks up a [ScriptStackType] by id. If the id is not valid an error is thrown.
         */
        public fun forId(id: Int): ScriptStackType = when (id) {
            0 -> INTEGER
            1 -> OBJECT
            2 -> LONG
            else -> error("Unsupported ScriptStackType id: $id")
        }
    }
}
