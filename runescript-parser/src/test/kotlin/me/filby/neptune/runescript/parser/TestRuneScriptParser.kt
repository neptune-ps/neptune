package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.antlr.RuneScriptParser
import me.filby.neptune.runescript.ast.NodeSourceLocation
import me.filby.neptune.runescript.ast.Parameter
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
import me.filby.neptune.runescript.parser.ScriptParser.invokeParser
import me.filby.neptune.runescript.type.PrimitiveType
import org.junit.jupiter.api.assertThrows
import kotlin.properties.ReadOnlyProperty
import kotlin.test.Test
import kotlin.test.assertEquals

class TestRuneScriptParser {

    @Test
    fun testScript() {
        val script = ScriptParser.createScript("[opheld1,abyssal_whip](int ${'$'}test)(int)")
        val parameters = listOf(Parameter(EMPTY_SOURCE, PrimitiveType.INT, Identifier(EMPTY_SOURCE, "test")))
        val returns = PrimitiveType.INT
        val expected = Script(
            EMPTY_SOURCE,
            Identifier(EMPTY_SOURCE, "opheld1"),
            Identifier(EMPTY_SOURCE, "abyssal_whip"),
            parameters,
            returns,
            emptyList()
        )
        assertEquals(expected, script)
    }

    @Test
    fun testBlockStatement() {
        val statement = invokeParser("{}", RuneScriptParser::statement)
        val expected = BlockStatement(EMPTY_SOURCE, emptyList())
        assertEquals(expected, statement)
    }

    @Test
    fun testReturnStatement() {
        val one = IntegerLiteral(EMPTY_SOURCE, 1)
        val statement = invokeParser("return(1);", RuneScriptParser::statement)
        val expected = ReturnStatement(EMPTY_SOURCE, listOf(one))
        assertEquals(expected, statement)
    }

    @Test
    fun testIfStatement() {
        // if (1 = 1) {}
        val simpleIf = IfStatement(
            EMPTY_SOURCE,
            BinaryExpression(EMPTY_SOURCE, IntegerLiteral(EMPTY_SOURCE, 1), "=", IntegerLiteral(EMPTY_SOURCE, 1)),
            BlockStatement(EMPTY_SOURCE, emptyList()),
            null
        )
        assertEquals(simpleIf, invokeParser("if (1 = 1) {}", RuneScriptParser::statement))

        // if (2 = 2) { } else if (1 = 1) {}
        val extraBlock = IfStatement(
            EMPTY_SOURCE,
            BinaryExpression(EMPTY_SOURCE, IntegerLiteral(EMPTY_SOURCE, 2), "=", IntegerLiteral(EMPTY_SOURCE, 2)),
            BlockStatement(EMPTY_SOURCE, emptyList()),
            simpleIf
        )
        assertEquals(extraBlock, invokeParser("if (2 = 2) {} else if (1 = 1) {}", RuneScriptParser::statement))

        // TODO test logical or and and
    }

    @Test
    fun testWhileStatement() {
        // while (1 = 1) {}
        val simpleWhile = WhileStatement(
            EMPTY_SOURCE,
            BinaryExpression(EMPTY_SOURCE, IntegerLiteral(EMPTY_SOURCE, 1), "=", IntegerLiteral(EMPTY_SOURCE, 1)),
            BlockStatement(EMPTY_SOURCE, emptyList()),
        )
        assertEquals(simpleWhile, invokeParser("while (1 = 1) {}", RuneScriptParser::statement))
    }

    @Test
    fun testSwitchStatement() {
        // switch_int ($test) {}
        val simpleSwitch = SwitchStatement(
            EMPTY_SOURCE,
            PrimitiveType.INT,
            LocalVariableExpression(EMPTY_SOURCE, Identifier(EMPTY_SOURCE, "test")),
            emptyList()
        )
        assertEquals(simpleSwitch, invokeParser("switch_int (\$test) {}", RuneScriptParser::statement))

        val simpleCase = SwitchCase(
            EMPTY_SOURCE,
            listOf(IntegerLiteral(EMPTY_SOURCE, 1)),
            listOf(ReturnStatement(EMPTY_SOURCE, listOf(BooleanLiteral(EMPTY_SOURCE, true))))
        )
        val simpleSwitchWithCase = SwitchStatement(
            EMPTY_SOURCE,
            PrimitiveType.INT,
            LocalVariableExpression(EMPTY_SOURCE, Identifier(EMPTY_SOURCE, "test")),
            listOf(simpleCase)
        )
        assertEquals(simpleSwitchWithCase, invokeParser("""
            switch_int (${'$'}test) {
                case 1:
                    return(true);
            }
        """.trimIndent(), RuneScriptParser::statement))
    }

