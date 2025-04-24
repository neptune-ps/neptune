package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.antlr.RuneScriptParser
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Token
import me.filby.neptune.runescript.ast.expr.BasicStringPart
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.expr.Literal
import me.filby.neptune.runescript.ast.expr.StringLiteral
import me.filby.neptune.runescript.ast.statement.SwitchCase
import me.filby.neptune.runescript.parser.ScriptParser.invokeParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.ParserRuleContext
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertEquals

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TestRuneScriptParser {
    @Test
    fun testScript() {
        runFileTest("script", RuneScriptParser::script)
    }

    @Test
    @Order(STATEMENT)
    fun testBlockStatement() {
        runFileTest("statements/block_statement", RuneScriptParser::statement)
    }

    @ParameterizedTest
    @ValueSource(strings = ["empty", "single", "multi"])
    @Order(STATEMENT)
    fun testReturnStatement(test: String) {
        runFileTest("statements/return/$test", RuneScriptParser::statement)
    }

    @ParameterizedTest
    @ValueSource(strings = ["basic", "else_if", "logical_and", "logical_or", "mixed"])
    @Order(STATEMENT)
    fun testIfStatement(test: String) {
        runFileTest("statements/ifs/$test", RuneScriptParser::statement)
    }

    @Test
    @Order(STATEMENT)
    fun testWhileStatement() {
        runFileTest("statements/while_statement", RuneScriptParser::statement)
    }

    @ParameterizedTest
    @ValueSource(strings = ["basic", "complex"])
    @Order(STATEMENT)
    fun testSwitchStatement(test: String) {
        runFileTest("statements/switch/$test", RuneScriptParser::statement)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "normal", "array",
        ],
    )
    @Order(STATEMENT)
    fun testDeclarationStatement(test: String) {
        runFileTest("statements/declaration/$test", RuneScriptParser::statement)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "local", "local_array", "local_multi", "game", "game_multi", "mixed_multi",
        ],
    )
    @Order(STATEMENT)
    fun testAssignmentStatement(test: String) {
        runFileTest("statements/assignment/$test", RuneScriptParser::statement)
    }

    @Test
    @Order(STATEMENT)
    fun testExpressionStatement() {
        runFileTest("statements/expression_statement", RuneScriptParser::statement)
    }

    @Test
    @Order(STATEMENT)
    fun testEmptyStatement() {
        runFileTest("statements/empty", RuneScriptParser::statement)
    }

    @Test
    @Order(EXPRESSION)
    fun testParenthesis() {
        runFileTest("expressions/parenthesis", RuneScriptParser::expression)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "add", "sub", "multiply", "divide",
            "modulo", "and", "or",
            "precedence_core", "precedence_bitwise",
        ],
    )
    @Order(EXPRESSION)
    fun testCalc(test: String) {
        runFileTest("expressions/calc/$test", RuneScriptParser::expression)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "proc_no_args", "proc_args",
            "command_no_args", "command_args", "command_dot",
            "jump_no_args", "jump_args",
        ],
    )
    @Order(EXPRESSION)
    fun testCall(test: String) {
        runFileTest("expressions/calls/$test", RuneScriptParser::expression)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "local", "local_array", "local_prefix", "local_postfix",
            "game", "game_prefix", "game_postfix",
            "constant",
        ],
    )
    @Order(EXPRESSION)
    fun testVariable(test: String) {
        runFileTest("expressions/variables/$test", RuneScriptParser::expression)
    }

    @ParameterizedTest
    @ValueSource(strings = ["basic_interop", "escape", "tags", "ptag"])
    @Order(EXPRESSION)
    fun testJoinedString(test: String) {
        runFileTest("expressions/joined_string/$test", RuneScriptParser::joinedString)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "null",
            "bool_true", "bool_false",
            "string_basic", "string_escape",
            "char_basic", "char_escape",
            "int_basic", "int_hex",
            "coord",
        ],
    )
    @Order(EXPRESSION)
    fun testLiteral(test: String) {
        runFileTest("expressions/literals/$test", RuneScriptParser::literal)
    }

    @ParameterizedTest
    @ValueSource(strings = ["basic", "digit_start", "component", "hex", "dot", "keyword"])
    @Order(EXPRESSION)
    fun testIdentifier(test: String) {
        runFileTest("expressions/identifiers/$test", RuneScriptParser::identifier)
    }

    companion object {
        /**
         * The test order used for [me.filby.neptune.runescript.ast.expr.Expression]s.
         */
        private const val EXPRESSION = 0

        /**
         * The test order used for [me.filby.neptune.runescript.ast.statement.Statement]s.
         */
        private const val STATEMENT = 1

        /**
         * Loads `<name>.src` and parses it into an AST then compares it to `<name>.exp`.
         */
        private fun runFileTest(name: String, entry: (RuneScriptParser) -> ParserRuleContext) = doubleUse(
            this::class.java.getResourceAsStream("$name.src") ?: error("$name.src does not exist"),
            this::class.java.getResourceAsStream("$name.exp") ?: error("$name.exp does not exist"),
        ) { input, output ->
            val ast = invokeParser(CharStreams.fromStream(input), entry) ?: error("parse error")
            val tree = ast.toStringTree().trim()
            val expectedTree = output.bufferedReader().use { it.readText() }.replace("\r", "").trim()
            assertEquals(expectedTree, tree, "$name.src != $name.exp")
        }

        /**
         * Builds the node tree as a string.
         */
        private fun Node.toStringTree(indent: Int = 0): String = buildString {
            for (i in 0 until indent) {
                append(' ')
            }

            with(this@toStringTree) {
                append(
                    when (this) {
                        is SwitchCase -> "SwitchCase(default=$isDefault)"
                        is Identifier -> "Identifier(text=\"$text\")"
                        is StringLiteral -> "StringLiteral(value=\"$value\")"
                        is BasicStringPart -> "${this::class.simpleName}(value=\"$value\")"
                        is Literal<*> -> "${this::class.simpleName}(value=$value)"
                        is Token -> "Token(text=$text)"
                        else -> "${this::class.simpleName}()"
                    },
                )

                append(' ')
                appendLine("${source.line}:${source.column}")
            }

            for (child in children) {
                append(child.toStringTree(indent + 1))
            }
        }

        private inline fun doubleUse(
            s1: InputStream,
            s2: InputStream,
            crossinline block: (InputStream, InputStream) -> Unit,
        ) {
            s1.use {
                s2.use {
                    block(s1, s2)
                }
            }
        }
    }
}
