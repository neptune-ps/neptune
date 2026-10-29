package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.ast.Node
import kotlin.test.Test
import kotlin.test.assertEquals

class TestNodeSourceLocation {
    @Test
    fun testOffsetsSpanEachNode() {
        val source = "[proc,a](int \$b)\nmes(calc(\$b + 22));\n"
        val script = ScriptParser.createScript(source) ?: error("parse error")

        val spans = mutableListOf<String>()
        fun collect(node: Node) {
            spans += "${node::class.simpleName} ${source.substring(node.source.startOffset, node.source.endOffset)}"
            node.children.forEach(::collect)
        }
        collect(script)

        assertEquals(
            listOf(
                "Script [proc,a](int \$b)\nmes(calc(\$b + 22));",
                "Identifier proc",
                "Identifier a",
                "Parameter int \$b",
                "Token int",
                "Identifier b",
                "ExpressionStatement mes(calc(\$b + 22));",
                "CommandCallExpression mes(calc(\$b + 22))",
                "Identifier mes",
                "CalcExpression calc(\$b + 22)",
                "ArithmeticExpression \$b + 22",
                "LocalVariableExpression \$b",
                "Identifier b",
                "Token +",
                "IntegerLiteral 22",
            ),
            spans,
        )
    }

    @Test
    fun testEmptyScriptFileSpansNothing() {
        val file = ScriptParser.createScriptFile("") ?: error("parse error")

        assertEquals(0, file.source.startOffset)
        assertEquals(0, file.source.endOffset)
    }
}