    @Test
    fun testDeclarationStatement() {
        // test with initializer
        assertEquals(
            DeclarationStatement(
                EMPTY_SOURCE,
                PrimitiveType.INT,
                Identifier(EMPTY_SOURCE, "test"),
                IntegerLiteral(EMPTY_SOURCE, 1)
            ),
            invokeParser("def_int ${'$'}test = 1;", RuneScriptParser::statement)
        )

        // test without initializer
        assertEquals(
            DeclarationStatement(EMPTY_SOURCE, PrimitiveType.INT, Identifier(EMPTY_SOURCE, "test"), null),
            invokeParser("def_int ${'$'}test;", RuneScriptParser::statement)
        )
    }

    @Test
    fun testArrayDeclarationStatement() {
        // test with size initializer
        assertEquals(
            ArrayDeclarationStatement(
                EMPTY_SOURCE,
                PrimitiveType.INT,
                Identifier(EMPTY_SOURCE, "test"),
                IntegerLiteral(EMPTY_SOURCE, 1)
            ),
            invokeParser("def_int ${'$'}test(1);", RuneScriptParser::statement)
        )

        // test without initializer
        assertThrows<ParsingException> { invokeParser("def_int ${'$'}test();", RuneScriptParser::statement) }
    }

    @Test
    fun testAssignmentStatement() {
        val one by dupe { IntegerLiteral(EMPTY_SOURCE, 1) }
        val localVar by dupe { LocalVariableExpression(EMPTY_SOURCE, Identifier(EMPTY_SOURCE, "test")) }
        val localArrayVar by dupe { LocalVariableExpression(EMPTY_SOURCE, Identifier(EMPTY_SOURCE, "test"), one) }
        val gameVar by dupe { GameVariableExpression(EMPTY_SOURCE, Identifier(EMPTY_SOURCE, "test")) }

        // simple assignment with 1 var and expression
        assertEquals(
            AssignmentStatement(EMPTY_SOURCE, listOf(localVar), listOf(one)),
            invokeParser("${'$'}test = 1;", RuneScriptParser::statement)
        )

        // simple assignment with 1 array var and expression
        assertEquals(
            AssignmentStatement(EMPTY_SOURCE, listOf(localArrayVar), listOf(one)),
            invokeParser("${'$'}test(1) = 1;", RuneScriptParser::statement)
        )

        // multi assignment
        assertEquals(
            AssignmentStatement(EMPTY_SOURCE, listOf(localVar, localVar), listOf(one, one)),
            invokeParser("${'$'}test, ${'$'}test = 1, 1;", RuneScriptParser::statement)
        )

        // test game var assignment
        assertEquals(
            AssignmentStatement(EMPTY_SOURCE, listOf(gameVar), listOf(one)),
            invokeParser("%test = 1;", RuneScriptParser::statement)
        )

        // test multi mixed var assignment
        assertEquals(
            AssignmentStatement(EMPTY_SOURCE, listOf(localVar, localArrayVar, gameVar), listOf(one, one, one)),
            invokeParser("${'$'}test, ${'$'}test(1), %test = 1, 1, 1;", RuneScriptParser::statement)
        )
    }

    @Test
    fun testExpressionStatement() {
        val statement = invokeParser("calc(1 + 1);", RuneScriptParser::statement)
        val expected = ExpressionStatement(
            EMPTY_SOURCE,
            CalcExpression(
                EMPTY_SOURCE,
                BinaryExpression(EMPTY_SOURCE, IntegerLiteral(EMPTY_SOURCE, 1), "+", IntegerLiteral(EMPTY_SOURCE, 1))
            )
        )
        assertEquals(expected, statement)
    }

    @Test
    fun testEmptyStatement() {
        assertEquals(EmptyStatement(EMPTY_SOURCE), invokeParser(";", RuneScriptParser::statement))
    }

