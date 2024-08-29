package me.filby.neptune.clientscript.compiler.configuration

/**
 * Main compiler configuration holder.
 */
data class ClientScriptCompilerConfig(
    val name: String = "neptune",
    val sourcePaths: List<String> = listOf("src/"),
    val symbolPaths: List<String> = listOf("symbols/"),
    val libraryPaths: List<String> = emptyList(),
    val excludePaths: List<String> = emptyList(),
    val writers: ClientScriptWriterConfig = ClientScriptWriterConfig(),
)
