package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.ast.Identifier
import me.filby.neptune.runescript.ast.Script
import kotlin.test.Test
import kotlin.test.assertEquals

class TestRuneScriptParser {

    @Test
    fun testScript() {
        val script = ScriptParser.createScript("[opheld1,abyssal_whip]")
        val expected = Script(Identifier("opheld1"), Identifier("abyssal_whip"))
        assertEquals(expected, script)
    }

}