    @Test
    fun testParenthesizedExpression() {
        // calc((1 + 1) * 1)
        val addition = ParenthesizedExpression(
            EMPTY_SOURCE,
            BinaryExpression(EMPTY_SOURCE, IntegerLiteral(EMPTY_SOURCE, 1), "+", IntegerLiteral(EMPTY_SOURCE, 1))
        )
        val combined = BinaryExpression(EMPTY_SOURCE, addition, "*", IntegerLiteral(EMPTY_SOURCE, 1))
        val calc = CalcExpression(EMPTY_SOURCE, combined)
        assertEquals(
            calc,
            invokeParser("calc((1 + 1) * 1)", RuneScriptParser::expression)
        )
    }

    @Test
    fun testCalcExpression() {
        val one by dupe { IntegerLiteral(EMPTY_SOURCE, 1) }

        // calc(1 + 1)
        val onePlusOne = BinaryExpression(EMPTY_SOURCE, one, "+", one)
        assertEquals(
            CalcExpression(EMPTY_SOURCE, onePlusOne),
            invokeParser("calc(1 + 1)", RuneScriptParser::expression)
        )

        // calc(1 - 1)
        val oneMinusOne = BinaryExpression(EMPTY_SOURCE, one, "-", one)
        assertEquals(
            CalcExpression(EMPTY_SOURCE, oneMinusOne),
            invokeParser("calc(1 - 1)", RuneScriptParser::expression)
        )

        // calc(1 * 1)
        val oneTimesOne = BinaryExpression(EMPTY_SOURCE, one, "*", one)
        assertEquals(
            CalcExpression(EMPTY_SOURCE, oneTimesOne),
            invokeParser("calc(1 * 1)", RuneScriptParser::expression)
        )

        // calc(1 / 1)
        val oneDivOne = BinaryExpression(EMPTY_SOURCE, one, "/", one)
        assertEquals(
            CalcExpression(EMPTY_SOURCE, oneDivOne),
            invokeParser("calc(1 / 1)", RuneScriptParser::expression)
        )

        // calc(1 % 1)
        val oneModOne = BinaryExpression(EMPTY_SOURCE, one, "%", one)
        assertEquals(
            CalcExpression(EMPTY_SOURCE, oneModOne),
            invokeParser("calc(1 % 1)", RuneScriptParser::expression)
        )

        // calc(1 & 1)
        val oneAndOne = BinaryExpression(EMPTY_SOURCE, one, "&", one)
        assertEquals(
            CalcExpression(EMPTY_SOURCE, oneAndOne),
            invokeParser("calc(1 & 1)", RuneScriptParser::expression)
        )

        // calc(1 | 1)
        val oneOrOne = BinaryExpression(EMPTY_SOURCE, one, "|", one)
        assertEquals(
            CalcExpression(EMPTY_SOURCE, oneOrOne),
            invokeParser("calc(1 | 1)", RuneScriptParser::expression)
        )

        // calc(1 + 1 - 1 * 1 / 1 % 1) should parse as calc((1 + 1) - (((1 * 1) / 1) % 1))
        val mul = BinaryExpression(EMPTY_SOURCE, one, "*", one)
        val div = BinaryExpression(EMPTY_SOURCE, mul, "/", one)
        val mod = BinaryExpression(EMPTY_SOURCE, div, "%", one)
        val add = BinaryExpression(EMPTY_SOURCE, one, "+", one)
        val sub = BinaryExpression(EMPTY_SOURCE, add, "-", mod)
        assertEquals(
            CalcExpression(EMPTY_SOURCE, sub),
            invokeParser("calc(1 + 1 - 1 * 1 / 1 % 1)", RuneScriptParser::expression)
        )

        // calc(1 | 1 & 1) should parse as calc(1 | (1 & 1))
        assertEquals(
            CalcExpression(
                EMPTY_SOURCE,
                BinaryExpression(EMPTY_SOURCE, one, "|", BinaryExpression(EMPTY_SOURCE, one, "&", one))
            ),
            invokeParser("calc(1 | 1 & 1)", RuneScriptParser::expression)
        )

        // verify addition only works inside of calc()
        assertThrows<ParsingException> { invokeParser("1 + 1", RuneScriptParser::expression) }
    }

