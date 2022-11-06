@file:JvmName("CompilerTestRunner")

package me.filby.neptune.runescript.compiler

import com.github.michaelbull.logging.InlineLogger
import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostics
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticsHandler
import me.filby.neptune.runescript.compiler.runtime.TestScriptRunner
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter
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

private class TestScriptWriter : BaseScriptWriter() {
    override fun write(script: RuneScript) {
    }
}

private fun testScriptFile(scriptFile: File): Boolean {
    val expectedErrors = parseExpectedErrors(scriptFile)
    val diagnosticsHandler = TestDiagnosticsHandler(scriptFile, expectedErrors)

    val compiler = ScriptCompiler(scriptFile.toPath(), TestScriptWriter())
    compiler.diagnosticsHandler = diagnosticsHandler
    compiler.run()

    // TODO test runtime
    // only run runtime if there were no compile time errors
    if (!diagnosticsHandler.hasErrors) {
        // val runner = createRuntime()
    }

    // TODO account for runtime errors too
    return diagnosticsHandler.hasErrors
}

private fun createRuntime(): TestScriptRunner {
    val runner = TestScriptRunner()
    runner.registerHandlersFrom(CoreOpcodesBase<ScriptState>(runner))
    runner.registerHandlersFrom(MathOpcodesBase<ScriptState>())
    // TODO register test specific opcodes
    return runner
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

private class TestDiagnosticsHandler(
    private val file: File,
    private val expectedErrors: ArrayDeque<String>,
) : DiagnosticsHandler {
    var hasErrors: Boolean = false
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

            val expected = expectedErrors.removeFirstOrNull()
            val actual = message.format(*args.toTypedArray())
            if (expected == null || expected != actual) {
                println(TEST_FAIL_FORMAT.format(file.toString(), line, expected ?: "nothing", actual))
                hasErrors = true
            }
        }

        if (expectedErrors.isNotEmpty()) {
            for (expected in expectedErrors) {
                println(TEST_FAIL_FORMAT.format(file.toString(), "0", expected, ""))
                hasErrors = true
            }
        }
    }

    override fun Diagnostics.handleCodeGeneration() {
        with(ScriptCompiler.DEFAULT_DIAGNOSTICS_HANDLER) {
            handleCodeGeneration()
        }
    }

    companion object {
        const val TEST_FAIL_FORMAT = "%s:%s: Expected <%s> got <%s>."
    }
}
