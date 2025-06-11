package me.filby.neptune.runescript.runtime

import me.filby.neptune.runescript.compiler.type.BaseVarType
import kotlin.math.max
import kotlin.math.min

/**
 * Represents an array of values used in the script runtime. The array is capable of storing
 * int-based, long-based, or string types.
 */
public class ScriptArray {
    public val type: BaseVarType
    public val mutable: Boolean
    private var size: Int = 0
    private var capacity: Int = 0
    private val defaultValue: Any?
    public lateinit var ints: IntArray
        private set
    public lateinit var longs: LongArray
        private set
    public lateinit var strings: Array<String?>
        private set

    public constructor(baseType: BaseVarType, defaultValue: Any?, size: Int, capacity: Int) {
        this.mutable = true
        this.type = baseType
        this.capacity = capacity
        this.defaultValue = defaultValue
        if (baseType == BaseVarType.INTEGER) {
            this.ints = IntArray(capacity)
        } else if (baseType == BaseVarType.LONG) {
            this.longs = LongArray(capacity)
        } else if (baseType == BaseVarType.STRING) {
            this.strings = arrayOfNulls<String>(capacity)
        }
        setSize(size)
    }

    public operator fun get(index: Int): Any? = when (type) {
        BaseVarType.INTEGER -> ints[index]
        BaseVarType.LONG -> longs[index]
        else -> strings[index]
    }

    public operator fun set(index: Int, value: Any?) {
        when (type) {
            BaseVarType.INTEGER -> ints[index] = value as Int
            BaseVarType.LONG -> longs[index] = value as Long
            else -> strings[index] = value as String?
        }
    }

    public fun resize(newSize: Int) {
        growStorage(newSize)
    }

    public fun growStorage(newCapacity: Int) {
        check(mutable)
        check(newCapacity in 0..MAX_ARRAY_CAPACITY)
        setCapacity(calculateNewCapacity(newCapacity))
    }

    private fun calculateNewCapacity(newCapacity: Int): Int {
        val expandedCapacity = (this.capacity shr 1) + this.capacity
        return max(newCapacity, min(expandedCapacity, MAX_ARRAY_CAPACITY))
    }

    private fun setCapacity(newCapacity: Int) {
        if (capacity != newCapacity) {
            capacity = newCapacity
            if (type == BaseVarType.INTEGER) {
                ints = ints.copyOf(newCapacity)
            } else if (type == BaseVarType.LONG) {
                longs = longs.copyOf(newCapacity)
            } else if (type == BaseVarType.STRING) {
                strings = strings.copyOf(newCapacity)
            }
        }
    }

    public fun setSize(newSize: Int) {
        val prevSize = size
        size = newSize
        if (newSize < prevSize) {
            if (type == BaseVarType.INTEGER) {
                ints.fill(0, newSize, prevSize)
            } else if (type == BaseVarType.LONG) {
                longs.fill(0L, newSize, prevSize)
            } else if (type == BaseVarType.STRING) {
                strings.fill(null, newSize, prevSize)
            }
        } else if (newSize > prevSize) {
            if (type == BaseVarType.INTEGER) {
                val default = defaultValue as Int
                if (default != 0) {
                    ints.fill(default, prevSize, newSize)
                }
            } else if (type == BaseVarType.LONG) {
                val default = defaultValue as Long
                if (default != 0L) {
                    longs.fill(default, prevSize, newSize)
                }
            } else if (defaultValue != null) {
                strings.fill(defaultValue as String?, prevSize, newSize)
            }
        }
    }

    private companion object {
        private const val MAX_ARRAY_CAPACITY = 5000
    }
}
