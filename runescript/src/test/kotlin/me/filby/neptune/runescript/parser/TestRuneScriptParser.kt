package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.antlr.RuneScriptParser
import me.filby.neptune.runescript.ast.Identifier
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.expr.BooleanLiteral
import me.filby.neptune.runescript.ast.expr.CharacterLiteral
import me.filby.neptune.runescript.ast.expr.IntegerLiteral
import me.filby.neptune.runescript.ast.expr.NullLiteral
import me.filby.neptune.runescript.parser.ScriptParser.invokeParser
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class TestRuneScriptParser {

    @Test
    fun testScript() {
        val script = ScriptParser.createScript("[opheld1,abyssal_whip]")
        val expected = Script(Identifier("opheld1"), Identifier("abyssal_whip"))
        assertEquals(expected, script)
    }

    @Test
    fun testIntegerLiteral() {
        assertEquals(IntegerLiteral(1337),
            invokeParser("1337", RuneScriptParser::literal))
        assertThrows<ParsingException> { invokeParser("1337_", RuneScriptParser::literal) }

        assertEquals(IntegerLiteral(0xFFFFFF),
            invokeParser("0xFFFFFF", RuneScriptParser::literal))
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
