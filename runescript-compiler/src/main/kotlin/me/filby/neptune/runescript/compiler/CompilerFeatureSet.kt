package me.filby.neptune.runescript.compiler

/**
 * Configuration for the compiler features.
 */
public interface CompilerFeatureSet {
    public val prefixPostfixExpressions: Boolean
    public val arraysV2: Boolean
    public val simplifiedTypeCodes: Boolean
}
