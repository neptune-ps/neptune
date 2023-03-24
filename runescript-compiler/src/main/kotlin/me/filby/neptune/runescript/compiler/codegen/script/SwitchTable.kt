package me.filby.neptune.runescript.compiler.codegen.script

/**
 * A table of [SwitchCase]s.
 */
public class SwitchTable(public val id: Int) {
    /**
     * The list of cases within the switch table.
     */
    private val _cases = mutableListOf<SwitchCase>()

    /**
     * An immutable list of cases within the switch table.
     */
    public val cases: List<SwitchCase> get() = _cases

    /**
     * Adds the [case] to the switch table.
     */
    public fun addCase(case: SwitchCase) {
        _cases += case
    }

    /**
     * A switch case that can contain multiple keys that point to a single label.
     */
    public data class SwitchCase(public val label: Label, public val keys: List<Any>)
}
