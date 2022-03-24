package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.antlr.RuneScriptParser.AdvancedIdentifierContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ArrayDeclarationStatementContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.AssignmentStatementContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.BinaryExpressionContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.BlockStatementContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.BooleanLiteralContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.CalcExpressionContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.CharacterLiteralContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.CommandCallExpressionContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ConstantVariableContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.DeclarationStatementContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.EmptyStatementContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ExpressionListContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ExpressionStatementContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.GameVariableContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.IdentifierContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.IfStatementContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.IntegerLiteralContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.JoinedStringContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.JumpCallExpressionContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.LocalArrayVariableContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.LocalVariableContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.NullLiteralContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ParameterContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ParenthesisContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ParenthesizedExpressionContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ProcCallExpressionContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ReturnStatementContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ScriptContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.ScriptFileContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.StringExpressionContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.StringLiteralContentContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.StringLiteralContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.SwitchCaseContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.SwitchStatementContext
import me.filby.neptune.runescript.antlr.RuneScriptParser.WhileStatementContext
import me.filby.neptune.runescript.antlr.RuneScriptParserBaseVisitor
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.NodeSourceLocation
import me.filby.neptune.runescript.ast.Parameter
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.ScriptFile
import me.filby.neptune.runescript.ast.Token
import me.filby.neptune.runescript.ast.expr.BinaryExpression
import me.filby.neptune.runescript.ast.expr.BooleanLiteral
import me.filby.neptune.runescript.ast.expr.CalcExpression
import me.filby.neptune.runescript.ast.expr.CharacterLiteral
import me.filby.neptune.runescript.ast.expr.CommandCallExpression
import me.filby.neptune.runescript.ast.expr.ConstantVariableExpression
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.GameVariableExpression
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.expr.IntegerLiteral
import me.filby.neptune.runescript.ast.expr.JoinedStringExpression
import me.filby.neptune.runescript.ast.expr.JumpCallExpression
import me.filby.neptune.runescript.ast.expr.LocalVariableExpression
import me.filby.neptune.runescript.ast.expr.NullLiteral
import me.filby.neptune.runescript.ast.expr.ParenthesizedExpression
import me.filby.neptune.runescript.ast.expr.ProcCallExpression
import me.filby.neptune.runescript.ast.expr.StringLiteral
import me.filby.neptune.runescript.ast.statement.ArrayDeclarationStatement
import me.filby.neptune.runescript.ast.statement.AssignmentStatement
import me.filby.neptune.runescript.ast.statement.BlockStatement
import me.filby.neptune.runescript.ast.statement.DeclarationStatement
import me.filby.neptune.runescript.ast.statement.EmptyStatement
import me.filby.neptune.runescript.ast.statement.ExpressionStatement
import me.filby.neptune.runescript.ast.statement.IfStatement
import me.filby.neptune.runescript.ast.statement.ReturnStatement
import me.filby.neptune.runescript.ast.statement.SwitchCase
import me.filby.neptune.runescript.ast.statement.SwitchStatement
import me.filby.neptune.runescript.ast.statement.WhileStatement
import org.antlr.v4.runtime.ParserRuleContext

/**
 * A visitor that converts an antlr parse tree into an [AST](https://en.wikipedia.org/wiki/Abstract_syntax_tree). See
 * [Node] implementations for all possible pieces of the tree.
 */
public class AstBuilder(private val source: String) : RuneScriptParserBaseVisitor<Node>() {
    override fun visitScriptFile(ctx: ScriptFileContext): Node {
        return ScriptFile(ctx.location, ctx.script().map { it.visit() })
    }

    override fun visitScript(ctx: ScriptContext): Node {
        val returns = ctx.typeList()?.IDENTIFIER()?.map { it.symbol.toAstToken() }
        return Script(
            source = ctx.location,
            trigger = ctx.trigger.visit(),
            name = ctx.name.visit(),
            parameters = ctx.parameterList()?.parameter()?.map { it.visit() },
            returnTokens = returns,
            statements = ctx.statement().map { it.visit() }
        )
    }

    override fun visitParameter(ctx: ParameterContext): Node {
        return Parameter(
            source = ctx.location,
            typeToken = ctx.type.toAstToken(),
            name = ctx.advancedIdentifier().visit()
        )
    }

