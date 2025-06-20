package me.filby.neptune.runescript.compiler

class TestCompilerFeatureSet : CompilerFeatureSet {
    override var prefixPostfixExpressions = false
    override var arraysV2 = false
    override val simplifiedTypeCodes: Boolean = false
}
