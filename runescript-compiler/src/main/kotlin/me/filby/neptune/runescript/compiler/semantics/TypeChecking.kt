package me.filby.neptune.runescript.compiler.semantics

import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.ScriptFile
import me.filby.neptune.runescript.ast.Token
import me.filby.neptune.runescript.ast.expr.BooleanLiteral
import me.filby.neptune.runescript.ast.expr.CharacterLiteral
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.expr.IntegerLiteral
import me.filby.neptune.runescript.ast.expr.JoinedStringExpression
import me.filby.neptune.runescript.ast.expr.LocalVariableExpression
import me.filby.neptune.runescript.ast.expr.NullLiteral
import me.filby.neptune.runescript.ast.expr.StringLiteral
import me.filby.neptune.runescript.ast.statement.ArrayDeclarationStatement
import me.filby.neptune.runescript.ast.statement.DeclarationStatement
import me.filby.neptune.runescript.ast.statement.Statement
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostic
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticMessage
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticType
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostics
import me.filby.neptune.runescript.compiler.reference
import me.filby.neptune.runescript.compiler.symbol
import me.filby.neptune.runescript.compiler.symbol.ClientScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.ComponentSymbol
import me.filby.neptune.runescript.compiler.symbol.ConfigSymbol
import me.filby.neptune.runescript.compiler.symbol.LocalVariableSymbol
import me.filby.neptune.runescript.compiler.symbol.ServerScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.Symbol
import me.filby.neptune.runescript.compiler.symbol.SymbolTable
import me.filby.neptune.runescript.compiler.symbol.SymbolType
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.ArrayType
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type

