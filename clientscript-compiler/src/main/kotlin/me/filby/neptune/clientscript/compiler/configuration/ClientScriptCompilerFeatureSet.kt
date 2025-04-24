package me.filby.neptune.clientscript.compiler.configuration

import me.filby.neptune.runescript.compiler.CompilerFeatureSet

data class ClientScriptCompilerFeatureSet(
    val dbFindReturnsCount: Boolean,
    val ccCreateAssertNewArg: Boolean,
    override val prefixPostfixExpressions: Boolean,
) : CompilerFeatureSet
