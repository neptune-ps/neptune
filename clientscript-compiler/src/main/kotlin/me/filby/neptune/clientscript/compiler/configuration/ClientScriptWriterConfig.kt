package me.filby.neptune.clientscript.compiler.configuration

import me.filby.neptune.clientscript.compiler.writer.BinaryFileScriptWriter

/**
 * Container for different script writers.
 */
data class ClientScriptWriterConfig(val binary: BinaryFileWriterConfig? = null)

/**
 * Configuration for [BinaryFileScriptWriter].
 */
data class BinaryFileWriterConfig(val outputPath: String)
