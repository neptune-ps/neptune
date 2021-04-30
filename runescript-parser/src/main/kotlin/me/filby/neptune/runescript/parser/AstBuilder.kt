package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.antlr.RuneScriptParser.BinaryExpressionContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.BooleanLiteralContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.CalcExpressionContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.CallExpressionContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.CharacterLiteralContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ConstantVariableContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ExpressionListContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ExpressionStatementContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.GameVariableContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.IdentifierContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.IntegerLiteralContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.LocalVariableContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.NullLiteralContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ParenthesisContext
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
import me.filby.neptune.runescript.ast.expr.ConstantVariableExpression
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.GameVariableExpression
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.expr.IntegerLiteral
import me.filby.neptune.runescript.ast.expr.LocalVariableExpression
import me.filby.neptune.runescript.ast.expr.NullLiteral
import me.filby.neptune.runescript.ast.statement.ExpressionStatement
import org.antlr.v4.runtime.ParserRuleContext

public class AstBuilder : RuneScriptParserBaseVisitor<Node>() {

    override fun visitScriptFile(ctx: ScriptFileContext): Node {
        return ScriptFile(ctx.script().map { it.visit() })
    }

    override fun visitScript(ctx: ScriptContext): Node {
        return Script(
            trigger = ctx.trigger.visit(),
            name = ctx.name.visit(),
            statements = ctx.statement().map { it.visit() }
        )
    }

    override fun visitExpressionStatement(ctx: ExpressionStatementContext): Node {
        return ExpressionStatement(ctx.expression().visit())
    }

    override fun visitBinaryExpression(ctx: BinaryExpressionContext): Node {
        return BinaryExpression(
            left = ctx.expression(0).visit(),
            operator = ctx.op.text,
            right = ctx.expression(1).visit()
        )
    }

    override fun visitCalcExpression(ctx: CalcExpressionContext): Node {
        return CalcExpression(ctx.parenthesis().visit())
    }

    override fun visitCallExpression(ctx: CallExpressionContext): Node {
        return CallExpression(
            name = ctx.identifier().visit(),
            arguments = ctx.expressionList().visit()
        )
    }

    override fun visitLocalVariable(ctx: LocalVariableContext): Node {
        return LocalVariableExpression(
            name = ctx.identifier().visit(),
            index = ctx.parenthesis()?.visit()
        )
    }

    override fun visitGameVariable(ctx: GameVariableContext): Node {
        return GameVariableExpression(ctx.identifier().visit())
    }

    override fun visitConstantVariable(ctx: ConstantVariableContext): Node {
        return ConstantVariableExpression(ctx.identifier().visit())
    }

    override fun visitIntegerLiteral(ctx: IntegerLiteralContext): Node {
        val text = ctx.text
        if (text.length > 1 && text[0] == '0' && (text[1] == 'x' || text[1] == 'X')) {
            // hex, trim 0x
            return IntegerLiteral(text.substring(2).toLong(16).toInt())
        }
        return IntegerLiteral(text.toInt())
    }

    override fun visitBooleanLiteral(ctx: BooleanLiteralContext): Node {
        return BooleanLiteral(ctx.text.toBoolean())
    }

    override fun visitCharacterLiteral(ctx: CharacterLiteralContext): Node {
        // TODO support for escaping
        return CharacterLiteral(ctx.text[1])
    }

    override fun visitNullLiteral(ctx: NullLiteralContext?): Node {
        return NullLiteral
    }

    override fun visitIdentifier(ctx: IdentifierContext): Node {
        return Identifier(ctx.text)
    }

    /**
     * Helper that calls [RuneScriptParserBaseVisitor.visit] on the current context.
     *
     * @return The [Node] casted to [T].
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : Node> ParserRuleContext.visit(): T {
        return visit(this) as T
    }

    /**
     * Helper that converts an [ExpressionListContext] to a [List] of [Expression]s.
     *
     * @return A list of expression if defined. If there are no expressions the list will be empty.
     */
    private fun ExpressionListContext?.visit(): List<Expression> {
        val expressions = this?.expression() ?: return emptyList()
        return expressions.map { it.visit() }
    }

    /**
     * Helper that converts an [ParenthesisContext] to an [Expression].
     *
     * @return The expression within `(` and `)`.
     */
    private fun ParenthesisContext.visit(): Expression {
        return expression().visit()
    }

}
