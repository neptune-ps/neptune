package me.filby.neptune.runescript.compiler.semantics

import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.ScriptFile
import me.filby.neptune.runescript.ast.Token
import me.filby.neptune.runescript.ast.expr.BinaryExpression
import me.filby.neptune.runescript.ast.expr.BooleanLiteral
import me.filby.neptune.runescript.ast.expr.CalcExpression
import me.filby.neptune.runescript.ast.expr.CharacterLiteral
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.expr.IntegerLiteral
import me.filby.neptune.runescript.ast.expr.JoinedStringExpression
import me.filby.neptune.runescript.ast.expr.LocalVariableExpression
import me.filby.neptune.runescript.ast.expr.NullLiteral
import me.filby.neptune.runescript.ast.expr.StringLiteral
import me.filby.neptune.runescript.ast.statement.ArrayDeclarationStatement
import me.filby.neptune.runescript.ast.statement.AssignmentStatement
import me.filby.neptune.runescript.ast.statement.BlockStatement
import me.filby.neptune.runescript.ast.statement.DeclarationStatement
import me.filby.neptune.runescript.ast.statement.ExpressionStatement
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
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.ArrayType
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type
import me.filby.neptune.runescript.compiler.typeHint

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

    override fun visitBlockStatement(blockStatement: BlockStatement) {
        // visit all statements
        blockStatement.statements.visit()
    }

    override fun visitDeclarationStatement(declarationStatement: DeclarationStatement) {
        val initializer = declarationStatement.initializer
        if (initializer != null) {
            val symbol = declarationStatement.symbol

            // type hint that we want whatever the declarations type is then visit
            initializer.typeHint = symbol.type
            initializer.visit()

            checkTypeMatch(initializer, symbol.type, initializer.type)
        }
    }

    override fun visitArrayDeclarationStatement(arrayDeclarationStatement: ArrayDeclarationStatement) {
        val initializer = arrayDeclarationStatement.initializer
        initializer.visit()
        checkTypeMatch(initializer, PrimitiveType.INT, initializer.type)
    }

    override fun visitAssignmentStatement(assignmentStatement: AssignmentStatement) {
        // visit the lhs to fetch the references
        assignmentStatement.vars.visit()

        // store the lhs types to help with type hinting
        val leftTypes = assignmentStatement.vars.map { it.type }
        val rightTypes = mutableListOf<Type>()
        var typeCounter = 0
        for (expr in assignmentStatement.expressions) {
            expr.typeHint = if (typeCounter < leftTypes.size) leftTypes[typeCounter] else null
            expr.visit()

            // add the evaluated type
            rightTypes += expr.type

            // increment the counter for type hinting
            typeCounter += if (expr.type is TupleType) {
                (expr.type as TupleType).children.size
            } else {
                1
            }
        }

        // convert types to tuple type if necessary for easy comparison
        val leftType = TupleType.fromList(leftTypes)
        val rightType = TupleType.fromList(rightTypes)
        if (leftType == null || rightType == null) {
            assignmentStatement.reportError(
                DiagnosticMessage.NULL_TYPE_IN_ASSIGNMENT,
                leftType?.representation ?: "null",
                rightType?.representation ?: "null"
            )
            return
        }

        checkTypeMatch(assignmentStatement, leftType, rightType)
    }

    override fun visitExpressionStatement(expressionStatement: ExpressionStatement) {
        // just visit the inside expression
        expressionStatement.expression.visit()
    }

    override fun visitBinaryExpression(binaryExpression: BinaryExpression) {
        // hint should be either int for `calc` or boolean for conditions
        val typeHint = binaryExpression.typeHint

        // binary expressions are syntactically only possible within calc and a condition,
        // which requires with an int or boolean.
        if (typeHint != PrimitiveType.INT && typeHint != PrimitiveType.BOOLEAN) {
            binaryExpression.type = PrimitiveType.UNDEFINED
            binaryExpression.reportError(DiagnosticMessage.INVALID_BINARYEXPR_TYPEHINT, typeHint ?: "null")
            return
        }

        val left = binaryExpression.left
        val right = binaryExpression.right
        val operator = binaryExpression.operator

        if (typeHint == PrimitiveType.INT && !checkBinaryMathOperation(binaryExpression, left, operator, right)) {
            // math operation failed so, error is reported in checkBinaryMathOperation
            binaryExpression.type = PrimitiveType.UNDEFINED
            return
        } else if (typeHint == PrimitiveType.BOOLEAN) {
            binaryExpression.type = PrimitiveType.UNDEFINED
            binaryExpression.reportError("Conditional binary expressions are not implemented.")
            return
        }

        binaryExpression.type = typeHint
    }

    /**
     * Verifies the binary expression is a valid math operation.
     */
    private fun checkBinaryMathOperation(
        parent: BinaryExpression,
        left: Expression,
        operator: String,
        right: Expression
    ): Boolean {
        if (operator !in MATH_OPS) {
            // parent _should_ be calc here, and we want to report error on it since the
            // operation is not a Token currently
            parent.reportError(DiagnosticMessage.INVALID_MATHOP, operator)
            return false
        }

        // visit left-hand side
        left.typeHint = PrimitiveType.INT
        left.visit()

        // visit right-hand side
        right.typeHint = PrimitiveType.INT
        right.visit()

        // verify if both sides are ints
        var bothMatch = checkTypeMatch(left, PrimitiveType.INT, left.type, reportError = false)
        bothMatch = bothMatch and checkTypeMatch(right, PrimitiveType.INT, right.type, reportError = false)

        // one or both don't match so report an error
        if (!bothMatch) {
            // TODO make operator a token so we can point to it in an error message
            parent.reportError(
                DiagnosticMessage.BINOP_INVALID_TYPES,
                operator,
                left.type.representation,
                right.type.representation
            )
        }

        return bothMatch
    }

    override fun visitCalcExpression(calcExpression: CalcExpression) {
        val innerExpression = calcExpression.expression

        // hint to the expression that we expect an int
        innerExpression.typeHint = PrimitiveType.INT
        innerExpression.visit()

        // verify type is an int
        if (!checkTypeMatch(innerExpression, PrimitiveType.INT, innerExpression.type)) {
            calcExpression.type = PrimitiveType.UNDEFINED
            return
        }

        calcExpression.type = PrimitiveType.INT
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

        val name = identifier.text
        var hint: Type? = identifier.typeHint

        // assume component if the name contains a colon
        if (hint == null && identifier.text.contains(":")) {
            hint = PrimitiveType.COMPONENT
        }

        // look through the global table for a symbol with the given name and type
        var symbol: Symbol? = null
        var symbolType: Type? = null
        for (temp in rootTable.findAll<Symbol>(name)) {
            symbolType = symbolToType(temp)
            if (hint == null || symbolType != null && isTypeCompatible(hint, symbolType)) {
                // hint type matches (or is null) so we can stop looking
                symbol = temp
                break
            } else if (symbol == null) {
                // default the symbol to the first thing found just in case
                // no exact matches exist
                symbol = temp
            }
        }

        if (symbol == null) {
            // unable to resolve the symbol
            identifier.type = PrimitiveType.UNDEFINED
            identifier.reportError(DiagnosticMessage.GENERIC_UNRESOLVED_SYMBOL, name)
            return
        }

        // identifier.reportInfo("hint=%s, symbol=%s", hint?.representation ?: "null", symbol)

        // compiler error if the symbol type isn't defined here
        if (symbolType == null) {
            identifier.type = PrimitiveType.UNDEFINED
            identifier.reportError(DiagnosticMessage.UNSUPPORTED_SYMBOLTYPE_TO_TYPE, symbol::class.java.simpleName)
            return
        }

        identifier.reference = symbol
        identifier.type = symbolType
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
     * Checks if the [expected] and [actual] match, including accepted casting.
     *
     * If the types passed in are a [TupleType] they will be compared using their flattened types.
     *
     * @see isTypeCompatible
     */
    private fun checkTypeMatch(node: Node, expected: Type, actual: Type, reportError: Boolean = true): Boolean {
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

        if (!match && reportError) {
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
        if (first == PrimitiveType.OBJ) {
            // allow assigning namedobj to obj but not obj to namedobj
            return second == PrimitiveType.OBJ || second == PrimitiveType.NAMEDOBJ
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

    private companion object {
        /**
         * Array of valid math operations allowed within `calc` expressions.
         */
        private val MATH_OPS = arrayOf(
            "*", "/", "%",
            "+", "-",
            "&", "|"
        )
    }
}
