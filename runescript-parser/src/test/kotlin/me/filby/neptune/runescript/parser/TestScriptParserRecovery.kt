package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.ast.ScriptFile
import org.antlr.v4.runtime.BaseErrorListener
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestScriptParserRecovery {
    @Test
    fun testSyntaxErrorReturnsNullByDefault() {
        val source = "[proc,a]\nif (\$x = ) { mes(\"z\"); }\n"

        assertNull(ScriptParser.createScriptFile(source, SILENT))
    }

    @Test
    fun testRecoverDropsOnlyTheBrokenStatement() {
        // the if statement is missing its right-hand side, so it can't be built
        val source = "[proc,a]\nmes(\"one\");\nif (\$x = ) { mes(\"z\"); }\nmes(\"two\");\n[proc,b]\nreturn;\n"

        val file = ScriptParser.createScriptFile(source, SILENT, recover = true)

        assertEquals(
            listOf("a" to listOf("ExpressionStatement", "ExpressionStatement"), "b" to listOf("ReturnStatement")),
            file.outline(),
        )
    }

    @Test
    fun testRecoverDropsAScriptWithABrokenName() {
        val source = "[proc,]\nmes(\"x\");\n[proc,b]\nreturn;\n"

        val file = ScriptParser.createScriptFile(source, SILENT, recover = true)

        assertEquals(listOf("b" to listOf("ReturnStatement")), file.outline())
    }

    @Test
    fun testRecoverSingleScript() {
        val source = "[proc,a]\nmes(\"one\");\nif (\$x = ) { mes(\"z\"); }\n"

        val script = ScriptParser.createScript(source, SILENT, recover = true)

        assertEquals(listOf("ExpressionStatement"), script?.statements?.map { it::class.simpleName })
    }

    /**
     * Returns each script's name with the kinds of its statements.
     */
    private fun ScriptFile?.outline() =
        this?.scripts?.map { script -> script.nameString to script.statements.map { it::class.simpleName } }

    private companion object {
        val SILENT = object : BaseErrorListener() {}
    }
}
