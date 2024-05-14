package me.filby.neptune.clientscript.compiler.command.debug

import me.filby.neptune.clientscript.compiler.util.ExpressionGenerator
import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.configuration.command.CodeGeneratorContext
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.symbol.SymbolType
import me.filby.neptune.runescript.compiler.trigger.CommandTrigger
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type

/**
 * Developer "command" used to easily debug any expression.
 *
 * Converts `dump(expr1, ...)` into the string `"expr1=<expr1>, ..."`, converting types where needed.
 */
class DumpCommandHandler : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        if (expression.arguments.isEmpty()) {
            expression.reportError(diagnostics, DIAGNOSTIC_INVALID_SIGNATURE)
        } else {
            for ((i, arg) in expression.arguments.withIndex()) {
                checkArgument(i, MetaType.Any)

                val type = arg.type
                if (type is TupleType) {
                    arg.reportError(diagnostics, DIAGNOSTIC_TUPLE_TYPE, type.representation)
                } else if (type.baseType != BaseVarType.INTEGER && type.baseType != BaseVarType.STRING) {
                    arg.reportError(diagnostics, DIAGNOSTIC_INVALID_BASE_TYPE, type.representation)
                } else if (type == MetaType.Unit) {
                    arg.reportError(diagnostics, DIAGNOSTIC_UNIT_TYPE, type.representation)
                }
            }
        }

        expression.type = PrimitiveType.STRING
    }

    override fun CodeGeneratorContext.generateCode() {
        expression.lineInstruction()

        var parts = 0
        for ((i, arg) in expression.arguments.withIndex()) {
            val argString = arg.accept(EXPRESSION_GENERATOR)

            // put the expression string
            instruction(Opcode.PushConstantString, "$argString=")
            parts++

            // evaluate the expression
            arg.visit()

            // convert expression to string, if necessary
            typeToString(arg.type)
            parts++

            // separate each argument with a comma
            if (i != expression.arguments.size - 1) {
                instruction(Opcode.PushConstantString, ", ")
                parts++
            }
        }

        instruction(Opcode.JoinString, parts)
    }

    private fun CodeGeneratorContext.typeToString(type: Type) {
        val conversionCommandName = if (type == PrimitiveType.STRING) {
            "escape"
        } else if (type.baseType == BaseVarType.INTEGER) {
            "tostring"
        } else {
            error("Unsupported type conversion to string: $type")
        }

        val conversionSymbol = rootTable.find(SymbolType.ClientScript(CommandTrigger), conversionCommandName)
        if (conversionSymbol != null) {
            instruction(Opcode.Command, conversionSymbol)
        }
    }

    private companion object {
        const val DIAGNOSTIC_INVALID_SIGNATURE = "Type mismatch: '<unit>' was given but 'any...' was expected."
        const val DIAGNOSTIC_TUPLE_TYPE = "Unable to dump multi-value types: %s"
        const val DIAGNOSTIC_INVALID_BASE_TYPE = "Unable to debug '%s' expressions."
        const val DIAGNOSTIC_UNIT_TYPE = "Unable to debug expression with no return value."
        val EXPRESSION_GENERATOR = ExpressionGenerator()
    }
}
