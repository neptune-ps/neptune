package me.filby.neptune.clientscript.compiler.util

import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.expr.BasicStringPart
import me.filby.neptune.runescript.ast.expr.BinaryExpression
import me.filby.neptune.runescript.ast.expr.CalcExpression
import me.filby.neptune.runescript.ast.expr.CharacterLiteral
import me.filby.neptune.runescript.ast.expr.CommandCallExpression
import me.filby.neptune.runescript.ast.expr.ConstantVariableExpression
import me.filby.neptune.runescript.ast.expr.ExpressionStringPart
import me.filby.neptune.runescript.ast.expr.GameVariableExpression
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.expr.JoinedStringExpression
import me.filby.neptune.runescript.ast.expr.Literal
import me.filby.neptune.runescript.ast.expr.LocalVariableExpression
import me.filby.neptune.runescript.ast.expr.NullLiteral
import me.filby.neptune.runescript.ast.expr.ProcCallExpression
import me.filby.neptune.runescript.ast.expr.StringLiteral
import me.filby.neptune.runescript.ast.expr.StringPart

class ExpressionGenerator : AstVisitor<String> {
    override fun visitBinaryExpression(binaryExpression: BinaryExpression): String =
        "${binaryExpression.left.visit()} ${binaryExpression.operator.text} ${binaryExpression.right.visit()}"

    override fun visitCalcExpression(calcExpression: CalcExpression): String =
        "calc(${calcExpression.expression.visit()})"

    override fun visitCommandCallExpression(commandCallExpression: CommandCallExpression) = buildString {
        append(commandCallExpression.name.visit())
        if (commandCallExpression.arguments.isNotEmpty()) {
            append('(')
            append(commandCallExpression.arguments.joinToString { it.visit() })
            append(')')
        }
    }

    override fun visitProcCallExpression(procCallExpression: ProcCallExpression) = buildString {
        append("~")
        append(procCallExpression.name.visit())
        if (procCallExpression.arguments.isNotEmpty()) {
            append('(')
            append(procCallExpression.arguments.joinToString { it.visit() })
            append(')')
        }
    }

    override fun visitLocalVariableExpression(localVariableExpression: LocalVariableExpression): String =
        "${'$'}${localVariableExpression.name.visit()}"

    override fun visitGameVariableExpression(gameVariableExpression: GameVariableExpression): String =
        "%${gameVariableExpression.name.visit()}"

    override fun visitConstantVariableExpression(constantVariableExpression: ConstantVariableExpression): String =
        "^${constantVariableExpression.name.visit()}"

    override fun visitCharacterLiteral(characterLiteral: CharacterLiteral): String = "'${characterLiteral.value}'"

    override fun visitNullLiteral(nullLiteral: NullLiteral): String = "null"

    override fun visitStringLiteral(stringLiteral: StringLiteral): String = "\"${stringLiteral.value}\""

    override fun visitLiteral(literal: Literal<*>): String = literal.value.toString()

    override fun visitJoinedStringExpression(joinedStringExpression: JoinedStringExpression) = buildString {
        append('"')
        for (part in joinedStringExpression.parts) {
            append(part.visit())
        }
        append('"')
    }

    override fun visitJoinedStringPart(stringPart: StringPart): String = when (stringPart) {
        is BasicStringPart -> stringPart.value
        is ExpressionStringPart -> "<${stringPart.expression.visit()}>"
        else -> error("Unsupported StringPart: $stringPart")
    }

    override fun visitIdentifier(identifier: Identifier): String = identifier.text

    private fun Node.visit(): String = accept(this@ExpressionGenerator)
}
