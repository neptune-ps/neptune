package me.filby.neptune.runescript.compiler.incremental

internal data class IncrementalFile(
    val name: String,
    val meta: MetaData,
    val scripts: List<String>,
    val dependents: Set<String>,
) {
    data class MetaData(val size: Long, val lastModified: Long)
}