internal class TypeChecking(
    private val rootTable: SymbolTable,
    private val diagnostics: Diagnostics
) : AstVisitor<Unit> {
    override fun visitScriptFile(scriptFile: ScriptFile) {
        // visit all scripts in the file
        scriptFile.scripts.visit()
    }

    override fun visitScript(script: Script) {
        // visit all statements, we don't need to do anything else with the script
        // since all the other stuff is handled in pre-type checking.
        script.statements.visit()
    }

    override fun visitDeclarationStatement(declarationStatement: DeclarationStatement) {
        val initializer = declarationStatement.initializer
        if (initializer != null) {
            val symbol = declarationStatement.symbol
            initializer.visit()
            checkTypeMatch(initializer, symbol.type, initializer.type)
        }
    }

    override fun visitArrayDeclarationStatement(arrayDeclarationStatement: ArrayDeclarationStatement) {
        val initializer = arrayDeclarationStatement.initializer
        initializer.visit()
        checkTypeMatch(initializer, PrimitiveType.INT, initializer.type)
    }

    override fun visitStatement(statement: Statement) {
        // TODO temporarily visit all statements
        statement.children.visit()
    }

    /**
     * Type checks the index value expression if it is defined.
     */
    override fun visitLocalVariableExpression(localVariableExpression: LocalVariableExpression) {
        // if the reference is null, that means it failed to find the symbol associated with it
        // in pre-type checking and would have already been reported.
        val reference = localVariableExpression.reference ?: return

        val indexExpression = localVariableExpression.index
        if (reference.type is ArrayType && indexExpression != null) {
            indexExpression.visit()
            checkTypeMatch(indexExpression, PrimitiveType.INT, indexExpression.type)
        }

        // type is set in PreTypeChecking
    }

    override fun visitIntegerLiteral(integerLiteral: IntegerLiteral) {
        integerLiteral.type = PrimitiveType.INT
    }

    override fun visitBooleanLiteral(booleanLiteral: BooleanLiteral) {
        booleanLiteral.type = PrimitiveType.BOOLEAN
    }

    override fun visitCharacterLiteral(characterLiteral: CharacterLiteral) {
        characterLiteral.type = PrimitiveType.CHAR
    }

    override fun visitNullLiteral(nullLiteral: NullLiteral) {
        nullLiteral.type = PrimitiveType.NULL
    }

    override fun visitStringLiteral(stringLiteral: StringLiteral) {
        // TODO support for specifying type as graphic
        stringLiteral.type = PrimitiveType.STRING
    }

    override fun visitJoinedStringExpression(joinedStringExpression: JoinedStringExpression) {
        // visit the parts and verify they're all strings
        joinedStringExpression.parts.visit()
        for (part in joinedStringExpression.parts) {
            checkTypeMatch(part, PrimitiveType.STRING, part.type)
        }

        joinedStringExpression.type = PrimitiveType.STRING
    }

    override fun visitIdentifier(identifier: Identifier) {
        if (identifier.reference != null) {
            // handled already in pre-type checking for array references.
            return
        }

        // TODO lookup command and fall through to the rest of the code if it doesn't exist

        val name = identifier.text
        var hint: Type? = null
        var hintSymbolType: SymbolType<*>? = null

        // assume component if the name contains a colon
        if (identifier.text.contains(":")) {
            hint = PrimitiveType.COMPONENT
            hintSymbolType = SymbolType.Component
        }

        // TODO make global lookup smarter somehow?
        val symbol = if (hintSymbolType == null) {
            rootTable.findAll<Symbol>(name).firstOrNull()
        } else {
            rootTable.find(hintSymbolType, name)
        }

        if (symbol == null) {
            // unable to resolve the symbol
            identifier.reportError("'%s' could not be resolved to a symbol.", name)
            identifier.type = PrimitiveType.UNDEFINED
            return
        }

        // try to convert the symbol into a type that can be stored on the node
        val typeFromSymbol = symbolToType(symbol)
        if (typeFromSymbol == null) {
            identifier.type = PrimitiveType.UNDEFINED
            identifier.reportError(DiagnosticMessage.UNSUPPORTED_SYMBOLTYPE_TO_TYPE, symbol::class.java.simpleName)
            return
        }

        identifier.reference = symbol
        identifier.type = typeFromSymbol
    }

    override fun visitNode(node: Node) {
        if (node !is Token) {
            val parent = node.parent
            if (parent == null) {
                node.reportInfo("Unhandled node: %s.", node::class.simpleName!!)
            } else {
                node.reportInfo("Unhandled node: %s. Parent: %s", node::class.simpleName!!, parent::class.simpleName!!)
            }
        }
    }

    /**
     * Converts a [Symbol] to its equivalent [Type].
     */
    private fun symbolToType(symbol: Symbol) = when (symbol) {
        is ServerScriptSymbol -> null
        is ClientScriptSymbol -> null
        is LocalVariableSymbol -> symbol.type
        is ComponentSymbol -> PrimitiveType.COMPONENT
        is ConfigSymbol -> symbol.type
    }

    /**
     * Checks if the [expected] and [actual] match, including accepted casting.
     *
     * If the types passed in are a [TupleType] they will be compared using their flattened types.
     *
     * @see isTypeCompatible
     */
    private fun checkTypeMatch(node: Node, expected: Type, actual: Type): Boolean {
        val expectedFlattened = if (expected is TupleType) expected.children else arrayOf(expected)
        val actualFlattened = if (actual is TupleType) actual.children else arrayOf(actual)

        var match = true
        if (expectedFlattened.size != actualFlattened.size) {
            match = false
        } else {
            for (i in expectedFlattened.indices) {
                match = match and isTypeCompatible(expectedFlattened[i], actualFlattened[i])
            }
        }

        if (!match) {
            node.reportError(DiagnosticMessage.GENERIC_TYPE_MISMATCH, actual.representation, expected.representation)
        }
        return match
    }

    /**
     * Checks if the two types are compatible in other ways than equality. All types
     * are compatible with `undefined` type to help prevent error propagation.
     *
     * Example: `graphic`s can be assigned to `fontmetrics`.
     */
    private fun isTypeCompatible(first: Type, second: Type): Boolean {
        if (first == PrimitiveType.UNDEFINED || second == PrimitiveType.UNDEFINED) {
            // allow undefined to be compatible with anything to prevent error propagation
            return true
        }
        if (second == PrimitiveType.NULL) {
            // any int based variable is nullable
            // TODO ability to configure this per type?
            return first.baseType == BaseVarType.INTEGER
        }
        return first == second
    }

    /**
     * Helper function to report a diagnostic with the type of [DiagnosticType.INFO].
     */
    private fun Node.reportInfo(message: String, vararg args: Any) {
        diagnostics.report(Diagnostic(DiagnosticType.INFO, this, message, *args))
    }

    /**
     * Helper function to report a diagnostic with the type of [DiagnosticType.WARNING].
     */
    private fun Node.reportWarning(message: String, vararg args: Any) {
        diagnostics.report(Diagnostic(DiagnosticType.WARNING, this, message, *args))
    }

    /**
     * Helper function to report a diagnostic with the type of [DiagnosticType.ERROR].
     */
    private fun Node.reportError(message: String, vararg args: Any) {
        diagnostics.report(Diagnostic(DiagnosticType.ERROR, this, message, *args))
    }

    /**
     * Shortcut to [Node.accept] for nullable nodes.
     */
    private fun Node?.visit() {
        this ?: return
        accept(this@TypeChecking)
    }

    /**
     * Calls [Node.accept] on all nodes in a list.
     */
    private fun List<Node>?.visit() {
        this ?: return
        for (n in this) {
            n.visit()
        }
    }
}
