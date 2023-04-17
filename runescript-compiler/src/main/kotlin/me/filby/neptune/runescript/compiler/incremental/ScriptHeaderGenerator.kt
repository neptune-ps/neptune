package me.filby.neptune.runescript.compiler.incremental

import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.Script

internal class ScriptHeaderGenerator : AstVisitor<String> {
    override fun visitScript(script: Script) = buildString {
        append("[")
        append(script.trigger.text)
        append(",")
        append(script.name.text)
        append("]")

        val parameters = script.parameters
        if (parameters != null) {
            append("(")
            for ((i, parameter) in parameters.withIndex()) {
                append(parameter.typeToken.text)
                append(" \$")
                append(parameter.name.text)
                if (i < parameters.size - 1) {
                    append(", ")
                }
            }
            append(")")
        }

        // returns
        val returns = script.returnTokens
        if (returns != null) {
            append("(")
            for ((i, ret) in returns.withIndex()) {
                append(ret.text)
                if (i < returns.size - 1) {
                    append(",")
                }
            }
            append(")")
        }
    }
}
