package me.filby.neptune.runescript.compiler.configuration.command

import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.expr.CommandCallExpression
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.compiler.codegen.CodeGenerator
import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostics
import me.filby.neptune.runescript.compiler.symbol
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol

/**
 * Contains the context of the [CodeGenerator] and supplies useful functions when
 * implementing a [DynamicCommandHandler].
 */
public data class CodeGeneratorContext(
    private val codeGenerator: CodeGenerator,
    val expression: CommandCallExpression,
    val diagnostics: Diagnostics
) {
    /**
     * Emits a new instruction with the given [opcode] and [operand].
     */
    public fun <T : Any> instruction(opcode: Opcode<T>, operand: T) {
        codeGenerator.instruction(opcode, operand)
    }

    /**
     * Emits the line number instruction and the command call instruction. This
     * should be preferred over manually writing the command instruction when
     * possible.
     *
     * This is a shortcut to the following:
     * ```
     * expression.lineInstruction()
     * instruction(Opcode.COMMAND, expression.symbol)
     * ```
     */
    public fun command() {
        // the symbol is verified to be not null in CodeGenerator before calling user
        // code generation code which makes this safe, but we'll make the compiler happy.
        val symbol = requireNotNull(expression.symbol as ScriptSymbol)
        expression.lineInstruction()
        instruction(Opcode.Command, symbol)
    }

    /**
     * Inserts the line number meta instruction for the node.
     */
    public fun Node.lineInstruction() {
        codeGenerator.run {
            this@lineInstruction.lineInstruction()
        }
    }

    /**
     * Passes the node through the code generator if it is not `null`.
     */
    public fun Node?.visit() {
        codeGenerator.run {
            visit()
        }
    }

    /**
     * Passes the expression through the code generator if it is not `null`.
     */
    public fun Expression?.visit() {
        codeGenerator.run {
            visit()
        }
    }

    /**
     * Passes all nodes within the list through the code generator if it is not `null`.
     */
    public fun List<Node>?.visit() {
        codeGenerator.run {
            visit()
        }
    }
}