    override fun visitBlockStatement(ctx: BlockStatementContext): Node {
        return BlockStatement(ctx.location, ctx.statement().map { it.visit() })
    }

    override fun visitReturnStatement(ctx: ReturnStatementContext): Node {
        return ReturnStatement(ctx.location, ctx.expressionList().visit())
    }

    override fun visitIfStatement(ctx: IfStatementContext): Node {
        return IfStatement(
            source = ctx.location,
            condition = ctx.parenthesis().visit(),
            thenStatement = ctx.statement(0).visit(),
            elseStatement = ctx.statement(1)?.visit()
        )
    }

    override fun visitWhileStatement(ctx: WhileStatementContext): Node {
        return WhileStatement(
            source = ctx.location,
            condition = ctx.parenthesis().visit(),
            thenStatement = ctx.statement().visit()
        )
    }

    override fun visitSwitchStatement(ctx: SwitchStatementContext): Node {
        return SwitchStatement(
            source = ctx.location,
            typeToken = ctx.SWITCH_TYPE().symbol.toAstToken(),
            condition = ctx.parenthesis().visit(),
            cases = ctx.switchCase().map { it.visit() }
        )
    }

    override fun visitSwitchCase(ctx: SwitchCaseContext): Node {
        return SwitchCase(
            source = ctx.location,
            keys = ctx.expressionList()?.visit() ?: emptyList(),
            statements = ctx.statement()?.map { it.visit() } ?: emptyList()
        )
    }

    override fun visitDeclarationStatement(ctx: DeclarationStatementContext): Node {
        return DeclarationStatement(
            source = ctx.location,
            typeToken = ctx.DEF_TYPE().symbol.toAstToken(),
            name = ctx.advancedIdentifier().visit(),
            initializer = ctx.expression()?.visit()
        )
    }

    override fun visitArrayDeclarationStatement(ctx: ArrayDeclarationStatementContext): Node {
        return ArrayDeclarationStatement(
            source = ctx.location,
            typeToken = ctx.DEF_TYPE().symbol.toAstToken(),
            name = ctx.advancedIdentifier().visit(),
            initializer = ctx.parenthesis().visit()
        )
    }

    override fun visitAssignmentStatement(ctx: AssignmentStatementContext): Node {
        return AssignmentStatement(
            source = ctx.location,
            vars = ctx.assignableVariableList().assignableVariable().map { it.visit() },
            expressions = ctx.expressionList().visit()
        )
    }

    override fun visitExpressionStatement(ctx: ExpressionStatementContext): Node {
        return ExpressionStatement(ctx.location, ctx.expression().visit())
    }

    override fun visitEmptyStatement(ctx: EmptyStatementContext): Node {
        return EmptyStatement(ctx.location)
    }

    override fun visitParenthesizedExpression(ctx: ParenthesizedExpressionContext): Node {
        return ParenthesizedExpression(ctx.location, ctx.parenthesis().visit())
    }

    override fun visitBinaryExpression(ctx: BinaryExpressionContext): Node {
        return BinaryExpression(
            source = ctx.location,
            left = ctx.expression(0).visit(),
            operator = ctx.op.toAstToken(),
            right = ctx.expression(1).visit()
        )
    }

    override fun visitCalcExpression(ctx: CalcExpressionContext): Node {
        return CalcExpression(ctx.location, ctx.parenthesis().visit())
    }

    override fun visitCommandCallExpression(ctx: CommandCallExpressionContext): Node {
        return CommandCallExpression(
            source = ctx.location,
            name = ctx.identifier().visit(),
            arguments = ctx.expressionList().visit()
        )
    }

    override fun visitProcCallExpression(ctx: ProcCallExpressionContext): Node {
        return ProcCallExpression(
            source = ctx.location,
            name = ctx.identifier().visit(),
            arguments = ctx.expressionList().visit()
        )
    }

    override fun visitJumpCallExpression(ctx: JumpCallExpressionContext): Node {
        return JumpCallExpression(
            source = ctx.location,
            name = ctx.identifier().visit(),
            arguments = ctx.expressionList().visit()
        )
    }

    override fun visitLocalVariable(ctx: LocalVariableContext): Node {
        return LocalVariableExpression(
            source = ctx.location,
            name = ctx.advancedIdentifier().visit(),
            index = null
        )
    }

