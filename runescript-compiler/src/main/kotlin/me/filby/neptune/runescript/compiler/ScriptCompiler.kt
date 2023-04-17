package me.filby.neptune.runescript.compiler

import com.github.michaelbull.logging.InlineLogger
import me.filby.neptune.runescript.ast.ScriptFile
import me.filby.neptune.runescript.compiler.codegen.CodeGenerator
import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.configuration.SymbolLoader
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostics
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticsHandler
import me.filby.neptune.runescript.compiler.incremental.IncrementalData
import me.filby.neptune.runescript.compiler.incremental.IncrementalFile
import me.filby.neptune.runescript.compiler.incremental.ScriptHeaderGenerator
import me.filby.neptune.runescript.compiler.semantics.PreTypeChecking
import me.filby.neptune.runescript.compiler.semantics.TypeChecking
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.SymbolTable
import me.filby.neptune.runescript.compiler.trigger.CommandTrigger
import me.filby.neptune.runescript.compiler.trigger.TriggerManager
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TypeManager
import me.filby.neptune.runescript.compiler.type.wrapped.WrappedType
import me.filby.neptune.runescript.compiler.writer.ScriptWriter
import me.filby.neptune.runescript.parser.ScriptParser
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString
import kotlin.io.path.inputStream
import kotlin.io.path.notExists
import kotlin.io.path.outputStream
import kotlin.io.path.readAttributes
import kotlin.io.path.relativeTo
import kotlin.system.measureTimeMillis

/**
 * An entry point for compiling scripts.
 */
