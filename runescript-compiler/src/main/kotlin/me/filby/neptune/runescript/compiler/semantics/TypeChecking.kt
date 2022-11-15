package me.filby.neptune.runescript.compiler.semantics

import me.filby.neptune.runescript.antlr.RuneScriptParser
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.ScriptFile
import me.filby.neptune.runescript.ast.Token
import me.filby.neptune.runescript.ast.expr.BinaryExpression
import me.filby.neptune.runescript.ast.expr.BooleanLiteral
import me.filby.neptune.runescript.ast.expr.CalcExpression
import me.filby.neptune.runescript.ast.expr.CallExpression
import me.filby.neptune.runescript.ast.expr.CharacterLiteral
import me.filby.neptune.runescript.ast.expr.ClientScriptExpression
import me.filby.neptune.runescript.ast.expr.CommandCallExpression
import me.filby.neptune.runescript.ast.expr.ConstantVariableExpression
import me.filby.neptune.runescript.ast.expr.CoordLiteral
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.GameVariableExpression
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.expr.IntegerLiteral
import me.filby.neptune.runescript.ast.expr.JoinedStringExpression
import me.filby.neptune.runescript.ast.expr.JumpCallExpression
import me.filby.neptune.runescript.ast.expr.Literal
import me.filby.neptune.runescript.ast.expr.LocalVariableExpression
import me.filby.neptune.runescript.ast.expr.NullLiteral
import me.filby.neptune.runescript.ast.expr.ParenthesizedExpression
import me.filby.neptune.runescript.ast.expr.ProcCallExpression
import me.filby.neptune.runescript.ast.expr.StringLiteral
import me.filby.neptune.runescript.ast.statement.ArrayDeclarationStatement
import me.filby.neptune.runescript.ast.statement.AssignmentStatement
import me.filby.neptune.runescript.ast.statement.BlockStatement
import me.filby.neptune.runescript.ast.statement.DeclarationStatement
import me.filby.neptune.runescript.ast.statement.EmptyStatement
import me.filby.neptune.runescript.ast.statement.ExpressionStatement
import me.filby.neptune.runescript.ast.statement.IfStatement
import me.filby.neptune.runescript.ast.statement.ReturnStatement
import me.filby.neptune.runescript.ast.statement.SwitchCase
import me.filby.neptune.runescript.ast.statement.SwitchStatement
import me.filby.neptune.runescript.ast.statement.WhileStatement
import me.filby.neptune.runescript.compiler.ParserErrorListener
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.defaultCase
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostic
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticMessage
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticType
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostics
import me.filby.neptune.runescript.compiler.nullableType
import me.filby.neptune.runescript.compiler.reference
import me.filby.neptune.runescript.compiler.returnType
import me.filby.neptune.runescript.compiler.scope
import me.filby.neptune.runescript.compiler.subExpression
import me.filby.neptune.runescript.compiler.symbol
import me.filby.neptune.runescript.compiler.symbol.BasicSymbol
import me.filby.neptune.runescript.compiler.symbol.ConfigSymbol
import me.filby.neptune.runescript.compiler.symbol.ConstantSymbol
import me.filby.neptune.runescript.compiler.symbol.LocalVariableSymbol
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.Symbol
import me.filby.neptune.runescript.compiler.symbol.SymbolTable
import me.filby.neptune.runescript.compiler.symbol.SymbolType
import me.filby.neptune.runescript.compiler.trigger.ClientTriggerType
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type
import me.filby.neptune.runescript.compiler.type.TypeManager
import me.filby.neptune.runescript.compiler.type.wrapped.ArrayType
import me.filby.neptune.runescript.compiler.type.wrapped.GameVarType
import me.filby.neptune.runescript.compiler.typeHint
import me.filby.neptune.runescript.parser.ScriptParser
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

/**
 * An implementation of [AstVisitor] that implements all remaining semantic/type
 * checking required to safely build scripts. This implementation assumes [PreTypeChecking]
 * is run beforehand.
 */
