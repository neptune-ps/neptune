package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.antlr.RuneScriptParser
import me.filby.neptune.runescript.antlr.RuneScriptParserBaseVisitor
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.ScriptFile
import me.filby.neptune.runescript.ast.expr.BinaryExpression
import me.filby.neptune.runescript.ast.expr.BooleanLiteral
import me.filby.neptune.runescript.ast.expr.CalcExpression
import me.filby.neptune.runescript.ast.expr.CallExpression
import me.filby.neptune.runescript.ast.expr.CharacterLiteral
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.IntegerLiteral
import me.filby.neptune.runescript.ast.expr.NullLiteral
import me.filby.neptune.runescript.ast.statement.ExpressionStatement
import me.filby.neptune.runescript.ast.statement.Statement

public class AstBuilder : RuneScriptParserBaseVisitor<Node>() {

    override fun visitScriptFile(ctx: RuneScriptParser.ScriptFileContext): ScriptFile {
        return ScriptFile(ctx.script().map { visit(it) as Script })
    }

    override fun visitScript(ctx: RuneScriptParser.ScriptContext): Script {
        return Script(
            trigger = visit(ctx.trigger) as Identifier,
            name = visit(ctx.name) as Identifier,
            statements = ctx.statement().map { visit(it) as Statement }
        )
    }

    override fun visitExpressionStatement(ctx: RuneScriptParser.ExpressionStatementContext): ExpressionStatement {
        return ExpressionStatement(visit(ctx.expression()) as Expression)
    }

    override fun visitBinaryExpression(ctx: RuneScriptParser.BinaryExpressionContext): Node {
        return BinaryExpression(
            left = visit(ctx.expression(0)) as Expression,
            operator = ctx.op.text,
            right = visit(ctx.expression(1)) as Expression
        )
    }

    override fun visitCalcExpression(ctx: RuneScriptParser.CalcExpressionContext): Node {
        return CalcExpression(visit(ctx.expression()) as Expression)
    }

    override fun visitCallExpression(ctx: RuneScriptParser.CallExpressionContext): Node {
        return CallExpression(
            name = visit(ctx.identifier()) as Identifier,
            arguments = ctx.expressionList().visit()
        )
    }

    override fun visitIntegerLiteral(ctx: RuneScriptParser.IntegerLiteralContext): IntegerLiteral {
        val text = ctx.text
        if (text.length > 1 && text[0] == '0' && (text[1] == 'x' || text[1] == 'X')) {
            // hex, trim 0x
            return IntegerLiteral(text.substring(2).toLong(16).toInt())
        }
        return IntegerLiteral(text.toInt())
    }

    override fun visitBooleanLiteral(ctx: RuneScriptParser.BooleanLiteralContext): BooleanLiteral {
        return BooleanLiteral(ctx.text.toBoolean())
    }

    override fun visitCharacterLiteral(ctx: RuneScriptParser.CharacterLiteralContext): Node {
        // TODO support for escaping
        return CharacterLiteral(ctx.text[1])
    }

    override fun visitNullLiteral(ctx: RuneScriptParser.NullLiteralContext?): NullLiteral {
        return NullLiteral
    }

    override fun visitIdentifier(ctx: RuneScriptParser.IdentifierContext): Identifier {
        return Identifier(ctx.text)
    }

    /**
     * Helper that converts an [RuneScriptParser.ExpressionListContext] to a [List] of [Expression]s.
     *
     * @return A list of expression if defined. If there are no expressions the list will be empty.
     */
    private fun RuneScriptParser.ExpressionListContext?.visit(): List<Expression> {
        val expressions = this?.expression() ?: return emptyList()
        return expressions.map { visit(it) as Expression }
    }

}