public open class ScriptCompiler(
    sourcePath: Path,
    private val scriptWriter: ScriptWriter,
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
     * The path to the incremental file that stores information for the last build.
     *
     * Set via `INCREMENTAL_PATH` environmental variable.
     */
    private val incrementalPath: Path?

    /**
     * The loaded [IncrementalData] from [incrementalPath] if it was loaded.
     */
    private var incrementalData: IncrementalData? = null

    /**
     * The root table that contains all global symbols.
     */
    private val rootTable = SymbolTable()

    /**
     * A list of [SymbolLoader]s called before attempting compilation.
     */
    private val symbolLoaders = mutableListOf<SymbolLoader>()

    /**
     * A mapping of command names to their handler implementation.
     */
    private val dynamicCommandHandlers = mutableMapOf<String, DynamicCommandHandler>()

    /**
     * The [TypeManager] for the compiler that is used for registering and looking up types.
     */
    public val types: TypeManager = TypeManager()

    /**
     * The [TriggerManager] for the compiler that is used for registering and looking up script triggers.
     */
    public val triggers: TriggerManager = TriggerManager()

    /**
     * Called after every step with all diagnostics that were collected during it.
     */
    public var diagnosticsHandler: DiagnosticsHandler = DEFAULT_DIAGNOSTICS_HANDLER

    init {
        // set up the incremental file path from environmental variable
        val incrementalPathEnv: String? = System.getenv("INCREMENTAL_PATH")
        incrementalPath = if (incrementalPathEnv != null) Path(incrementalPathEnv).absolute() else null

        // register the core types
        types.registerAll<PrimitiveType>()
        setupDefaultTypeCheckers()

        // register the command trigger
        triggers.register(CommandTrigger)
    }

    /**
     * Adds the core type checkers that the compiler depends on.
     */
    private fun setupDefaultTypeCheckers() {
        // allow anything to be assigned to any
        types.addTypeChecker { left, right -> left == MetaType.Any || right == MetaType.Any }

        // allow anything to be assigned to error to prevent error propagation
        types.addTypeChecker { left, right -> left == MetaType.Error || right == MetaType.Error }

        // basic checker where both types are equal
        types.addTypeChecker { left, right -> left == right }

        // checker for WrappedType that compares the inner types
        types.addTypeChecker { left, right ->
            left is WrappedType && right is WrappedType && left::class == right::class &&
                types.check(left.inner, right.inner)
        }
    }

    /**
     * Adds [loader] to the list of symbol loaders to run pre-compilation. This
     * can be used to load external symbols outside of scripts.
     */
    public fun addSymbolLoader(loader: SymbolLoader) {
        symbolLoaders += loader
    }

    /**
     * Adds a [DynamicCommandHandler] to the compiler with the given [name]. See
     * [DynamicCommandHandler] for information on implementation.
     *
     * If a handler was registered for the [name] already an error is thrown.
     */
    public fun addDynamicCommandHandler(name: String, handler: DynamicCommandHandler) {
        val existing = dynamicCommandHandlers.putIfAbsent(name, handler)
        if (existing != null) {
            error("A dynamic command handler with the name of '$name' already exists.")
        }
    }

    /**
     * Runs the compiler by loading external symbols and then actually running
     * the compile process.
     */
    public fun run() {
        loadSymbols()
        loadIncrementalData()
        compile()
    }

    /**
     * Calls all [SymbolLoader]s added to the compiler.
     */
    private fun loadSymbols() {
        for (symbolLoader in symbolLoaders) {
            symbolLoader.run {
                rootTable.load(this@ScriptCompiler)
            }
        }
        // logger.info { "Loaded 1234 symbols." }
    }

    /**
     * Loads the incremental data if the path is specified and the file exists.
     */
    private fun loadIncrementalData() {
        if (incrementalPath == null || incrementalPath.notExists()) {
            return
        }

        logger.info { "Loading incremental data from $incrementalPath." }
        val inputStream = incrementalPath.inputStream().buffered()
        val incrementalData = inputStream.use { IncrementalData.unpack(it) }
        if (incrementalData != null) {
            logger.info { "Loaded incremental data for ${incrementalData.files.size} files." }
        } else {
            logger.info { "Failed to load incremental data." }
        }
        this.incrementalData = incrementalData
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

        // 5) Write build info for incremental compilation
        writeBuildInfo(fileNodes)
    }

    /**
     * Parses all files in the source path and returns the parsed AST nodes.
     */
    private fun parse(): Pair<Boolean, List<ScriptFile>> {
        val diagnostics = Diagnostics()

        logger.info { "Parsing files in $sourcePath" }

        // find all files in source directory
        var files = sourcePath.toFile().walkTopDown()
            .filter { !it.isDirectory && it.extension == "cs2" }
            .map { it.toPath() }
            .toList()

        // narrow down files if incremental file exists
        if (incrementalData != null) {
            files = processIncrementalData(files)
        }

        // iterate over all files and parse them
        val fileNodes = mutableListOf<ScriptFile>()
        val time = measureTimeMillis {
            for (file in files) {
                val time = measureTimeMillis {
                    val errorListener = ParserErrorListener(file.absolutePathString(), diagnostics)
                    val node = ScriptParser.createScriptFile(file, errorListener)
                    if (node != null) {
                        fileNodes += node
                    }
                }
                logger.trace { "Parsed $file in ${time}ms" }
            }
        }
        logger.info { "Parsed ${files.size} files in ${time}ms" }

        // call the diagnostics handler
        with(diagnosticsHandler) {
            diagnostics.handleParse()
        }
        return !diagnostics.hasErrors() to fileNodes
    }

    /**
     * Handles figuring out which files were added, deleted, or modified. Any files that were
     * determined to not be modified will load a small portion of the script so references can
     * be resolved.
     */
    private fun processIncrementalData(all: List<Path>): List<Path> {
        // if incremental data hasn't been loaded yet just parse everything in the list
        val incrementalData = incrementalData ?: return all

        // figure out which files need to be parsed
        val fullFilesToParse = hashSetOf<String>()
        val partialFilesToParse = hashSetOf<IncrementalFile>()
        val time = measureTimeMillis {
            logger.debug { "Computing modified files." }
            val nameToFile = incrementalData.files.associateByTo(HashMap()) { it.name }
            for (file in all) {
                // normalize the path to be relative to the source path
                val relativeFile = file.relativeTo(sourcePath).toString()
                val incrementalFile = nameToFile.remove(relativeFile)
                if (incrementalFile == null) {
                    // file added
                    fullFilesToParse += relativeFile
                    // logger.trace { "New file $relativeFile." }
                    continue
                }

                // check for changes using file size and last modified time
                val attributes = file.readAttributes<BasicFileAttributes>()
                val curSize = attributes.size()
                val curLastModified = attributes.lastModifiedTime().toMillis()
                val (previousSize, previousLastModified) = incrementalFile.meta
                if (curSize != previousSize || curLastModified != previousLastModified) {
                    // file modified, add file and direct dependents
                    fullFilesToParse += relativeFile
                    fullFilesToParse += incrementalFile.dependents
                    // logger.trace { "Modified file $relativeFile." }
                    continue
                }
                // logger.trace { "Unmodified file $relativeFile." }
                partialFilesToParse += incrementalFile
            }
            // remaining things in nameToFile were delete
        }
        logger.debug { "Computed modified files in ${time}ms." }

        // remove all files that are needing a full parse
        partialFilesToParse.removeIf { it.name in fullFilesToParse }

        logger.debug { "Processing previous build data." }
        // iterate over all the remaining partial files and parse them
        val diagnostics = Diagnostics()
        val parseTime = measureTimeMillis {
            for (file in partialFilesToParse) {
                parseAndCheckIncrementalFile(file, diagnostics)
            }
        }
        logger.debug { "Processed previous build data for ${partialFilesToParse.size} files in ${parseTime}ms." }

        // verify there were no errors during parsing or checking previous build scripts
        require(!diagnostics.hasErrors())

        return fullFilesToParse.map { sourcePath.resolve(it) }
    }

    /**
     * Parses [file] and runs it through [PreTypeChecking] to verify the header and insert it into
     * the symbol table.
     */
    private fun parseAndCheckIncrementalFile(file: IncrementalFile, diagnostics: Diagnostics) {
        // the checker that inserts script symbols to the symbol table
        val checker = PreTypeChecking(types, triggers, rootTable, diagnostics)

        val filePath = sourcePath.resolve(file.name).toString()
        val errorListener = ParserErrorListener(filePath, diagnostics)
        val contents = file.scripts.joinToString("")
        val scriptFile = ScriptParser.createScriptFile(contents, errorListener) ?: return

        // Note: We do the checking here so that we don't need to add the file to the main list of file
        //       nodes that will be passed through other stages of the compiler. The only stage we care
        //       for is pre-type checking since it is what inserts the script symbol so references will
        //       be resolvable to scripts that are being skipped.
        scriptFile.accept(checker)
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
                    file.accept(PreTypeChecking(types, triggers, rootTable, diagnostics))
                }
                logger.trace { "Pre-type checked ${file.source.name} in ${time}ms" }
            }
        }
        logger.debug { "Finished pre-type checking in ${preTypeCheckingTime}ms" }

        // type check: this does all major type checking
        logger.debug { "Starting type checking" }
        val typeCheckingTime = measureTimeMillis {
            for (file in files) {
                val time = measureTimeMillis {
                    file.accept(TypeChecking(types, triggers, rootTable, dynamicCommandHandlers, diagnostics))
                }
                logger.trace { "Type checked ${file.source.name} in ${time}ms" }
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
                    val codegen = CodeGenerator(types, dynamicCommandHandlers, diagnostics)
                    file.accept(codegen)
                    scripts.addAll(codegen.scripts)
                }
                logger.trace { "Generated code for ${file.source.name} in ${time}ms" }
            }
        }
        logger.debug { "Finished codegen in ${codegenTime}ms" }

        // call the diagnostics handler
        with(diagnosticsHandler) {
            diagnostics.handleCodeGeneration()
        }

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

    /**
     * Writes the build information to disk if a path is specified.
     */
    private fun writeBuildInfo(fileNodes: List<ScriptFile>) {
        if (incrementalPath == null) {
            return
        }

        val buildInfo = createBuildInfo(fileNodes) ?: return
        incrementalPath.outputStream().buffered().use {
            buildInfo.pack(it)
        }
    }

    /**
     * Creates [IncrementalData] for the current build, merging any information
     * possible from previous data if it exists.
     */
    private fun createBuildInfo(fileNodes: List<ScriptFile>): IncrementalData? {
        // early return if there is no data to save
        if (fileNodes.isEmpty()) {
            return null
        }

        val scriptHeaderGenerator = ScriptHeaderGenerator()

        val files = mutableListOf<IncrementalFile>()
        val fileDependents = HashMap<String, HashSet<String>>(fileNodes.size)
        val symbolToPath = HashMap<ScriptSymbol, String>(fileNodes.size)
        var headerTimer = 0L
        for (fileNode in fileNodes) {
            val path = Path(fileNode.source.name)
            val relativePath = path.relativeTo(sourcePath).toString()
            val scriptHeaders = ArrayList<String>(fileNode.scripts.size)
            for (script in fileNode.scripts) {
                val time = measureTimeMillis {
                    scriptHeaders += script.accept(scriptHeaderGenerator)
                }
                headerTimer += time
                symbolToPath[script.symbol] = relativePath
            }

            val attributes = path.readAttributes<BasicFileAttributes>()
            val fileMeta = IncrementalFile.MetaData(attributes.size(), attributes.lastModifiedTime().toMillis())
            val dependents = hashSetOf<String>()
            files += IncrementalFile(relativePath, fileMeta, scriptHeaders, dependents)
            fileDependents[relativePath] = dependents
        }

        // inverse dependencies
        for (fileNode in fileNodes) {
            val path = Path(fileNode.source.name).relativeTo(sourcePath).toString()
            val dependencies = fileNode.dependencies

            for (dependency in dependencies) {
                val dependencyFile = symbolToPath[dependency] ?: continue
                if (dependencyFile == path) {
                    continue
                }

                val dependents = fileDependents[dependencyFile] ?: error(dependencyFile)
                dependents += path
            }
        }

        // merge existing data for unchanged files
        val incrementalData = incrementalData
        if (incrementalData != null) {
            for (file in incrementalData.files) {
                if (file.name in fileDependents) {
                    // file was modified
                    continue
                }

                files += file
            }
        }
        return IncrementalData(files)
    }

    public companion object {
        public val DEFAULT_DIAGNOSTICS_HANDLER: DiagnosticsHandler = DiagnosticsHandler.BaseDiagnosticsHandler()
    }
}
