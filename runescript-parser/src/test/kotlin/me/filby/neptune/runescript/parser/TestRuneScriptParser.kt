package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.antlr.RuneScriptParser
import me.filby.neptune.runescript.ast.Script
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
import me.filby.neptune.runescript.ast.statement.BlockStatement
import me.filby.neptune.runescript.ast.statement.ExpressionStatement
import me.filby.neptune.runescript.ast.statement.ReturnStatement
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
    fun testBlockStatement() {
        val statement = invokeParser("{}", RuneScriptParser::statement)
        val expected = BlockStatement(emptyList())
        assertEquals(expected, statement)
    }

    @Test
    fun testReturnStatement() {
        val one = IntegerLiteral(1)
        val statement = invokeParser("return(1);", RuneScriptParser::statement)
        val expected = ReturnStatement(listOf(one))
        assertEquals(expected, statement)
    }

    @Test
    fun testExpressionStatement() {
        val statement = invokeParser("calc(1 + 1);", RuneScriptParser::statement)
        val expected = ExpressionStatement(CalcExpression(BinaryExpression(IntegerLiteral(1), "+", IntegerLiteral(1))))
        assertEquals(expected, statement)
    }

    @Test
    fun testParenthesizedExpression() {
        // calc((1 + 1) * 1)
        val addition = ParenthesizedExpression(BinaryExpression(IntegerLiteral(1), "+", IntegerLiteral(1)))
        val combined = BinaryExpression(addition, "*", IntegerLiteral(1))
        val calc = CalcExpression(combined)
        assertEquals(calc,
            invokeParser("calc((1 + 1) * 1)", RuneScriptParser::expression))
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

        // calc(1 | 1 & 1) should parse as calc(1 | (1 & 1))
        assertEquals(CalcExpression(BinaryExpression(one, "|", BinaryExpression(one, "&", one))),
            invokeParser("calc(1 | 1 & 1)", RuneScriptParser::expression))

        // verify addition only works inside of calc()
        assertThrows<ParsingException> { invokeParser("1 + 1", RuneScriptParser::expression) }
    }

    @Test
    fun testCommandCallExpression() {
        assertEquals(CommandCallExpression(Identifier("clientclock"), emptyList()),
            invokeParser("clientclock()", RuneScriptParser::expression))

        assertEquals(CommandCallExpression(Identifier(".npc_find"), emptyList()),
            invokeParser(".npc_find()", RuneScriptParser::expression))

        // TODO test for call without arguments? currently will parse as a normal identifier
    }

    @Test
    fun testProcCallExpression() {
        // call with no arguments
        assertEquals(ProcCallExpression(Identifier("testing"), emptyList()),
            invokeParser("~testing", RuneScriptParser::expression))

        // call with argument
        assertEquals(ProcCallExpression(Identifier("testing"), listOf(IntegerLiteral(1))),
            invokeParser("~testing(1)", RuneScriptParser::expression))
    }

    @Test
    fun testJumpCallExpression() {
        // call with no arguments
        assertEquals(JumpCallExpression(Identifier("testing"), emptyList()),
            invokeParser("@testing", RuneScriptParser::expression))

        // call with argument
        assertEquals(JumpCallExpression(Identifier("testing"), listOf(IntegerLiteral(1))),
            invokeParser("@testing(1)", RuneScriptParser::expression))
    }

    @Test
    fun testLocalVariableExpression() {
        // $var
        assertEquals(LocalVariableExpression(Identifier("var")),
            invokeParser("${'$'}var", RuneScriptParser::expression))

        // $var(1)
        assertEquals(LocalVariableExpression(Identifier("var"), IntegerLiteral(1)),
            invokeParser("${'$'}var(1)", RuneScriptParser::expression))

        // %var
        assertEquals(GameVariableExpression(Identifier("var")),
            invokeParser("%var", RuneScriptParser::expression))

        // ^var
        assertEquals(ConstantVariableExpression(Identifier("var")),
            invokeParser("^var", RuneScriptParser::expression))
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

        // escaped '
        assertEquals(CharacterLiteral('\''),
            invokeParser("'\\''", RuneScriptParser::literal))

        assertThrows<ParsingException> { invokeParser("'test'", RuneScriptParser::literal) }
    }

    @Test
    fun testStringLiteral() {
        // basic string test
        assertEquals(StringLiteral("this is a test"),
            invokeParser("\"this is a test\"", RuneScriptParser::literal))

        // test escaping "\< \" \\"
        assertEquals(StringLiteral("< \" \\"),
            invokeParser("\"\\< \\\" \\\\\"", RuneScriptParser::literal))

        // test tags
        assertEquals(StringLiteral("<col=ffffff><br> testing"),
            invokeParser("\"<col=ffffff><br> testing\"", RuneScriptParser::literal))
    }

    @Test
    fun testNullLiteral() {
        assertEquals(NullLiteral,
            invokeParser("null", RuneScriptParser::literal))
    }

    @Test
    fun testJoinedStringExpression() {
        // normal
        val part1 = StringLiteral("1 + 1 = ")
        val part2 = CalcExpression(BinaryExpression(IntegerLiteral(1), "+", IntegerLiteral(1)))
        val part3 = StringLiteral(".")
        val joined = JoinedStringExpression(listOf(part1, part2, part3))
        assertEquals(joined,
            invokeParser("\"1 + 1 = <calc(1 + 1)>.\"", RuneScriptParser::expression))

        // escaping
        val part4 = StringLiteral("\\")
        val joined2 = JoinedStringExpression(listOf(part1, part2, part3, part4))
        assertEquals(joined2,
            invokeParser("\"1 + 1 = <calc(1 + 1)>.\\\\\"", RuneScriptParser::expression))

        // tags
        val exprs: List<Expression> = listOf(StringLiteral("<col=ffffff>"), IntegerLiteral(1), StringLiteral("</col>"))
        assertEquals(JoinedStringExpression(exprs),
            invokeParser("\"<col=ffffff><1></col>\"", RuneScriptParser::expression))
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

        // identifier with a .
        assertEquals(Identifier(".abyssal_whip"),
            invokeParser(".abyssal_whip", RuneScriptParser::identifier))

        // identifier that is a keyword
        assertEquals(Identifier("true"),
            invokeParser("true", RuneScriptParser::identifier))
    }

}
