package me.filby.neptune.runescript.compiler.codegen.script

import me.filby.neptune.runescript.compiler.symbol.BasicSymbol
import me.filby.neptune.runescript.compiler.symbol.LocalVariableSymbol
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.trigger.TriggerType

/**
 * A representation of a script containing the blocks of instructions and switch tables.
 */
public class RuneScript(
    public val sourceName: String,
    public val symbol: ScriptSymbol,
    public val subjectReference: BasicSymbol?,
) {
    /**
     * The trigger of the script.
     */
    public val trigger: TriggerType = symbol.trigger

    /**
     * The name of the script.
     */
    public val name: String = symbol.name

    /**
     * Combination of `[trigger,name]`.
     */
    public val fullName: String = "[${trigger.identifier},$name]"

    /**
     * The table that contains all `LocalVariableSymbol`s defined within the script.
     */
    public val locals: LocalTable = LocalTable()

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

    /**
     * Containers all local variables declared in the script.
     */
    public inner class LocalTable internal constructor() {
        /**
         * A list of all parameters.
         */
        public val parameters: MutableList<LocalVariableSymbol> = mutableListOf()

        /**
         * A list of all variables. This will include all the variables in [parameters] as well.
         */
        public val all: MutableList<LocalVariableSymbol> = mutableListOf()
    }
}
