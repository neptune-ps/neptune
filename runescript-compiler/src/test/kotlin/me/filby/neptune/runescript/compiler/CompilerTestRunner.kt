@file:JvmName("CompilerTestRunner")

package me.filby.neptune.runescript.compiler

import com.github.michaelbull.logging.InlineLogger
import me.filby.neptune.runescript.compiler.configuration.SymbolLoader
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostics
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticsHandler
import me.filby.neptune.runescript.compiler.runtime.ScriptManager
import me.filby.neptune.runescript.compiler.runtime.TestOpcodes
import me.filby.neptune.runescript.compiler.runtime.TestScriptRunner
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.SymbolTable
import me.filby.neptune.runescript.compiler.symbol.SymbolType
import me.filby.neptune.runescript.compiler.trigger.CommandTrigger
import me.filby.neptune.runescript.compiler.trigger.TestTriggerType
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type
import me.filby.neptune.runescript.compiler.type.wrapped.VarBitType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClientType
import me.filby.neptune.runescript.compiler.type.wrapped.VarPlayerType
import me.filby.neptune.runescript.runtime.impl.opcodes.CoreOpcodesBase
import me.filby.neptune.runescript.runtime.impl.opcodes.MathOpcodesBase
import me.filby.neptune.runescript.runtime.state.ScriptState
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

private val logger = InlineLogger()
private val testsPath = Paths.get("tests").toAbsolutePath()

fun main() {
    if (!testsPath.exists()) {
        error("$testsPath does not exist.")
    }

    logger.info { "Testing scripts within '$testsPath'" }

    val files = testsPath.toFile()
        .walk()
        .filter { it.extension == "cs2" }

    var hasErrors = false
    for (file in files) {
        if (testScriptFile(file)) {
            hasErrors = true
        }
    }

    if (hasErrors) {
        exitProcess(1)
    }
}

private fun testScriptFile(scriptFile: File): Boolean {
    val expectedErrors = parseExpectedErrors(scriptFile)

    val scriptManager = ScriptManager()
    val diagnosticsHandler = TestDiagnosticsHandler(scriptFile, expectedErrors)

    // run compiler
    val writer = TestScriptWriter(scriptManager)
    val compiler = ScriptCompiler(scriptFile.toPath(), writer)
    compiler.triggers.registerAll<TestTriggerType>()
    compiler.addSymbolLoader(CommandSymbolLoader())

    // setup proc type
    compiler.types.register("proc", MetaType.Script(TestTriggerType.PROC, MetaType.Unit, MetaType.Unit))

    // setup label type
    compiler.types.register("label", MetaType.Script(TestTriggerType.LABEL, MetaType.Unit, MetaType.Nothing))

    compiler.diagnosticsHandler = diagnosticsHandler
    compiler.run()

    // run runtime if no errors
    var hasRuntimeErrors = false
    if (!diagnosticsHandler.hasSemanticErrors) {
        val runner = createRuntime(scriptManager)

        // run all tests in the file
        val tests = scriptManager.getAllByTrigger(TestTriggerType.TEST)
        for (test in tests) {
            val time = measureTimeMillis {
                runner.execute(test) {
                    // count all aborted scripts as error
                    if (execution == ScriptState.ExecutionState.ABORTED) {
                        hasRuntimeErrors = true
                    }
                }
            }
            logger.info { "Ran ${test.name} in ${time}ms." }
        }
    }
    return diagnosticsHandler.hasUnexpectedErrors || hasRuntimeErrors
}

private fun parseExpectedErrors(file: File): ArrayDeque<String> {
    val expectedErrors = ArrayDeque<String>()
    file.useLines {
        for (line in it) {
            // skip any lines that don't contain a comment
            if ("//" !in line) {
                continue
            }

            val expectedIndex = line.indexOf("Expect:", ignoreCase = true)
            if (expectedIndex == -1) {
                continue
            }

            expectedErrors += line.substring(expectedIndex + "Expect:".length).trim()
        }
    }
    return expectedErrors
}

private fun createRuntime(scriptManager: ScriptManager): TestScriptRunner {
    val runner = TestScriptRunner()
    runner.registerHandlersFrom(CoreOpcodesBase<ScriptState>(scriptManager))
    runner.registerHandlersFrom(MathOpcodesBase<ScriptState>())
    runner.registerHandlersFrom(TestOpcodes())
    return runner
}

