package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.antlr.RuneScriptParser
import me.filby.neptune.runescript.antlr.RuneScriptParserBaseVisitor
import me.filby.neptune.runescript.ast.Identifier
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.ScriptFile
import me.filby.neptune.runescript.ast.expr.BooleanLiteral
import me.filby.neptune.runescript.ast.expr.IntegerLiteral

public class AstBuilder : RuneScriptParserBaseVisitor<Node>() {

    override fun visitScriptFile(ctx: RuneScriptParser.ScriptFileContext): ScriptFile {
        return ScriptFile(ctx.script().map { visit(it) as Script })
    }

    override fun visitScript(ctx: RuneScriptParser.ScriptContext): Script {
        return Script(visitIdentifier(ctx.trigger), visitIdentifier(ctx.name))
    }

    override fun visitIntegerLiteral(ctx: RuneScriptParser.IntegerLiteralContext): IntegerLiteral {
        return IntegerLiteral(ctx.text.toInt())
    }

    override fun visitBooleanLiteral(ctx: RuneScriptParser.BooleanLiteralContext): BooleanLiteral {
        return BooleanLiteral(ctx.text.toBoolean())
    }

    override fun visitIdentifier(ctx: RuneScriptParser.IdentifierContext): Identifier {
        return Identifier(ctx.text)
    }

}
