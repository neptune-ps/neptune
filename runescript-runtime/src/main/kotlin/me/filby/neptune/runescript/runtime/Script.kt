package me.filby.neptune.runescript.runtime

import java.util.TreeMap

/**
 * An alias of an [Int] to [Int] map for a single switch table.
 */
public typealias SwitchTable = Map<Int, Int>

/**
 * Defines a script that is executable by a [ScriptRunner].
 */
public class Script(
    public val sourceInfo: SourceInfo?,
    public val opcodes: IntArray,
    public val intParameterCount: Int,
    public val objParameterCount: Int,
    public val longParameterCount: Int,
    public val intLocalCount: Int,
    public val objLocalCount: Int,
    public val longLocalCount: Int,
    public val intOperands: IntArray,
    public val objOperands: Array<Any?>,
    public val switchTables: List<SwitchTable>,
) {
    init {
        require(opcodes.size == intOperands.size) { "${opcodes.size} != ${intOperands.size}" }
        require(opcodes.size == objOperands.size) { "${opcodes.size} != ${objOperands.size}" }
    }

    /**
     * The script name if it was defined.
     */
    public val name: String? get() = sourceInfo?.name

    /**
     * Contains information about the source of a script.
     */
    public class SourceInfo(
        public val name: String? = null,
        public val path: String? = null,
        public val lineNumberTable: TreeMap<Int, Int>? = null,
    )
}
