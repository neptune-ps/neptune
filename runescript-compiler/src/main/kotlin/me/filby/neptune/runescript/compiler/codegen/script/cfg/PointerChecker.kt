package me.filby.neptune.runescript.compiler.codegen.script.cfg

import com.github.michaelbull.logging.InlineLogger
import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostic
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticMessage
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticType
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostics
import me.filby.neptune.runescript.compiler.pointer.PointerHolder
import me.filby.neptune.runescript.compiler.pointer.PointerType
import me.filby.neptune.runescript.compiler.symbol.BasicSymbol
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.trigger.TriggerType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClanSettingsType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClanType
import java.util.EnumSet
import java.util.IdentityHashMap
import java.util.LinkedList
import kotlin.system.measureTimeMillis

internal class PointerChecker(
    private val diagnostics: Diagnostics,
    private val scripts: List<RuneScript>,
    private val commandPointers: Map<String, PointerHolder>,
) {
    /**
     * Logger for this class.
     */
    private val logger = InlineLogger()

    /**
     * Mapping of a scripts symbol to the code generated script.
     */
    private val scriptsBySymbol = scripts.associateByTo(IdentityHashMap(scripts.size)) { it.symbol }

    /**
     * Local instance of the graph generator.
     */
    private val graphGenerator = GraphGenerator(commandPointers)

    /**
     * Cache of script graphs.
     */
    private val scriptGraphs = IdentityHashMap<ScriptSymbol, List<InstructionNode>>(scripts.size)

    /**
     * Cache of calculated [PointerHolder]s.
     */
    private val scriptPointers = IdentityHashMap<ScriptSymbol, PointerHolder>(scripts.size)

    /**
     * Contains the scripts currently having their pointers calculated.
     */
    private val pendingScripts = LinkedHashSet<ScriptSymbol>()

    /**
     * The control flow graph for the script.
     *
     * Generates using [GraphGenerator] and caches the result for all future calls.
     */
    private val RuneScript.graph: List<InstructionNode>
        get() {
            val cached = scriptGraphs[symbol]
            if (cached != null) {
                return cached
            }

            val graph = graphGenerator.generate(blocks)
            scriptGraphs[symbol] = graph
            return graph
        }

    /**
     * The pointer information for the script.
     *
     * Calculates using [calculatePointers] and caches the result for all future calls.
     */
    private val ScriptSymbol.pointers: PointerHolder
        get() {
            val cached = scriptPointers[this]
            if (cached != null) {
                return cached
            }

            val calculated = calculatePointers()
            scriptPointers[this] = calculated
            return calculated
        }

    fun run() {
        for (script in scripts) {
            val time = measureTimeMillis {
                script.validatePointers()
            }
            logger.trace { "Checked pointers for ${script.fullName} in ${time}ms" }
        }
    }

    /**
     * Calculates the pointers a script requires, sets, and/or corrupts. This is generally
     * only called for scripts that are invokable (e.g. procs).
     *
     * @see pointers
     */
    private fun ScriptSymbol.calculatePointers(): PointerHolder {
        // temporary fix for recursive scripts to prevent stack overflow
        if (this in pendingScripts) {
            return PointerHolder(emptySet(), emptySet(), false, emptySet())
        }

        val script = scriptsBySymbol[this] ?: error("Unable to find script from sym: $this")
        val required = EnumSet.noneOf(PointerType::class.java)
        val set = EnumSet.noneOf(PointerType::class.java)
        val corrupted = EnumSet.noneOf(PointerType::class.java)

        pendingScripts += this
        for (pointer in PointerType.values()) {
            if (script.requiresPointer(pointer)) {
                required += pointer
            }

            if (script.setsPointer(pointer)) {
                set += pointer
            }

            if (script.corruptsPointer(pointer)) {
                corrupted += pointer
            }
        }
        pendingScripts -= this

        return PointerHolder(required, set, false, corrupted)
    }

    private fun RuneScript.validatePointers() {
        // TODO cache values()
        for (pointer in PointerType.values()) {
            validatePointer(pointer)
        }
    }

    /**
     * Verifies that [pointer] is available everywhere that is it needed. If a pointer was deemed
     * not valid, an error is reported to [diagnostics].
     */
    private fun RuneScript.validatePointer(pointer: PointerType) {
        val required = graph.filterTo(ArrayList()) { it.requiresPointer(pointer) }
        val set = graph.filterTo(ArrayList()) { it.setsPointer(pointer) }
        val corrupted = graph.filterTo(ArrayList()) { it.corruptsPointer(pointer) }

        // Check if the trigger implicitly defines the pointer
        if (!trigger.setsPointer(pointer)) {
            // If the trigger doesn't implicitly define the pointer we need to specify the starting
            // node as corrupting it so that there is a path found, resulting in an error.
            corrupted += graph.first()
        }

        // Attempt to find a path between any of the nodes that require the pointer and any nodes
        // that corrupt the pointer.
        val path = findEdgePath(
            required,
            { it in corrupted },
            { it.previous.filterNotTo(ArrayList()) { prev -> prev in set } },
        )

        // If a path was found then there is an error to raise.
        if (path != null) {
            val errorNode = path.first()
            val errorLocation = errorNode.instruction?.source ?: error("Unknown instruction source.")
            val corruptedNode = path.last()
            val isCorrupted = corruptedNode != graph.first() && corruptedNode != errorNode

            val message = if (isCorrupted) {
                DiagnosticMessage.POINTER_CORRUPTED
            } else {
                DiagnosticMessage.POINTER_UNINITIALIZED
            }

            val error = Diagnostic(DiagnosticType.ERROR, errorLocation, message, listOf(pointer.representation))
            diagnostics.report(error)

            if (isCorrupted) {
                val corruptedLocation = corruptedNode.instruction?.source ?: error("Unknown instruction source.")
                val hint = Diagnostic(
                    DiagnosticType.HINT,
                    corruptedLocation,
                    DiagnosticMessage.POINTER_CORRUPTED_LOC,
                    listOf(pointer.representation),
                )
                diagnostics.report(hint)
            }

            fun logProcRequirement(node: InstructionNode) {
                if (node.instruction == null || node.instruction.opcode != Opcode.Gosub) {
                    return
                }

                val symbol = node.instruction.operand as ScriptSymbol
                val script = scripts.find { it.symbol == symbol } ?: error("Unable to find script.")
                val scriptPath = script.requiresPointerPath(pointer) ?: error("Unable to find requirement path?")
                val requireNode = scriptPath.first()
                val requireLocation = requireNode.instruction?.source ?: error("Invalid instruction/source")

                diagnostics.report(
                    Diagnostic(
                        DiagnosticType.HINT,
                        requireLocation,
                        DiagnosticMessage.POINTER_REQUIRED_LOC,
                        listOf(pointer.representation),
                    ),
                )
                logProcRequirement(requireNode)
            }
            logProcRequirement(errorNode)

            // for ((index, node) in graph.withIndex()) {
            //     if (index == 0) {
            //         continue
            //     }
            //     if (node is PointerInstructionNode) {
            //         println("    $index[label=\"SetPointer ${node.set.joinToString { it.representation }}\"]")
            //         continue
            //     }
            //     val operand = when (val operand = node.instruction?.operand) {
            //         is ScriptSymbol.ClientScriptSymbol -> operand.name
            //         is LocalVariableSymbol -> "${operand.type}:\$${operand.name}"
            //         is BasicSymbol -> "${operand.type}:${operand.name}"
            //         is Label -> operand.name
            //         is SwitchTable -> operand.id
            //         is Unit -> ""
            //         else -> operand.toString()
            //     }
            //     if (node in path) {
            //         println("    $index[label=\"${node.instruction?.opcode?.javaClass?.simpleName} ${operand}\" color=\"red\"]")
            //     } else {
            //         println("    $index[label=\"${node.instruction?.opcode?.javaClass?.simpleName} ${operand}\"]")
            //     }
            // }
            //
            // for (node in graph) {
            //     val index = graph.indexOf(node)
            //     for (next in node.next) {
            //         println("    $index -> ${graph.indexOf(next)}")
            //     }
            // }
        }
    }

    /**
     * Checks if the [TriggerType] sets [pointer] by default.
     */
    private fun TriggerType.setsPointer(pointer: PointerType): Boolean {
        val pointers = pointers
        return pointers != null && pointer in pointers
    }

    /**
     * Checks if [RuneScript] requires the [pointer] to be called.
     */
    private fun RuneScript.requiresPointer(pointer: PointerType): Boolean = requiresPointerPath(pointer) != null

    /**
     * Finds a path from instructions that require [pointer] to the first node without passing through
     * an instruction that sets the pointer.
     */
    private fun RuneScript.requiresPointerPath(pointer: PointerType): List<InstructionNode>? {
        val usages = graph.filterTo(ArrayList()) { it.requiresPointer(pointer) }
        return findEdgePath(
            usages,
            { it == graph.first() },
            { it.previous.filterNotTo(ArrayList(1)) { prev -> prev.setsPointer(pointer) } },
        )
    }

    /**
     * Checks if [RuneScript] sets the [pointer] after being called.
     */
    private fun RuneScript.setsPointer(pointer: PointerType): Boolean {
        val returns = graph.filterTo(ArrayList()) { it.instruction?.opcode == Opcode.Return }

        // check that all paths from start to return pass through a pointer assignment (no path
        // not passing through any assignment exist)
        return findEdgePath(
            returns,
            { it == graph.first() || it.corruptsPointer(pointer) },
            { it.previous.filterNotTo(ArrayList()) { prev -> prev.setsPointer(pointer) } },
        ) == null
    }

    /**
     * Checks if [RuneScript] corrupts the [pointer] after being called.
     */
    private fun RuneScript.corruptsPointer(pointer: PointerType): Boolean {
        val returns = graph.filterTo(ArrayList()) { it.instruction?.opcode == Opcode.Return }

        return findEdgePath(
            returns,
            { it.corruptsPointer(pointer) },
            { it.previous.filterNotTo(ArrayList()) { prev -> prev.setsPointer(pointer) } },
        ) != null
    }

    /**
     * Checks if the instruction requires [pointer].
     */
    private fun InstructionNode.requiresPointer(pointer: PointerType): Boolean {
        if (instruction == null) {
            return false
        }

        return when (instruction.opcode) {
            Opcode.Command -> {
                val command = instruction.operand as ScriptSymbol
                val pointers = commandPointers[command.name] ?: return false
                pointer in pointers.required
            }
            Opcode.Gosub, Opcode.Jump -> {
                val symbol = instruction.operand as ScriptSymbol
                pointer in symbol.pointers.required
            }
            Opcode.PushVar, Opcode.PopVar -> {
                val symbol = instruction.operand as BasicSymbol
                when (symbol.type) {
                    // is VarPlayerType -> pointer == PointerType.ACTIVE_PLAYER
                    // is VarBitType -> pointer == PointerType.ACTIVE_PLAYER
                    is VarClanType -> pointer == PointerType.ACTIVE_CLANPROFILE
                    is VarClanSettingsType -> pointer == PointerType.ACTIVE_CLANSETTINGS
                    else -> false
                }
            }
            else -> false
        }
    }

    /**
     * Checks if the instruction sets [pointer].
     */
    private fun InstructionNode.setsPointer(pointer: PointerType): Boolean {
        if (this is PointerInstructionNode) {
            // special node inserted for commands that conditionally set a pointer
            return pointer in set
        }

        if (instruction == null) {
            return false
        }

        return when (instruction.opcode) {
            Opcode.Command -> {
                val command = instruction.operand as ScriptSymbol
                val pointers = commandPointers[command.name] ?: return false
                pointer in pointers.set && !pointers.conditionalSet
            }
            Opcode.Gosub -> {
                val symbol = instruction.operand as ScriptSymbol
                pointer in symbol.pointers.set
            }
            else -> false
        }
    }

    /**
     * Checks if the instruction corrupts [pointer].
     */
    private fun InstructionNode.corruptsPointer(pointer: PointerType): Boolean {
        if (instruction == null) {
            return false
        }
        return when (instruction.opcode) {
            Opcode.Command -> {
                val command = instruction.operand as ScriptSymbol
                val pointers = commandPointers[command.name] ?: return false
                pointer in pointers.corrupted
            }
            Opcode.Gosub -> {
                val symbol = instruction.operand as ScriptSymbol
                pointer in symbol.pointers.corrupted
            }
            else -> false
        }
    }

    /**
     * Attempts to find a path starting from any neighbors of any nodes within [starts].
     */
    private inline fun findEdgePath(
        starts: List<InstructionNode>,
        end: (InstructionNode) -> Boolean,
        getNeighbors: (InstructionNode) -> List<InstructionNode>?,
    ): List<InstructionNode>? {
        if (starts.isEmpty()) {
            return null
        }

        val startSource = IdentityHashMap<InstructionNode, InstructionNode?>()
        val sources = IdentityHashMap<InstructionNode, InstructionNode?>()
        val queue = ArrayDeque<InstructionNode>()
        for (start in starts) {
            val neighbors = getNeighbors(start) ?: continue
            for (neighbor in neighbors) {
                if (!sources.containsKey(neighbor)) {
                    startSource[neighbor] = start
                    sources[neighbor] = null
                    queue.add(neighbor)
                }
            }
        }
        while (!queue.isEmpty()) {
            var current: InstructionNode = queue.removeFirst()
            if (end(current)) {
                val result = LinkedList<InstructionNode>()
                while (true) {
                    result.addFirst(current)
                    current = sources[current] ?: break
                }
                result.addFirst(startSource[result.first]!!)
                return result
            }
            val neighbors = getNeighbors(current) ?: continue
            for (neighbor in neighbors) {
                if (!sources.containsKey(neighbor)) {
                    sources[neighbor] = current
                    queue.add(neighbor)
                }
            }
        }
        return null
    }
}