    override fun visitLocalArrayVariable(ctx: LocalArrayVariableContext): Node {
        return LocalVariableExpression(
            source = ctx.location,
            name = ctx.advancedIdentifier().visit(),
            index = ctx.parenthesis().visit()
        )
    }

    override fun visitGameVariable(ctx: GameVariableContext): Node {
        return GameVariableExpression(ctx.location, ctx.advancedIdentifier().visit())
    }

    override fun visitConstantVariable(ctx: ConstantVariableContext): Node {
        return ConstantVariableExpression(ctx.location, ctx.advancedIdentifier().visit())
    }

    override fun visitIntegerLiteral(ctx: IntegerLiteralContext): Node {
        val text = ctx.text
        if (text.length > 1 && text[0] == '0' && (text[1] == 'x' || text[1] == 'X')) {
            // hex, trim 0x
            return IntegerLiteral(ctx.location, text.substring(2).toLong(16).toInt())
        }
        return IntegerLiteral(ctx.location, text.toInt())
    }

    override fun visitBooleanLiteral(ctx: BooleanLiteralContext): Node {
        return BooleanLiteral(ctx.location, ctx.text.toBoolean())
    }

    override fun visitCharacterLiteral(ctx: CharacterLiteralContext): Node {
        val cleaned = ctx.text.substring(1, ctx.text.length - 1).unescape()
        if (cleaned.length != 1) {
            error("invalid character literal: text=${ctx.text}, cleaned=$cleaned")
        }
        return CharacterLiteral(ctx.location, cleaned[0])
    }

    override fun visitStringLiteral(ctx: StringLiteralContext): Node {
        // trim off the quotes and remove escape sequences
        return StringLiteral(ctx.location, ctx.text.substring(1, ctx.text.length - 1).unescape())
    }

    override fun visitNullLiteral(ctx: NullLiteralContext): Node {
        return NullLiteral(ctx.location)
    }

    override fun visitJoinedString(ctx: JoinedStringContext): Node {
        val parts = mutableListOf<Expression>()

        for (child in ctx.children) {
            when (child) {
                is StringLiteralContentContext -> {
                    // create a new literal since the rule only allows valid string parts
                    parts += StringLiteral(child.location, child.text.unescape())
                }
                is StringExpressionContext -> {
                    // visit the inner expression
                    parts += child.expression().visit<Expression>()
                }
                else -> {
                    // noop, any other rules are things we don't care about (e.g. double quotes)
                }
            }
        }

        return JoinedStringExpression(ctx.location, parts.toList())
    }

    override fun visitIdentifier(ctx: IdentifierContext): Node {
        return Identifier(ctx.location, ctx.text)
    }

    override fun visitAdvancedIdentifier(ctx: AdvancedIdentifierContext): Node {
        return Identifier(ctx.location, ctx.text)
    }

    /**
     * The source location of the [ParserRuleContext].
     */
    private inline val ParserRuleContext.location
        get() = NodeSourceLocation(source, start.line, start.charPositionInLine + 1)

    /**
     * The source location of the [Token].
     */
    private inline val org.antlr.v4.runtime.Token.location
        get() = NodeSourceLocation(source, line, charPositionInLine + 1)

    /**
     * Converts a [org.antlr.v4.runtime.Token] into a [Token] AST node.
     */
    private fun org.antlr.v4.runtime.Token.toAstToken(): Token {
        return Token(location, text)
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

    /**
     * Replaces escape sequences in a string.
     *
     * @return The string with all escape sequences replaced.
     */
    private fun String.unescape(): String {
        val builder = StringBuilder(length)
        var i = 0
        while (i < length) {
            val curr = this[i]
            if (curr == '\\') {
                // start of escape sequence, so fetch the next character
                val next = if (i == length - 1) '\\' else this[i + 1]
                builder.append(
                    when (next) {
                        '\\', '\'', '"', '<' -> next
                        else -> error("unsupported escape sequence: \\$next")
                    }
                )
                i++
            } else {
                builder.append(curr)
            }
            i++
        }
        return builder.toString()
    }

    private companion object {
        /**
         * The prefix used when specifying a switch type.
         */
        private const val SWITCH_TYPE_PREFIX = "switch_"

        /**
         * The prefix used when specifying the type of a local variable.
         */
        private const val DEF_TYPE_PREFIX = "def_"

        /**
         * The suffix used for specifying a parameter that is an array.
         */
        private const val TYPE_ARRAY_SUFFIX = "array"
    }
}