private class CommandSymbolLoader : SymbolLoader {
    override fun SymbolTable.load(compiler: ScriptCompiler) {
        // fake config symbols
        addBasic(VarPlayerType(PrimitiveType.INT), "varp")
        addBasic(VarBitType, "varbit")
        addBasic(VarClientType(PrimitiveType.INT), "varc")
        addBasic(VarClientType(PrimitiveType.STRING), "varcstr")

        // constants
        addConstant("max_32bit_int", "2147483647")
        addConstant("min_32bit_int", "-2147483648")
        addConstant("quote", "\"")

        // cyclic reference constants
        addConstant("a", "^b")
        addConstant("b", "^c")
        addConstant("c", "^a")

        // general commands
        addCommand("jump", compiler.types.find("label"), MetaType.Nothing)
        addCommand("gosub", compiler.types.find("proc"))
        addCommand("println", PrimitiveType.STRING)
        addCommand("tostring", PrimitiveType.INT, PrimitiveType.STRING)
        addCommand("int_to_long", PrimitiveType.INT, PrimitiveType.LONG)
        addCommand("long_to_int", PrimitiveType.LONG, PrimitiveType.INT)

        // test specific commands
        // TODO implement the argument checks better once dynamic command handling is added to compiler
        addCommand("error", PrimitiveType.STRING)
        addCommand("assert_equals", TupleType(MetaType.Any, MetaType.Any))
        addCommand("assert_equals_obj", TupleType(MetaType.Any, MetaType.Any))
        addCommand("assert_equals_long", TupleType(PrimitiveType.LONG, PrimitiveType.LONG))
        addCommand("assert_not", TupleType(MetaType.Any, MetaType.Any))
        addCommand("assert_not_obj", TupleType(MetaType.Any, MetaType.Any))
        addCommand("assert_not_long", TupleType(PrimitiveType.LONG, PrimitiveType.LONG))
    }

    fun SymbolTable.addCommand(name: String, parameters: Type = MetaType.Unit, returns: Type = MetaType.Unit) {
        val type = SymbolType.ClientScript(CommandTrigger)
        val symbol = ScriptSymbol.ClientScriptSymbol(CommandTrigger, name, parameters, returns)
        insert(type, symbol)
    }
}

private class TestDiagnosticsHandler(
    private val file: File,
    private val expectedErrors: ArrayDeque<String>,
) : DiagnosticsHandler {
    var hasUnexpectedErrors = false
        private set

    var hasSemanticErrors = false
        private set

    override fun Diagnostics.handleParse() {
        with(ScriptCompiler.DEFAULT_DIAGNOSTICS_HANDLER) {
            handleParse()
        }
    }

    override fun Diagnostics.handleTypeChecking() {
        for (diagnostic in diagnostics) {
            val (_, source, message, args) = diagnostic
            val (_, line) = source

            val formatted = message.format(*args.toTypedArray())
            if (!diagnostic.isError()) {
                // just print any diagnostic that isn't an error
                println(TEST_INFO_FORMAT.format(file.toString(), line, formatted))
                continue
            }

            val expected = expectedErrors.removeFirstOrNull()
            if (expected == null || expected != formatted) {
                println(TEST_FAIL_FORMAT.format(file.toString(), line, expected ?: "nothing", formatted))
                hasUnexpectedErrors = true
            }
        }

        if (expectedErrors.isNotEmpty()) {
            for (expected in expectedErrors) {
                println(TEST_FAIL_FORMAT.format(file.toString(), "0", expected, ""))
                hasUnexpectedErrors = true
            }
        }

        if (hasErrors()) {
            hasSemanticErrors = true
        }
    }

    override fun Diagnostics.handleCodeGeneration() {
        with(ScriptCompiler.DEFAULT_DIAGNOSTICS_HANDLER) {
            handleCodeGeneration()
        }
    }

    companion object {
        const val TEST_INFO_FORMAT = "%s:%s: %s"
        const val TEST_FAIL_FORMAT = "%s:%s: Expected <%s> got <%s>."
    }
}
