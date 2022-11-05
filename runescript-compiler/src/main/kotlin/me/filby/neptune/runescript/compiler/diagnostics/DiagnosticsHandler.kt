package me.filby.neptune.runescript.compiler.diagnostics

import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.system.exitProcess

/**
 * Allows handling diagnostics for different parts of the compiler process.
 */
public interface DiagnosticsHandler {
    /**
     * Allows handling diagnostics after the parse step.
     */
    public fun Diagnostics.handleParse() {
    }

    /**
     * Allows handling diagnostics after the type checking step.
     */
    public fun Diagnostics.handleTypeChecking() {
    }

    /**
     * Allows handling diagnostics after the code generation step.
     */
    public fun Diagnostics.handleCodeGeneration() {
    }

    /**
     * A base implementation of a diagnostics handler that points to the line an error occurs on
     * and exits the process with exit code `1` if there were any errors during any steps.
     */
    public class BaseDiagnosticsHandler : DiagnosticsHandler {
        override fun Diagnostics.handleParse() {
            handleShared()
        }

        override fun Diagnostics.handleTypeChecking() {
            handleShared()
        }

        override fun Diagnostics.handleCodeGeneration() {
            handleShared()
        }

        private fun Diagnostics.handleShared() {
            // TODO handle looking up source line information better
            val fileLines = mutableMapOf<String, List<String>>()
            for ((type, source, message, args) in diagnostics) {
                val lines = fileLines.getOrPut(source.source) { Files.readAllLines(Path(source.source)) }
                val location = "${source.source}:${source.line}:${source.column}"
                val formattedMessage = message.format(*args.toTypedArray())
                println("$location: $type: $formattedMessage")
                if (source.line - 1 < lines.size) {
                    println("    > ${lines[source.line - 1]}")
                    println("    > ${"-".repeat(source.column - 1)}^")
                }
            }

            if (hasErrors()) {
                exitProcess(1)
            }
        }
    }
}
