package me.filby.neptune.runescript.compiler.incremental

import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.expr.ClientScriptExpression
import me.filby.neptune.runescript.ast.expr.ProcCallExpression
import me.filby.neptune.runescript.ast.expr.StringLiteral
import me.filby.neptune.runescript.compiler.reference
import me.filby.neptune.runescript.compiler.subExpression
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol

public class ScriptFileDependencyCollector : AstVisitor<Set<ScriptSymbol>> {
    override fun visitProcCallExpression(procCallExpression: ProcCallExpression): Set<ScriptSymbol> =
        buildSet {
            val reference = procCallExpression.reference
            if (reference is ScriptSymbol) {
                add(reference)
            }
            for (child in procCallExpression.children) {
                addAll(child.accept(this@ScriptFileDependencyCollector))
            }
        }

    override fun visitClientScriptExpression(clientScriptExpression: ClientScriptExpression): Set<ScriptSymbol> =
        buildSet {
            val reference = clientScriptExpression.reference
            if (reference is ScriptSymbol) {
                add(reference)
            }
            for (child in clientScriptExpression.children) {
                addAll(child.accept(this@ScriptFileDependencyCollector))
            }
        }

    override fun visitStringLiteral(stringLiteral: StringLiteral): Set<ScriptSymbol> =
        buildSet {
            val subExpression = stringLiteral.subExpression
            if (subExpression != null) {
                addAll(subExpression.accept(this@ScriptFileDependencyCollector))
            }
        }

    override fun visitNode(node: Node): Set<ScriptSymbol> {
        return buildSet {
            for (child in node.children) {
                addAll(child.accept(this@ScriptFileDependencyCollector))
            }
        }
    }
}