public class TypeChecking(
    private val typeManager: TypeManager,
    private val rootTable: SymbolTable,
    private val dynamicCommands: MutableMap<String, DynamicCommandHandler>,
    private val diagnostics: Diagnostics
) : AstVisitor<Unit> {
    // var Expression.type: Type
    //     get() = getAttribute<Type>("type") ?: error("type not set")
    //     set(value) {
    //         putAttribute("type", value)
    //         reportInfo("Type resolved to: '%s'", value.representation)
    //     }

    /**
     * The current table. This is updated each time when entering a new script or block.
     */
    private var table: SymbolTable = rootTable

    /**
     * State for if we're currently within an expression that is meant to be
     * evaluated as a condition.
     */
    private var inCondition = false

    /**
     * State for if we're currently within a `calc` expression.
     */
    private var inCalc = false

    /**
     * A set of symbols that are currently being evaluated. Used to prevent re-entering
     * a constant and causing a stack overflow.
     */
    private val constantsBeingEvaluated = LinkedHashSet<Symbol>()

    /**
     * Sets the active [table] to [newTable] and runs [block] then sets [table] back to what it was originally.
     */
    private inline fun scoped(newTable: SymbolTable, block: () -> Unit) {
        val oldTable = table
        table = newTable
        block()
        table = oldTable
    }

    override fun visitScriptFile(scriptFile: ScriptFile) {
        // visit all scripts in the file
        scriptFile.scripts.visit()
    }

    override fun visitScript(script: Script) {
        scoped(script.scope) {
            // visit all statements, we don't need to do anything else with the script
            // since all the other stuff is handled in pre-type checking.
            script.statements.visit()
        }
    }

    override fun visitBlockStatement(blockStatement: BlockStatement) {
        scoped(blockStatement.scope) {
            // visit all statements
            blockStatement.statements.visit()
        }
    }

    override fun visitReturnStatement(returnStatement: ReturnStatement) {
        val script = returnStatement.findParentByType<Script>()
        if (script == null) {
            // a return statement should always be within a script, if not
            // then we have problems!
            returnStatement.reportError(DiagnosticMessage.RETURN_ORPHAN)
            return
        }

        // use the return types from the script node and get the types being returned
        val expectedTypes = TupleType.toList(script.returnType)
        val actualTypes = typeHintExpressionList(expectedTypes, returnStatement.expressions)

        // convert the types into a single type
        val expectedType = TupleType.fromList(expectedTypes)
        val actualType = TupleType.fromList(actualTypes)

        // type check
        checkTypeMatch(returnStatement, expectedType, actualType)
    }

    override fun visitIfStatement(ifStatement: IfStatement) {
        checkCondition(ifStatement.condition)
        ifStatement.thenStatement.visit()
        ifStatement.elseStatement?.visit()
    }

    override fun visitWhileStatement(whileStatement: WhileStatement) {
        checkCondition(whileStatement.condition)
        whileStatement.thenStatement.visit()
    }

    /**
     * Handles type hinting and visiting the expression, then checking if it is a valid conditional
     * expression. If the condition returns anything other than `boolean`, or is not a valid
     * condition expression, an error is emitted.
     */
    private fun checkCondition(expression: Expression) {
        // update state to inside condition
        inCondition = true

        // type hint and visit condition
        expression.typeHint = PrimitiveType.BOOLEAN

        // attempts to find the first expression that isn't a binary expression or parenthesis expression
        val invalidExpression = findInvalidConditionExpression(expression)
        if (invalidExpression == null) {
            // visit expression and type check it, we don't visit outside this because we don't want
            // to report type mismatches AND invalid conditions at the same time.
            expression.visit()
            checkTypeMatch(expression, PrimitiveType.BOOLEAN, expression.type)
        } else {
            // report invalid condition expression on the erroneous node.
            invalidExpression.reportError(DiagnosticMessage.CONDITION_INVALID_NODE_TYPE)
        }

        // update state to outside condition
        inCondition = false
    }

    /**
     * Finds the first [Expression] node in the tree that is not either a [BinaryExpression] or
     * [ParenthesizedExpression]. If `null` is returned then that means the whole tree is valid
     * is all valid conditional expressions.
     */
    private fun findInvalidConditionExpression(expression: Expression): Node? = when (expression) {
        is BinaryExpression -> if (expression.operator.text == "|" || expression.operator.text == "&") {
            // check the left side and return it if it isn't null, otherwise return the value
            // of the right side
            findInvalidConditionExpression(expression.left) ?: findInvalidConditionExpression(expression.right)
        } else {
            // all other operators are valid
            null
        }
        is ParenthesizedExpression -> findInvalidConditionExpression(expression.expression)
        else -> expression
    }

    override fun visitSwitchStatement(switchStatement: SwitchStatement) {
        val expectedType = switchStatement.type

        // type hint the condition and visit it
        val condition = switchStatement.condition
        condition.typeHint = expectedType
        condition.visit()
        checkTypeMatch(condition, expectedType, condition.type)

        // TODO check for duplicate case labels (other than default)
        // visit all the cases, cases will be type checked there.
        var defaultCase: SwitchCase? = null
        for (case in switchStatement.cases) {
            if (case.isDefault) {
                if (defaultCase == null) {
                    defaultCase = case
                } else {
                    case.reportError(DiagnosticMessage.SWITCH_DUPLICATE_DEFAULT)
                }
            }
            case.visit()
        }
        switchStatement.defaultCase = defaultCase
    }

    override fun visitSwitchCase(switchCase: SwitchCase) {
        val switchType = switchCase.findParentByType<SwitchStatement>()?.type
        if (switchType == null) {
            // the parent should always be a switch statement, if not we're in trouble...
            switchCase.reportError(DiagnosticMessage.CASE_WITHOUT_SWITCH)
            return
        }

        // visit the case keys
        for (key in switchCase.keys) {
            // type hint and visit so we can access more information in constant expression check
            key.typeHint = switchType
            key.visit()

            if (!isConstantExpression(key)) {
                key.reportError(DiagnosticMessage.SWITCH_CASE_NOT_CONSTANT)
                continue
            }

            // expression is a constant, now we need to verify the types match
            checkTypeMatch(key, switchType, key.type)
        }

        scoped(switchCase.scope) {
            // visit the statements
            switchCase.statements.visit()
        }
    }

    /**
     * Checks if the result of [expression] is known at compile time.
     */
    internal fun isConstantExpression(expression: Expression): Boolean = when (expression) {
        is ConstantVariableExpression -> true
        is StringLiteral -> {
            // we need to special case this since it's possible for a string literal to have been
            // transformed into another expression type (e.g. graphic or clientscript)
            val sub = expression.subExpression
            sub == null || isConstantExpression(sub)
        }
        is Literal<*> -> true
        is Identifier -> {
            val ref = expression.reference
            ref != null && isConstantSymbol(ref)
        }
        else -> false
    }

    /**
     * Checks if the value of [symbol] is known at compile time.
     */
    private fun isConstantSymbol(symbol: Symbol): Boolean = when (symbol) {
        is BasicSymbol -> true
        is ConstantSymbol -> true
        is ConfigSymbol -> true
        else -> false
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
        val rightTypes = typeHintExpressionList(leftTypes, assignmentStatement.expressions)

        // convert types to tuple type if necessary for easy comparison
        val leftType = TupleType.fromList(leftTypes)
        val rightType = TupleType.fromList(rightTypes)

        checkTypeMatch(assignmentStatement, leftType, rightType)
    }

    override fun visitExpressionStatement(expressionStatement: ExpressionStatement) {
        // just visit the inside expression
        expressionStatement.expression.visit()
    }

    override fun visitEmptyStatement(emptyStatement: EmptyStatement) {
        // NO-OP
    }

    override fun visitParenthesizedExpression(parenthesizedExpression: ParenthesizedExpression) {
        val innerExpression = parenthesizedExpression.expression

        // relay the type hint to the inner expression and visit it
        innerExpression.typeHint = parenthesizedExpression.typeHint
        innerExpression.visit()

        // set the type to the type of what the expression evaluates to
        parenthesizedExpression.type = innerExpression.type
    }

    override fun visitBinaryExpression(binaryExpression: BinaryExpression) {
        val left = binaryExpression.left
        val right = binaryExpression.right
        val operator = binaryExpression.operator

        // we should only ever be within a condition or within calc at this point
        if (!inCondition && !inCalc) {
            binaryExpression.type = MetaType.Error
            binaryExpression.reportError(DiagnosticMessage.INVALID_BINEXP_STATE)
            return
        }

        // check for validation based on if we're within calc or condition.
        val validOperation = if (inCalc) {
            checkBinaryMathOperation(binaryExpression.typeHint ?: PrimitiveType.INT, left, operator, right)
        } else {
            checkBinaryConditionOperation(left, operator, right)
        }

        // early return if it isn't a valid operation
        if (!validOperation) {
            binaryExpression.type = MetaType.Error
            return
        }

        // conditions expect boolean, calc expects int/long
        // we shouldn't get to this point without one of those two being true due to the above check
        binaryExpression.type = if (inCalc) {
            binaryExpression.typeHint ?: PrimitiveType.INT
        } else {
            PrimitiveType.BOOLEAN
        }
    }

    /**
     * Verifies the binary expression is a valid math operation.
     */
    private fun checkBinaryMathOperation(
        typeHint: Type,
        left: Expression,
        operator: Token,
        right: Expression
    ): Boolean {
        if (operator.text !in MATH_OPS) {
            operator.reportError(DiagnosticMessage.INVALID_MATHOP, operator)
            return false
        }

        // visit left-hand side
        left.typeHint = typeHint
        left.visit()

        // visit right-hand side
        right.typeHint = typeHint
        right.visit()

        // verify if both sides are ints
        var bothMatch = checkTypeMatch(left, typeHint, left.type, reportError = false)
        bothMatch = bothMatch and checkTypeMatch(right, typeHint, right.type, reportError = false)

        // one or both don't match so report an error
        if (!bothMatch) {
            operator.reportError(
                DiagnosticMessage.BINOP_INVALID_TYPES,
                operator.text,
                left.type.representation,
                right.type.representation
            )
        }

        return bothMatch
    }

    /**
     * Verifies the binary expression is a valid condition operation.
     */
    private fun checkBinaryConditionOperation(
        left: Expression,
        operator: Token,
        right: Expression
    ): Boolean {
        if (operator.text !in CONDITIONAL_OPS) {
            operator.reportError(DiagnosticMessage.INVALID_CONDITIONOP, operator)
            return false
        }

        // some operators expect a specific type on both sides, specify those type(s) here
        val allowedTypes = when (operator.text) {
            "&", "|" -> arrayOf(PrimitiveType.BOOLEAN)
            "<", ">", "<=", ">=" -> arrayOf(PrimitiveType.INT, PrimitiveType.LONG)
            else -> null
        }

        // if required type is set we should type hint with those, otherwise use the opposite
        // sides type as a hint.
        if (allowedTypes != null) {
            left.typeHint = allowedTypes.first()
            right.typeHint = allowedTypes.first()
        } else {
            // assign the type hints using the opposite side if it isn't already assigned.
            left.typeHint = if (left.typeHint != null) left.typeHint else right.nullableType
            right.typeHint = if (right.typeHint != null) right.typeHint else left.nullableType
        }

        // TODO better logic for this to allow things such as 'if (null ! $var)', should also revisit the above
        // visit left side to get the type for hinting to the right side if needed
        left.visit()

        // type hint right if not already hinted to the left type and then visit
        right.typeHint = right.typeHint ?: left.type
        right.visit()

        // check if either side is a tuple type. the runtime only allows comparing two values
        if (left.type is TupleType || right.type is TupleType) {
            if (left.type is TupleType) {
                left.reportError(DiagnosticMessage.BINOP_TUPLE_TYPE, "Left", left.type.representation)
            }
            if (right.type is TupleType) {
                right.reportError(DiagnosticMessage.BINOP_TUPLE_TYPE, "Right", right.type.representation)
            }
            return false
        }

        // handle operator specific required types, this applies to all except `!` and `=`.
        if (allowedTypes != null) {
            var leftMatch = false
            var rightMatch = false

            // loop through allowed types and check if any of them match for both left and right
            for (type in allowedTypes) {
                leftMatch = leftMatch || checkTypeMatch(left, type, left.type, reportError = false)
                rightMatch = rightMatch || checkTypeMatch(right, type, right.type, reportError = false)
            }

            if (!leftMatch || !rightMatch) {
                operator.reportError(
                    DiagnosticMessage.BINOP_INVALID_TYPES,
                    operator.text,
                    left.type.representation,
                    right.type.representation
                )
                return false
            }
        }

        // handle equality operator, which allows any type on either side as long as they match
        if (!checkTypeMatch(left, left.type, right.type, reportError = false)) {
            operator.reportError(
                DiagnosticMessage.BINOP_INVALID_TYPES,
                operator.text,
                left.type.representation,
                right.type.representation
            )
            return false
        }

        // other cases are true
        return true
    }

    override fun visitCalcExpression(calcExpression: CalcExpression) {
        // update state to inside calc
        inCalc = true

        val typeHint = calcExpression.typeHint ?: PrimitiveType.INT
        val innerExpression = calcExpression.expression

        // hint to the expression that we expect an int
        innerExpression.typeHint = typeHint
        innerExpression.visit()

        // verify type is an int
        if (!checkTypeMatch(innerExpression, typeHint, innerExpression.type)) {
            calcExpression.type = MetaType.Error
        } else {
            calcExpression.type = typeHint
        }
        // update state to outside calc
        inCalc = false
    }

    /**
     * Overrides original implementation that would fall through to [visitCallExpression]
     * so that we can display an error for attempting to `jump` within a clientscript.
     */
    override fun visitJumpCallExpression(jumpCallExpression: JumpCallExpression) {
        jumpCallExpression.reportError(DiagnosticMessage.JUMP_CALL_IN_CS2, jumpCallExpression.name.text)
        jumpCallExpression.type = MetaType.Unit
    }

    override fun visitCommandCallExpression(commandCallExpression: CommandCallExpression) {
        val name = commandCallExpression.name.text

        // attempt to call the dynamic command handlers type checker (if one exists)
        dynamicCommands[name]?.run {
            // invoke the custom command type checking
            val context = TypeCheckingContext(this@TypeChecking, typeManager, commandCallExpression, diagnostics)
            context.typeCheck()

            // verify the type has been set
            if (commandCallExpression.nullableType == null) {
                commandCallExpression.reportError(DiagnosticMessage.CUSTOM_HANDLER_NOTYPE)
            }

            // if the symbol was not manually specified attempt to look up a predefined one
            if (commandCallExpression.symbol == null) {
                val symbol = rootTable.find(SymbolType.ClientScript(ClientTriggerType.COMMAND), name)
                if (symbol == null) {
                    commandCallExpression.reportError(DiagnosticMessage.CUSTOM_HANDLER_NOSYMBOL)
                }
                commandCallExpression.symbol = symbol
            }
            return
        }

        // fall back to normal call expression implementation
        visitCallExpression(commandCallExpression)
    }

    /**
     * Handles looking up and type checking all call expressions.
     */
    override fun visitCallExpression(callExpression: CallExpression) {
        // lookup the expected symbol type based on the call expression type
        val symbolType = when (callExpression) {
            is CommandCallExpression -> SymbolType.ClientScript(ClientTriggerType.COMMAND)
            is ProcCallExpression -> SymbolType.ClientScript(ClientTriggerType.PROC)
            else -> error(callExpression)
        }

        // TODO support for custom implementation for special commands such as `enum`.
        // lookup the symbol using the symbol type and name
        val name = callExpression.name.text
        val symbol = rootTable.find(symbolType, name)
        if (symbol == null) {
            val errorMessage = when (callExpression) {
                is CommandCallExpression -> DiagnosticMessage.COMMAND_REFERENCE_UNRESOLVED
                is ProcCallExpression -> DiagnosticMessage.PROC_REFERENCE_UNRESOLVED
                else -> error(callExpression)
            }
            callExpression.type = MetaType.Error
            callExpression.reportError(errorMessage, name)
        } else {
            callExpression.symbol = symbol
            callExpression.type = symbol.returns
        }

        // verify the arguments are all valid
        typeCheckArguments(symbol, callExpression, name)
    }

    override fun visitClientScriptExpression(clientScriptExpression: ClientScriptExpression) {
        val typeHint = clientScriptExpression.typeHint
        require(typeHint is MetaType.ClientScript)

        // lookup the symbol by name
        val name = clientScriptExpression.name.text
        val symbolType = SymbolType.ClientScript(ClientTriggerType.CLIENTSCRIPT)
        val symbol = rootTable.find(symbolType, name)

        // verify the clientscript exists
        if (symbol == null) {
            clientScriptExpression.reportError(DiagnosticMessage.CLIENTSCRIPT_REFERENCE_UNRESOLVED, name)
            clientScriptExpression.type = MetaType.Error
        } else {
            clientScriptExpression.symbol = symbol
            clientScriptExpression.type = typeHint
        }

        // verify the arguments are all valid
        typeCheckArguments(symbol, clientScriptExpression, name)

        // disallow transmit list when not expected
        val transmitListType = typeHint.inner
        if (transmitListType == MetaType.Unit && clientScriptExpression.transmitList.isNotEmpty()) {
            clientScriptExpression.transmitList.first().reportError("Unexpected hook transmit list.")
            clientScriptExpression.type = MetaType.Error
            return
        } else {
            for (expr in clientScriptExpression.transmitList) {
                expr.typeHint = transmitListType
                expr.visit()
                checkTypeMatch(expr, transmitListType, expr.type)
            }
        }
    }

    /**
     * Verifies that [callExpression] arguments match the parameter types from [symbol].
     */
    private fun typeCheckArguments(
        symbol: ScriptSymbol.ClientScriptSymbol?,
        callExpression: CallExpression,
        name: String,
    ) {
        // Type check the parameters, use `unit` if there are no parameters
        // we will display a special message if the parameter ends up having unit
        // as the type but arguments are supplied.
        //
        // If the symbol is null then that means we failed to look up the symbol,
        // therefore we should specify the parameter types as error, so we can continue
        // analysis on all the arguments without worrying about a type mismatch.
        val parameterTypes = if (symbol == null) MetaType.Error else symbol.parameters ?: MetaType.Unit
        val expectedTypes = if (parameterTypes is TupleType) {
            parameterTypes.children.toList()
        } else {
            listOf(parameterTypes)
        }
        val actualTypes = typeHintExpressionList(expectedTypes, callExpression.arguments)

        // convert the type lists into a singular type, used for type checking
        val expectedType = TupleType.fromList(expectedTypes)
        val actualType = TupleType.fromList(actualTypes)

        // special case for the temporary state of using unit for no arguments
        if (expectedType == MetaType.Unit && actualType != MetaType.Unit) {
            val errorMessage = when (callExpression) {
                is CommandCallExpression -> DiagnosticMessage.COMMAND_NOARGS_EXPECTED
                is ProcCallExpression -> DiagnosticMessage.PROC_NOARGS_EXPECTED
                is ClientScriptExpression -> DiagnosticMessage.CLIENTSCRIPT_NOARGS_EXPECTED
                else -> error(callExpression)
            }
            callExpression.reportError(
                errorMessage,
                name,
                actualType.representation
            )
            return
        }

        // do the actual type checking
        checkTypeMatch(callExpression, expectedType, actualType)
    }

    /**
     * Type checks the index value expression if it is defined.
     */
    override fun visitLocalVariableExpression(localVariableExpression: LocalVariableExpression) {
        val name = localVariableExpression.name.text
        val symbol = table.find(SymbolType.LocalVariable, name)
        if (symbol == null) {
            // trying to reference a variable that isn't defined
            localVariableExpression.reportError(DiagnosticMessage.LOCAL_REFERENCE_UNRESOLVED, name)
            localVariableExpression.type = MetaType.Error
            return
        }

        val symbolIsArray = symbol.type is ArrayType
        if (!symbolIsArray && localVariableExpression.isArray) {
            // trying to reference non-array local variable and specifying an index
            localVariableExpression.reportError(DiagnosticMessage.LOCAL_REFERENCE_NOT_ARRAY, name)
            localVariableExpression.type = MetaType.Error
            return
        }

        if (symbolIsArray && !localVariableExpression.isArray) {
            // trying to reference array variable without specifying the index in which to access
            localVariableExpression.reportError(DiagnosticMessage.LOCAL_ARRAY_REFERENCE_NOINDEX, name)
            localVariableExpression.type = MetaType.Error
            return
        }

        val indexExpression = localVariableExpression.index
        if (symbol.type is ArrayType && indexExpression != null) {
            // visit the index to set the type of any references
            indexExpression.visit()
            checkTypeMatch(indexExpression, PrimitiveType.INT, indexExpression.type)
        }

        localVariableExpression.reference = symbol
        localVariableExpression.type = if (symbol.type is ArrayType) symbol.type.inner else symbol.type
    }

    override fun visitGameVariableExpression(gameVariableExpression: GameVariableExpression) {
        val name = gameVariableExpression.name.text
        val symbol = rootTable.findAll<ConfigSymbol>(name).firstOrNull { it.type is GameVarType }
        if (symbol == null || symbol.type !is GameVarType) {
            gameVariableExpression.type = MetaType.Error
            gameVariableExpression.reportError(DiagnosticMessage.GAME_REFERENCE_UNRESOLVED, name)
            return
        }

        gameVariableExpression.reference = symbol
        gameVariableExpression.type = symbol.type.inner
    }

    override fun visitConstantVariableExpression(constantVariableExpression: ConstantVariableExpression) {
        val name = constantVariableExpression.name.text

        // constants rely on having a type to parse the constant value for
        val typeHint = constantVariableExpression.typeHint
        if (typeHint == null) {
            constantVariableExpression.reportError(DiagnosticMessage.CONSTANT_UNKNOWN_TYPE, name)
            constantVariableExpression.type = MetaType.Error
            return
        } else if (typeHint == MetaType.Error) {
            // Avoid attempting to parse the constant if it was type hinted to error.
            // This is safe because if the hint type is error that means an error happened
            // elsewhere so an error will have been reported.
            constantVariableExpression.type = MetaType.Error
            return
        }

        // lookup the constant
        val symbol = rootTable.find(SymbolType.Constant, name)
        if (symbol == null) {
            constantVariableExpression.reportError(DiagnosticMessage.CONSTANT_REFERENCE_UNRESOLVED, name)
            constantVariableExpression.type = MetaType.Error
            return
        }

        // check if we're trying to evaluate a constant that is still being evaluated
        if (symbol in constantsBeingEvaluated) {
            // create a stack string and append the symbol that was the start of the loop to it
            var stack = constantsBeingEvaluated.joinToString(" -> ") { "^${it.name}" }
            stack += " -> ^${symbol.name}"

            constantVariableExpression.reportError(DiagnosticMessage.CONSTANT_CYCLIC_REF, stack)
            constantVariableExpression.type = MetaType.Error
            return
        }

        // add the symbol to the set of constants being evaluated
        constantsBeingEvaluated += symbol

        try {
            // base the source information on the string literal
            val (sourceName, sourceLine, sourceColumn) = constantVariableExpression.source

            // wrap string and quote
            val stringExpected = typeHint == PrimitiveType.STRING || typeHint == PrimitiveType.GRAPHIC
            val valueToParse = if (stringExpected && shouldQuoteConstantValue(symbol.value)) {
                "\"${escapeString(symbol.value)}\""
            } else {
                symbol.value
            }

            // attempt to parse the constant value
            val parsedExpression = ScriptParser.invokeParser(
                CharStreams.fromString(valueToParse, sourceName),
                RuneScriptParser::singleExpression,
                DISCARD_ERROR_LISTENER,
                sourceLine - 1,
                sourceColumn - 1
            ) as? Expression

            // verify that the expression parsed properly
            if (parsedExpression == null) {
                constantVariableExpression.reportError(
                    DiagnosticMessage.CONSTANT_PARSE_ERROR,
                    valueToParse,
                    typeHint.representation
                )
                constantVariableExpression.type = MetaType.Error
                return
            }

            // type hint the parsed expression to the expected type and then visit it
            parsedExpression.typeHint = typeHint
            parsedExpression.visit()

            // verify the constant evaluates to a constant expression (no macros!)
            if (!isConstantExpression(parsedExpression)) {
                constantVariableExpression.reportError(DiagnosticMessage.CONSTANT_NONCONSTANT, valueToParse)
                constantVariableExpression.type = MetaType.Error
                return
            }

            // set the sub expression to the parser expression and the type to the parsed expressions type
            constantVariableExpression.subExpression = parsedExpression
            constantVariableExpression.type = parsedExpression.type
        } finally {
            // remove the symbol from the set since it is no longer being evaluated
            constantsBeingEvaluated -= symbol
        }
    }

    /**
     * Determines if the [value] given should implicitly add double quotes (")
     * to it to make it parse-able.
     */
    private fun shouldQuoteConstantValue(value: String): Boolean {
        if (value.length == 1) {
            return true
        }

        if (!value.startsWith("\"") && !value.endsWith("\"")) {
            return true
        }
        return false
    }

    private fun escapeString(value: String): String {
        return value.replace("\"", "\\\"")
    }

    override fun visitIntegerLiteral(integerLiteral: IntegerLiteral) {
        integerLiteral.type = PrimitiveType.INT
    }

    override fun visitCoordLiteral(coordLiteral: CoordLiteral) {
        coordLiteral.type = PrimitiveType.COORD
    }

    override fun visitBooleanLiteral(booleanLiteral: BooleanLiteral) {
        booleanLiteral.type = PrimitiveType.BOOLEAN
    }

    override fun visitCharacterLiteral(characterLiteral: CharacterLiteral) {
        characterLiteral.type = PrimitiveType.CHAR
    }

    override fun visitNullLiteral(nullLiteral: NullLiteral) {
        val hint = nullLiteral.typeHint
        if (hint != null && (hint.baseType == BaseVarType.INTEGER || hint.baseType == BaseVarType.LONG)) {
            // infer the type if the hint base type is an int OR long.
            nullLiteral.type = hint
            return
        }
        nullLiteral.type = PrimitiveType.INT
    }

    override fun visitStringLiteral(stringLiteral: StringLiteral) {
        val typeHint = stringLiteral.typeHint
        if (typeHint == PrimitiveType.GRAPHIC) {
            val symbol = rootTable.find(SymbolType.Basic(PrimitiveType.GRAPHIC), stringLiteral.value)
            if (symbol == null) {
                stringLiteral.type = MetaType.Error
                stringLiteral.reportError(DiagnosticMessage.GENERIC_UNRESOLVED_SYMBOL, stringLiteral.value)
                return
            }
            stringLiteral.reference = symbol
            stringLiteral.type = PrimitiveType.GRAPHIC
            return
        } else if (typeHint is MetaType.ClientScript) {
            handleClientScriptExpression(stringLiteral, typeHint)
            return
        }
        stringLiteral.type = PrimitiveType.STRING
    }

    /**
     * Handles parsing and checking a [ClientScriptExpression] that is parsed from withing the [stringLiteral].
     *
     * This assigns the [StringLiteral.type] to [MetaType.ClientScript] and stores the [ClientScriptExpression]
     * as an attribute on [stringLiteral] for usage later.
     */
    private fun handleClientScriptExpression(stringLiteral: StringLiteral, typeHint: Type) {
        // base the source information on the string literal
        val (sourceName, sourceLine, sourceColumn) = stringLiteral.source

        // invoke the parser to parse the text within the string
        val errorListener = ParserErrorListener(sourceName, diagnostics, sourceLine - 1, sourceColumn)
        val clientScriptExpression = ScriptParser.invokeParser(
            CharStreams.fromString(stringLiteral.value, sourceName),
            RuneScriptParser::clientScript,
            errorListener,
            sourceLine - 1,
            sourceColumn
        ) as? ClientScriptExpression

        // parser returns null if there was a parse error
        if (clientScriptExpression == null) {
            stringLiteral.type = MetaType.Error
            return
        }

        // set typehint to the same as the argument
        clientScriptExpression.typeHint = typeHint
        clientScriptExpression.visit()

        // copy the type from the parsed expression
        stringLiteral.subExpression = clientScriptExpression
        stringLiteral.type = clientScriptExpression.type
    }

    override fun visitJoinedStringExpression(joinedStringExpression: JoinedStringExpression) {
        // visit the parts and verify they're all strings
        for (part in joinedStringExpression.parts) {
            part.typeHint = PrimitiveType.STRING
            part.visit()
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
        val hint = identifier.typeHint

        // look through the global table for a symbol with the given name and type
        var symbol: Symbol? = null
        var symbolType: Type? = null
        for (temp in rootTable.findAll<Symbol>(name)) {
            val tempSymbolType = symbolToType(temp) ?: continue
            if (hint == null || typeManager.check(hint, tempSymbolType)) {
                // hint type matches (or is null) so we can stop looking
                symbol = temp
                symbolType = tempSymbolType
                break
            } else if (symbol == null) {
                // default the symbol to the first thing found just in case
                // no exact matches exist
                symbol = temp
                symbolType = tempSymbolType
            }
        }

        if (symbol == null) {
            // unable to resolve the symbol
            identifier.type = MetaType.Error
            identifier.reportError(DiagnosticMessage.GENERIC_UNRESOLVED_SYMBOL, name)
            return
        }

        // identifier.reportInfo("hint=%s, symbol=%s", hint?.representation ?: "null", symbol)

        // compiler error if the symbol type isn't defined here
        if (symbolType == null) {
            identifier.type = MetaType.Error
            identifier.reportError(DiagnosticMessage.UNSUPPORTED_SYMBOLTYPE_TO_TYPE, symbol::class.java.simpleName)
            return
        }

        identifier.reference = symbol
        identifier.type = symbolType
    }

    /**
     * Attempts to figure out the return type of [symbol].
     *
     * If the symbol is not valid for direct identifier lookup then `null` is returned.
     */
    private fun symbolToType(symbol: Symbol) = when (symbol) {
        is ScriptSymbol -> if (symbol.parameters == null || symbol.parameters == MetaType.Unit) {
            symbol.returns
        } else {
            // things with arguments should not be looked up by only via identifier, so we just
            // skip them by returning null here.
            null
        }
        is LocalVariableSymbol -> symbol.type
        is BasicSymbol -> symbol.type
        is ConstantSymbol -> null
        is ConfigSymbol -> symbol.type
    }

    override fun visitToken(token: Token) {
        // NO-OP
    }

    override fun visitNode(node: Node) {
        val parent = node.parent
        if (parent == null) {
            node.reportInfo("Unhandled node: %s.", node::class.simpleName!!)
        } else {
            node.reportInfo("Unhandled node: %s. Parent: %s", node::class.simpleName!!, parent::class.simpleName!!)
        }
    }

    /**
     * Takes [expectedTypes] and iterates over [expressions] assigning each [Expression.typeHint]
     * a type from [expectedTypes]. All of the [expressions] types are then returned for comparison
     * at call site.
     *
     * This is only useful when the expected types are known ahead of time (e.g. assignments and calls).
     */
    private fun typeHintExpressionList(expectedTypes: List<Type>, expressions: List<Expression>): List<Type> {
        val actualTypes = mutableListOf<Type>()
        var typeCounter = 0
        for (expr in expressions) {
            expr.typeHint = if (typeCounter < expectedTypes.size) expectedTypes[typeCounter] else null
            expr.visit()

            // add the evaluated type
            actualTypes += expr.type

            // increment the counter for type hinting
            typeCounter += if (expr.type is TupleType) {
                (expr.type as TupleType).children.size
            } else {
                1
            }
        }
        return actualTypes
    }

    /**
     * Checks if the [expected] and [actual] match, including accepted casting.
     *
     * If the types passed in are a [TupleType] they will be compared using their flattened types.
     *
     * @see TypeManager.check
     */
    internal fun checkTypeMatch(node: Node, expected: Type, actual: Type, reportError: Boolean = true): Boolean {
        val expectedFlattened = if (expected is TupleType) expected.children else arrayOf(expected)
        val actualFlattened = if (actual is TupleType) actual.children else arrayOf(actual)

        var match = true
        // compare the flattened types
        if (expected == MetaType.Error) {
            // we need to do this to prevent error propagation due to expected type resolving to an error
            match = true
        } else if (expectedFlattened.size != actualFlattened.size) {
            match = false
        } else {
            for (i in expectedFlattened.indices) {
                match = match and typeManager.check(expectedFlattened[i], actualFlattened[i])
            }
        }

        if (!match && reportError) {
            val actualRepresentation = if (actual == MetaType.Unit) {
                "<nothing>"
            } else {
                actual.representation
            }
            node.reportError(
                DiagnosticMessage.GENERIC_TYPE_MISMATCH,
                actualRepresentation,
                expected.representation
            )
        }
        return match
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
    public fun Node?.visit() {
        if (this == null) {
            return
        }
        accept(this@TypeChecking)
    }

    /**
     * Calls [Node.accept] on all nodes in a list.
     */
    public fun List<Node>?.visit() {
        if (this == null) {
            return
        }

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

        /**
         * Array of valid conditional operations allowed within `if` and `while` statements.
         */
        private val CONDITIONAL_OPS = arrayOf(
            "<", ">", "<=", ">=",
            "=", "!",
            "&", "|"
        )

        /**
         * A parser error listener that discards any syntax errors.
         */
        private val DISCARD_ERROR_LISTENER = object : BaseErrorListener() {
            override fun syntaxError(
                recognizer: Recognizer<*, *>?,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String?,
                e: RecognitionException?
            ) {
                // no-op
            }
        }
    }
}
