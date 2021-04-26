package me.filby.neptune.runescript.ast

import me.filby.neptune.runescript.ast.expr.BooleanLiteral
import me.filby.neptune.runescript.ast.expr.IntegerLiteral
import me.filby.neptune.runescript.ast.expr.Literal

public interface AstVisitor<R> {

    public fun visitScriptFile(scriptFile: ScriptFile): R {
        return visitNode(scriptFile)
    }

    public fun visitScript(script: Script): R {
        return visitNode(script)
    }

    public fun visitIntegerLiteral(integerLiteral: IntegerLiteral): R {
        return visitLiteral(integerLiteral)
    }

    public fun visitBooleanLiteral(booleanLiteral: BooleanLiteral): R {
        return visitLiteral(booleanLiteral)
    }

    public fun visitLiteral(literal: Literal<*>): R {
        return visitNode(literal)
    }

    public fun visitIdentifier(identifier: Identifier): R {
        return visitNode(identifier)
    }

    public fun visitNode(node: Node): R {
        throw UnsupportedOperationException("not implemented: $node")
    }

}