    @Test
    fun testCommandCallExpression() {
        assertEquals(
            CommandCallExpression(EMPTY_SOURCE, Identifier(EMPTY_SOURCE, "clientclock"), emptyList()),
            invokeParser("clientclock()", RuneScriptParser::expression)
        )

        assertEquals(
            CommandCallExpression(EMPTY_SOURCE, Identifier(EMPTY_SOURCE, ".npc_find"), emptyList()),
            invokeParser(".npc_find()", RuneScriptParser::expression)
        )

        // TODO test for call without arguments? currently will parse as a normal identifier
    }

    @Test
    fun testProcCallExpression() {
        // call with no arguments
        assertEquals(
            ProcCallExpression(EMPTY_SOURCE, Identifier(EMPTY_SOURCE, "testing"), emptyList()),
            invokeParser("~testing", RuneScriptParser::expression)
        )

        // call with argument
        assertEquals(
            ProcCallExpression(
                EMPTY_SOURCE,
                Identifier(EMPTY_SOURCE, "testing"),
                listOf(IntegerLiteral(EMPTY_SOURCE, 1))
            ),
            invokeParser("~testing(1)", RuneScriptParser::expression)
        )
    }

    @Test
    fun testJumpCallExpression() {
        // call with no arguments
        assertEquals(
            JumpCallExpression(EMPTY_SOURCE, Identifier(EMPTY_SOURCE, "testing"), emptyList()),
            invokeParser("@testing", RuneScriptParser::expression)
        )

        // call with argument
        assertEquals(
            JumpCallExpression(
                EMPTY_SOURCE,
                Identifier(EMPTY_SOURCE, "testing"),
                listOf(IntegerLiteral(EMPTY_SOURCE, 1))
            ),
            invokeParser("@testing(1)", RuneScriptParser::expression)
        )
    }

    @Test
    fun testLocalVariableExpression() {
        // $var
        assertEquals(
            LocalVariableExpression(EMPTY_SOURCE, Identifier(EMPTY_SOURCE, "var")),
            invokeParser("${'$'}var", RuneScriptParser::expression)
        )

        // $var(1)
        assertEquals(
            LocalVariableExpression(EMPTY_SOURCE, Identifier(EMPTY_SOURCE, "var"), IntegerLiteral(EMPTY_SOURCE, 1)),
            invokeParser("${'$'}var(1)", RuneScriptParser::expression)
        )

        // %var
        assertEquals(
            GameVariableExpression(EMPTY_SOURCE, Identifier(EMPTY_SOURCE, "var")),
            invokeParser("%var", RuneScriptParser::expression)
        )

        // ^var
        assertEquals(
            ConstantVariableExpression(EMPTY_SOURCE, Identifier(EMPTY_SOURCE, "var")),
            invokeParser("^var", RuneScriptParser::expression)
        )
    }

    @Test
    fun testIntegerLiteral() {
        assertEquals(
            IntegerLiteral(EMPTY_SOURCE, 1337),
            invokeParser("1337", RuneScriptParser::literal)
        )
        assertThrows<ParsingException> { invokeParser("1337_", RuneScriptParser::literal) }

        assertEquals(
            IntegerLiteral(EMPTY_SOURCE, 0xFFFFFF),
            invokeParser("0xFFFFFF", RuneScriptParser::literal)
        )
        assertEquals(
            IntegerLiteral(EMPTY_SOURCE, -1),
            invokeParser("0xFFFFFFFF", RuneScriptParser::literal)
        )
        assertEquals(
            IntegerLiteral(EMPTY_SOURCE, 0xFFFFFF),
            invokeParser("0XFFFFFF", RuneScriptParser::literal)
        )
    }

    @Test
    fun testBooleanLiteral() {
        assertEquals(
            BooleanLiteral(EMPTY_SOURCE, true),
            invokeParser("true", RuneScriptParser::literal)
        )
        assertEquals(
            BooleanLiteral(EMPTY_SOURCE, false),
            invokeParser("false", RuneScriptParser::literal)
        )
    }

    @Test
    fun testCharacterLiteral() {
        assertEquals(
            CharacterLiteral(EMPTY_SOURCE, 't'),
            invokeParser("'t'", RuneScriptParser::literal)
        )

        // escaped '
        assertEquals(
            CharacterLiteral(EMPTY_SOURCE, '\''),
            invokeParser("'\\''", RuneScriptParser::literal)
        )

        assertThrows<ParsingException> { invokeParser("'test'", RuneScriptParser::literal) }
    }

