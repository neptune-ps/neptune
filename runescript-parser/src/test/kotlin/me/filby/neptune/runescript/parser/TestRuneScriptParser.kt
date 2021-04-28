package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.antlr.RuneScriptParser
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.expr.BinaryExpression
import me.filby.neptune.runescript.ast.expr.BooleanLiteral
import me.filby.neptune.runescript.ast.expr.CalcExpression
import me.filby.neptune.runescript.ast.expr.CallExpression
import me.filby.neptune.runescript.ast.expr.CharacterLiteral
import me.filby.neptune.runescript.ast.expr.IntegerLiteral
import me.filby.neptune.runescript.ast.expr.NullLiteral
import me.filby.neptune.runescript.ast.statement.ExpressionStatement
import me.filby.neptune.runescript.parser.ScriptParser.invokeParser
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class TestRuneScriptParser {

    @Test
    fun testScript() {
        val script = ScriptParser.createScript("[opheld1,abyssal_whip]")
        val expected = Script(Identifier("opheld1"), Identifier("abyssal_whip"), emptyList())
        assertEquals(expected, script)
    }

    @Test
    fun testExpressionStatement() {
        val statement = invokeParser("calc(1 + 1);", RuneScriptParser::statement)
        val expected = ExpressionStatement(CalcExpression(BinaryExpression(IntegerLiteral(1), "+", IntegerLiteral(1))))
        assertEquals(expected, statement)
    }

    @Test
    fun testCalcExpression() {
        val one = IntegerLiteral(1)

        // calc(1 + 1)
        val onePlusOne = BinaryExpression(one, "+", one)
        assertEquals(CalcExpression(onePlusOne),
            invokeParser("calc(1 + 1)", RuneScriptParser::expression))

        // calc(1 - 1)
        val oneMinusOne = BinaryExpression(one, "-", one)
        assertEquals(CalcExpression(oneMinusOne),
            invokeParser("calc(1 - 1)", RuneScriptParser::expression))

        // calc(1 * 1)
        val oneTimesOne = BinaryExpression(one, "*", one)
        assertEquals(CalcExpression(oneTimesOne),
            invokeParser("calc(1 * 1)", RuneScriptParser::expression))

        // calc(1 / 1)
        val oneDivOne = BinaryExpression(one, "/", one)
        assertEquals(CalcExpression(oneDivOne),
            invokeParser("calc(1 / 1)", RuneScriptParser::expression))

        // calc(1 % 1)
        val oneModOne = BinaryExpression(one, "%", one)
        assertEquals(CalcExpression(oneModOne),
            invokeParser("calc(1 % 1)", RuneScriptParser::expression))

        // calc(1 & 1)
        val oneAndOne = BinaryExpression(one, "&", one)
        assertEquals(CalcExpression(oneAndOne),
            invokeParser("calc(1 & 1)", RuneScriptParser::expression))

        // calc(1 | 1)
        val oneOrOne = BinaryExpression(one, "|", one)
        assertEquals(CalcExpression(oneOrOne),
            invokeParser("calc(1 | 1)", RuneScriptParser::expression))

        // calc(1 + 1 - 1 * 1 / 1 % 1) should parse as calc((1 + 1) - (((1 * 1) / 1) % 1))
        val mul = BinaryExpression(one, "*", one)
        val div = BinaryExpression(mul, "/", one)
        val mod = BinaryExpression(div, "%", one)
        val add = BinaryExpression(one, "+", one)
        val sub = BinaryExpression(add, "-", mod)
        assertEquals(CalcExpression(sub),
            invokeParser("calc(1 + 1 - 1 * 1 / 1 % 1)", RuneScriptParser::expression))

        // verify addition only works inside of calc()
        assertThrows<ParsingException> { invokeParser("1 + 1", RuneScriptParser::expression) }
    }

    @Test
    fun testCallExpression() {
        assertEquals(CallExpression(Identifier("clientclock"), emptyList()),
            invokeParser("clientclock()", RuneScriptParser::expression))

        // TODO test for call without arguments? currently will parse as a normal identifier
    }

    @Test
    fun testIntegerLiteral() {
        assertEquals(IntegerLiteral(1337),
            invokeParser("1337", RuneScriptParser::literal))
        assertThrows<ParsingException> { invokeParser("1337_", RuneScriptParser::literal) }

        assertEquals(IntegerLiteral(0xFFFFFF),
            invokeParser("0xFFFFFF", RuneScriptParser::literal))
        assertEquals(IntegerLiteral(-1),
            invokeParser("0xFFFFFFFF", RuneScriptParser::literal))
        assertEquals(IntegerLiteral(0xFFFFFF),
            invokeParser("0XFFFFFF", RuneScriptParser::literal))
    }

    @Test
    fun testBooleanLiteral() {
        assertEquals(BooleanLiteral(true),
            invokeParser("true", RuneScriptParser::literal))
        assertEquals(BooleanLiteral(false),
            invokeParser("false", RuneScriptParser::literal))
    }

    @Test
    fun testCharacterLiteral() {
        assertEquals(CharacterLiteral('t'),
            invokeParser("'t'", RuneScriptParser::literal))

        assertThrows<ParsingException> { invokeParser("'test'", RuneScriptParser::literal) }
    }

    @Test
    fun testNullLiteral() {
        assertEquals(NullLiteral,
            invokeParser("null", RuneScriptParser::literal))
    }

    @Test
    fun testIdentifier() {
        // normal identifier
        assertEquals(Identifier("abyssal_whip"),
            invokeParser("abyssal_whip", RuneScriptParser::identifier))

        // normal identifier beginning with digit
        assertEquals(Identifier("100_coral_flower"),
            invokeParser("100_coral_flower", RuneScriptParser::identifier))

        // component identifier (mostly)
        assertEquals(Identifier("smithing:arrowheads"),
            invokeParser("smithing:arrowheads", RuneScriptParser::identifier))

        // identifier that looks like a hex literal
        assertEquals(Identifier("0x123"),
            invokeParser("0x123", RuneScriptParser::identifier))

        // TODO: test for keywords when used as an identifier that is prefixed with something ($, ^, and %)
    }

}
