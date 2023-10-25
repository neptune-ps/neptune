package me.filby.neptune.clientscript.compiler.configuration

/**
 * Main compiler configuration holder.
 */
data class ClientScriptCompilerConfig(
    val sourcePaths: List<String> = listOf("src/"),
    val symbolPaths: List<String> = listOf("symbols/"),
    val excludePaths: List<String> = emptyList(),
    val writers: ClientScriptWriterConfig = ClientScriptWriterConfig(),
)
