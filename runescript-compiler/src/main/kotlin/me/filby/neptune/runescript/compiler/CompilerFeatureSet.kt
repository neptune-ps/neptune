package me.filby.neptune.runescript.compiler

/**
 * Configuration for the compiler features.
 */
public data class CompilerFeatureSet(
    val prefixExpressions: Boolean = false,
    val postfixExpressions: Boolean = false,
)
