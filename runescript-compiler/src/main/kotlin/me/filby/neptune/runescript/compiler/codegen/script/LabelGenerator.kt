package me.filby.neptune.runescript.compiler.codegen.script

/**
 * A class used to generate unique label names by appending a number to the end
 * based on the number of times the name has been used.
 */
internal class LabelGenerator {
    /**
     * A map of names that have been generated with the number of them that have been generated.
     */
    private val names = mutableMapOf<String, Int>()

    /**
     * Generates a new version of [name] with an incremented number at the end
     * if the name has previously been used.
     */
    fun generate(name: String): Label {
        val count = names[name] ?: 0
        names[name] = count + 1
        return Label("${name}_$count")
    }

    /**
     * Resets the internal map of names to reset name counts.
     */
    fun reset() {
        names.clear()
    }
}
