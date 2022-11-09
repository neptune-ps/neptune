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
import me.filby.neptune.runescript.compiler.trigger.ClientTriggerType
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
    compiler.addSymbolLoader(CommandSymbolLoader())
    compiler.diagnosticsHandler = diagnosticsHandler
    compiler.run()

    // run runtime if no errors
    var hasRuntimeErrors = false
    if (!diagnosticsHandler.hasSemanticErrors) {
        val runner = createRuntime(scriptManager)

        // search for the entry point
        val entryPoint = scriptManager.getOrNull("[proc,main]")
        if (entryPoint == null) {
            println("$scriptFile:0: Unable to locate [proc,main]")
            return true
        }

        runner.execute(entryPoint) {
            // count all aborted scripts as error
            if (execution == ScriptState.ExecutionState.ABORTED) {
                hasRuntimeErrors = true
            }
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
    override fun load(compiler: ScriptCompiler, rootTable: SymbolTable) {
        // fake config symbols
        rootTable.addConfig(VarPlayerType(PrimitiveType.INT), "varp")
        rootTable.addConfig(VarBitType, "varbit")
        rootTable.addConfig(VarClientType(PrimitiveType.INT), "varc")
        rootTable.addConfig(VarClientType(PrimitiveType.STRING), "varcstr")

        // general commands
        rootTable.addCommand("println", PrimitiveType.STRING)
        rootTable.addCommand("tostring", PrimitiveType.INT, PrimitiveType.STRING)
        rootTable.addCommand("int_to_long", PrimitiveType.INT, PrimitiveType.LONG)
        rootTable.addCommand("long_to_int", PrimitiveType.LONG, PrimitiveType.INT)

        // test specific commands
        // TODO implement the argument checks better once dynamic command handling is added to compiler
        rootTable.addCommand("error", PrimitiveType.STRING)
        rootTable.addCommand("assert_equals", TupleType(MetaType.Any, MetaType.Any))
        rootTable.addCommand("assert_equals_obj", TupleType(MetaType.Any, MetaType.Any))
        rootTable.addCommand("assert_equals_long", TupleType(MetaType.Any, MetaType.Any))
        rootTable.addCommand("assert_not", TupleType(MetaType.Any, MetaType.Any))
        rootTable.addCommand("assert_not_obj", TupleType(MetaType.Any, MetaType.Any))
        rootTable.addCommand("assert_not_long", TupleType(MetaType.Any, MetaType.Any))
    }

    fun SymbolTable.addCommand(name: String, parameters: Type = MetaType.Unit, returns: Type = MetaType.Unit) {
        val type = SymbolType.ClientScript(ClientTriggerType.COMMAND)
        val symbol = ScriptSymbol.ClientScriptSymbol(ClientTriggerType.COMMAND, name, parameters, returns)
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
