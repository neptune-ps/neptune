package me.filby.neptune.runescript.compiler.codegen.script

import me.filby.neptune.runescript.compiler.trigger.TriggerType

/**
 * A representation of a script containing the blocks of instructions and switch tables.
 */
public class RuneScript(public val trigger: TriggerType, public val name: String) {
    /**
     * The blocks of instructions that make up the script.
     */
    public val blocks: MutableList<Block> = mutableListOf()

    /**
     * The switch tables used within the script.
     */
    public val switchTables: MutableList<SwitchTable> = mutableListOf()

    /**
     * Generates a new switch table and adds it to the internal list of switch tables.
     */
    public fun generateSwitchTable(): SwitchTable {
        val newTable = SwitchTable(switchTables.size)
        switchTables += newTable
        return newTable
    }
}