    @Test
    fun testStringLiteral() {
        // basic string test
        assertEquals(
            StringLiteral(EMPTY_SOURCE, "this is a test"),
            invokeParser("\"this is a test\"", RuneScriptParser::literal)
        )

        // test escaping "\< \" \\"
        assertEquals(
            StringLiteral(EMPTY_SOURCE, "< \" \\"),
            invokeParser("\"\\< \\\" \\\\\"", RuneScriptParser::literal)
        )

        // test tags
        assertEquals(
            StringLiteral(EMPTY_SOURCE, "<col=ffffff><br> testing"),
            invokeParser("\"<col=ffffff><br> testing\"", RuneScriptParser::literal)
        )
    }

    @Test
    fun testNullLiteral() {
        assertEquals(
            NullLiteral(EMPTY_SOURCE),
            invokeParser("null", RuneScriptParser::literal)
        )
    }

    @Test
    fun testJoinedStringExpression() {
        // normal
        val part1 by dupe { StringLiteral(EMPTY_SOURCE, "1 + 1 = ") }
        val part2 by dupe {
            CalcExpression(
                EMPTY_SOURCE,
                BinaryExpression(EMPTY_SOURCE, IntegerLiteral(EMPTY_SOURCE, 1), "+", IntegerLiteral(EMPTY_SOURCE, 1))
            )
        }
        val part3 by dupe { StringLiteral(EMPTY_SOURCE, ".") }
        val joined = JoinedStringExpression(EMPTY_SOURCE, listOf(part1, part2, part3))
        assertEquals(
            joined,
            invokeParser("\"1 + 1 = <calc(1 + 1)>.\"", RuneScriptParser::expression)
        )

        // escaping
        val part4 = StringLiteral(EMPTY_SOURCE, "\\")
        val joined2 = JoinedStringExpression(EMPTY_SOURCE, listOf(part1, part2, part3, part4))
        assertEquals(
            joined2,
            invokeParser("\"1 + 1 = <calc(1 + 1)>.\\\\\"", RuneScriptParser::expression)
        )

        // tags
        val exprs: List<Expression> = listOf(
            StringLiteral(EMPTY_SOURCE, "<col=ffffff>"),
            IntegerLiteral(EMPTY_SOURCE, 1),
            StringLiteral(EMPTY_SOURCE, "</col>")
        )
        assertEquals(
            JoinedStringExpression(EMPTY_SOURCE, exprs),
            invokeParser("\"<col=ffffff><1></col>\"", RuneScriptParser::expression)
        )
    }

    @Test
    fun testIdentifier() {
        // normal identifier
        assertEquals(
            Identifier(EMPTY_SOURCE, "abyssal_whip"),
            invokeParser("abyssal_whip", RuneScriptParser::identifier)
        )

        // normal identifier beginning with digit
        assertEquals(
            Identifier(EMPTY_SOURCE, "100_coral_flower"),
            invokeParser("100_coral_flower", RuneScriptParser::identifier)
        )

        // component identifier (mostly)
        assertEquals(
            Identifier(EMPTY_SOURCE, "smithing:arrowheads"),
            invokeParser("smithing:arrowheads", RuneScriptParser::identifier)
        )

        // identifier that looks like a hex literal
        assertEquals(
            Identifier(EMPTY_SOURCE, "0x123"),
            invokeParser("0x123", RuneScriptParser::identifier)
        )

        // identifier with a .
        assertEquals(
            Identifier(EMPTY_SOURCE, ".abyssal_whip"),
            invokeParser(".abyssal_whip", RuneScriptParser::identifier)
        )

        // identifier that is a keyword
        assertEquals(
            Identifier(EMPTY_SOURCE, "true"),
            invokeParser("true", RuneScriptParser::identifier)
        )
    }

    companion object {
        private val EMPTY_SOURCE = NodeSourceLocation("null", 0, 0)

        /**
         * Returns whatever [initializer] returns each time the property is fetched. This is only used so we can easily
         * "re-use" a variable when doing tests since nodes will throw an error when trying to set their parent after
         * it was already set.
         */
        private inline fun <T, V> dupe(crossinline initializer: () -> V) =
            ReadOnlyProperty<T, V> { _, _ -> initializer() }
    }

}
