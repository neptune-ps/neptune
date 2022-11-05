package me.filby.neptune.runescript.compiler

import com.github.michaelbull.logging.InlineLogger
import me.filby.neptune.runescript.ast.ScriptFile
import me.filby.neptune.runescript.compiler.codegen.CodeGenerator
import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.configuration.SymbolLoader
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostics
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticsHandler
import me.filby.neptune.runescript.compiler.semantics.PreTypeChecking
import me.filby.neptune.runescript.compiler.semantics.TypeChecking
import me.filby.neptune.runescript.compiler.symbol.SymbolTable
import me.filby.neptune.runescript.compiler.writer.ScriptWriter
import me.filby.neptune.runescript.parser.ScriptParser
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.system.measureTimeMillis

/**
 * An entry point for compiling scripts.
 */
public class ScriptCompiler(
    sourcePath: Path,
    private val symbolLoader: SymbolLoader,
    private val scriptWriter: ScriptWriter
) {

    /**
     * Logger for this class.
     */
    private val logger = InlineLogger()

    /**
     * The root folder path that contains the source code.
     */
    private val sourcePath: Path = sourcePath.absolute().normalize()

    /**
     * The root table that contains all global symbols.
     */
    private val rootTable = SymbolTable()

    /**
     * Called after every step with all diagnostics that were collected during it.
     */
    public var diagnosticsHandler: DiagnosticsHandler = DEFAULT_DIAGNOSTICS_HANDLER

    /**
     * Runs the compiler by loading external symbols and then actually running
     * the compile process.
     */
    public fun run() {
        loadSymbols()
        compile()
    }

    /**
     * Calls the [SymbolLoader] with the root table to load all external symbols.
     */
    private fun loadSymbols() {
        symbolLoader.load(this, rootTable)
        // logger.info { "Loaded 1234 symbols." }
    }

    /**
     * Initiates the actual compile pipeline.
     */
    private fun compile() {
        // 1) Parse all files
        val (parseSuccess, fileNodes) = parse()
        if (!parseSuccess) {
            return
        }

        // 2) Analyze the nodes
        val analyzeSuccess = analyze(fileNodes)
        if (!analyzeSuccess) {
            return
        }

        // 3) Generate code
        val (codegenSuccess, scripts) = codegen(fileNodes)
        if (!codegenSuccess) {
            return
        }

        // 4) Write scripts
        write(scripts)
    }

    /**
     * Parses all files in the source path and returns the parsed AST nodes.
     */
    private fun parse(): Pair<Boolean, List<ScriptFile>> {
        val diagnostics = Diagnostics()

        logger.info { "Parsing files in $sourcePath" }
        val fileNodes = mutableListOf<ScriptFile>()
        // iterate over all folders and files in the source path
        var fileCount = 0
        for (file in sourcePath.toFile().walkTopDown()) {
            // TODO ability to configure file extension
            // skip directories and non .cs2 files
            if (file.isDirectory || file.extension != "cs2") {
                continue
            }

            val time = measureTimeMillis {
                val errorListener = ParserErrorListener(file.absolutePath, diagnostics)
                val node = ScriptParser.createScriptFile(file.toPath(), errorListener)
                if (node != null) {
                    fileNodes += node
                }
            }
            fileCount++
            logger.trace { "Parsed $file in ${time}ms" }
        }
        logger.info { "Parsed $fileCount files" }

        // call the diagnostics handler
        with(diagnosticsHandler) {
            diagnostics.handleParse()
        }
        return !diagnostics.hasErrors() to fileNodes
    }

    /**
     * Runs all [files] through the semantic analysis pipeline. If there were any errors,
     * the program will be halted with exit code `1`.
     */
    private fun analyze(files: List<ScriptFile>): Boolean {
        val diagnostics = Diagnostics()

        // pre-type check: this adds all scripts to the symbol table for lookup in the next phase
        logger.debug { "Starting pre-type checking" }
        val preTypeCheckingTime = measureTimeMillis {
            for (file in files) {
                val time = measureTimeMillis {
                    file.accept(PreTypeChecking(rootTable, diagnostics))
                }
                logger.trace { "Pre-type checked ${file.source.source} in ${time}ms" }
            }
        }
        logger.debug { "Finished pre-type checking in ${preTypeCheckingTime}ms" }

        // type check: this does all major type checking
        logger.debug { "Starting type checking" }
        val typeCheckingTime = measureTimeMillis {
            for (file in files) {
                val time = measureTimeMillis {
                    file.accept(TypeChecking(rootTable, diagnostics))
                }
                logger.trace { "Type checked ${file.source.source} in ${time}ms" }
            }
        }
        logger.debug { "Finished type checking in ${typeCheckingTime}ms" }

        // call the diagnostics handler
        with(diagnosticsHandler) {
            diagnostics.handleTypeChecking()
        }

        return !diagnostics.hasErrors()
    }

    /**
     * Runs all [files] through the code generator. Returns a list of all generated [RuneScript].
     * If there were any errors, the program will be halted with exit code `1`.
     */
    private fun codegen(files: List<ScriptFile>): Pair<Boolean, List<RuneScript>> {
        val diagnostics = Diagnostics()

        // run each file through the code generator and fetch the scripts from the generator
        // and add to a list that we return
        val scripts = mutableListOf<RuneScript>()
        logger.debug { "Starting codegen" }
        val codegenTime = measureTimeMillis {
            for (file in files) {
                val time = measureTimeMillis {
                    val codegen = CodeGenerator(rootTable, diagnostics)
                    file.accept(codegen)
                    scripts.addAll(codegen.scripts)
                }
                logger.trace { "Generated code for ${file.source.source} in ${time}ms" }
            }
        }
        logger.debug { "Finished codegen in ${codegenTime}ms" }
        return !diagnostics.hasErrors() to scripts
    }

    /**
     * Runs all [scripts] through the [ScriptWriter].
     */
    private fun write(scripts: List<RuneScript>) {
        logger.debug { "Starting script writing" }
        val writingTime = measureTimeMillis {
            for (script in scripts) {
                val scriptWriteTimer = measureTimeMillis {
                    scriptWriter.write(script)
                }
                logger.trace { "Wrote [${script.trigger.identifier},${script.name}] in ${scriptWriteTimer}ms" }
            }
        }
        logger.debug { "Finished script writing in ${writingTime}ms" }
    }

    public companion object {
        public val DEFAULT_DIAGNOSTICS_HANDLER: DiagnosticsHandler = DiagnosticsHandler.BaseDiagnosticsHandler()
    }
}
