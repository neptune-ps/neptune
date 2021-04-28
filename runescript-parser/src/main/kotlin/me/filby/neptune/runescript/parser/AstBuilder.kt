package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.antlr.RuneScriptParser.BinaryExpressionContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.BooleanLiteralContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.CalcExpressionContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.CallExpressionContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.CharacterLiteralContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ExpressionListContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ExpressionStatementContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.IdentifierContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.IntegerLiteralContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.NullLiteralContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ScriptContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ScriptFileContext
import me.filby.neptune.runescript.antlr.RuneScriptParserBaseVisitor
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.ScriptFile
import me.filby.neptune.runescript.ast.expr.BinaryExpression
import me.filby.neptune.runescript.ast.expr.BooleanLiteral
import me.filby.neptune.runescript.ast.expr.CalcExpression
import me.filby.neptune.runescript.ast.expr.CallExpression
import me.filby.neptune.runescript.ast.expr.CharacterLiteral
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.expr.IntegerLiteral
import me.filby.neptune.runescript.ast.expr.NullLiteral
import me.filby.neptune.runescript.ast.statement.ExpressionStatement
import me.filby.neptune.runescript.ast.statement.Statement

public class AstBuilder : RuneScriptParserBaseVisitor<Node>() {

    override fun visitScriptFile(ctx: ScriptFileContext): ScriptFile {
        return ScriptFile(ctx.script().map { visit(it) as Script })
    }

    override fun visitScript(ctx: ScriptContext): Script {
        return Script(
            trigger = visit(ctx.trigger) as Identifier,
            name = visit(ctx.name) as Identifier,
            statements = ctx.statement().map { visit(it) as Statement }
        )
    }

    override fun visitExpressionStatement(ctx: ExpressionStatementContext): ExpressionStatement {
        return ExpressionStatement(visit(ctx.expression()) as Expression)
    }

    override fun visitBinaryExpression(ctx: BinaryExpressionContext): Node {
        return BinaryExpression(
            left = visit(ctx.expression(0)) as Expression,
            operator = ctx.op.text,
            right = visit(ctx.expression(1)) as Expression
        )
    }

    override fun visitCalcExpression(ctx: CalcExpressionContext): Node {
        return CalcExpression(visit(ctx.expression()) as Expression)
    }

    override fun visitCallExpression(ctx: CallExpressionContext): Node {
        return CallExpression(
            name = visit(ctx.identifier()) as Identifier,
            arguments = ctx.expressionList().visit()
        )
    }

    override fun visitIntegerLiteral(ctx: IntegerLiteralContext): IntegerLiteral {
        val text = ctx.text
        if (text.length > 1 && text[0] == '0' && (text[1] == 'x' || text[1] == 'X')) {
            // hex, trim 0x
            return IntegerLiteral(text.substring(2).toLong(16).toInt())
        }
        return IntegerLiteral(text.toInt())
    }

    override fun visitBooleanLiteral(ctx: BooleanLiteralContext): BooleanLiteral {
        return BooleanLiteral(ctx.text.toBoolean())
    }

    override fun visitCharacterLiteral(ctx: CharacterLiteralContext): Node {
        // TODO support for escaping
        return CharacterLiteral(ctx.text[1])
    }

    override fun visitNullLiteral(ctx: NullLiteralContext?): NullLiteral {
        return NullLiteral
    }

    override fun visitIdentifier(ctx: IdentifierContext): Identifier {
        return Identifier(ctx.text)
    }

    /**
     * Helper that converts an [ExpressionListContext] to a [List] of [Expression]s.
     *
     * @return A list of expression if defined. If there are no expressions the list will be empty.
     */
    private fun ExpressionListContext?.visit(): List<Expression> {
        val expressions = this?.expression() ?: return emptyList()
        return expressions.map { visit(it) as Expression }
    }

}
