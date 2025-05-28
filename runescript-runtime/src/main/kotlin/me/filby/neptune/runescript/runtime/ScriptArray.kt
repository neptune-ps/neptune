package me.filby.neptune.runescript.runtime

// temporary implementation of a type agnostic array
// TODO resizing, deleting, etc
public class ScriptArray(public val mutable: Boolean, public val size: Int, defaultValue: Any) {
    private val data: Any = if (defaultValue is Int) {
        IntArray(size) { defaultValue }
    } else {
        Array(size) { defaultValue }
    }

    public val isStringArray: Boolean = defaultValue is String

    public fun getInt(index: Int): Int = (data as IntArray)[index]

    public fun setInt(index: Int, value: Int) {
        (data as IntArray)[index] = value
    }

    @Suppress("UNCHECKED_CAST")
    public fun getString(index: Int): String = (data as Array<Any>)[index] as String

    @Suppress("UNCHECKED_CAST")
    public fun setString(index: Int, value: String) {
        (data as Array<Any>)[index] = value
    }
}
