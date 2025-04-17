package me.filby.neptune.runescript.compiler.semantics

import me.filby.neptune.runescript.antlr.RuneScriptParser
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.NodeSourceLocation
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.ScriptFile
import me.filby.neptune.runescript.ast.Token
import me.filby.neptune.runescript.ast.expr.ArithmeticExpression
import me.filby.neptune.runescript.ast.expr.BinaryExpression
import me.filby.neptune.runescript.ast.expr.BooleanLiteral
import me.filby.neptune.runescript.ast.expr.CalcExpression
import me.filby.neptune.runescript.ast.expr.CallExpression
import me.filby.neptune.runescript.ast.expr.CharacterLiteral
import me.filby.neptune.runescript.ast.expr.ClientScriptExpression
import me.filby.neptune.runescript.ast.expr.CommandCallExpression
import me.filby.neptune.runescript.ast.expr.ConditionExpression
import me.filby.neptune.runescript.ast.expr.ConstantVariableExpression
import me.filby.neptune.runescript.ast.expr.CoordLiteral
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.ExpressionStringPart
import me.filby.neptune.runescript.ast.expr.FixExpression
import me.filby.neptune.runescript.ast.expr.GameVariableExpression
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.expr.IntegerLiteral
import me.filby.neptune.runescript.ast.expr.JoinedStringExpression
import me.filby.neptune.runescript.ast.expr.JumpCallExpression
import me.filby.neptune.runescript.ast.expr.Literal
import me.filby.neptune.runescript.ast.expr.LocalVariableExpression
import me.filby.neptune.runescript.ast.expr.NullLiteral
import me.filby.neptune.runescript.ast.expr.ParenthesizedExpression
import me.filby.neptune.runescript.ast.expr.PostfixExpression
import me.filby.neptune.runescript.ast.expr.PrefixExpression
import me.filby.neptune.runescript.ast.expr.ProcCallExpression
import me.filby.neptune.runescript.ast.expr.StringLiteral
import me.filby.neptune.runescript.ast.expr.StringPart
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
import me.filby.neptune.runescript.compiler.symbol.ConstantSymbol
import me.filby.neptune.runescript.compiler.symbol.LocalVariableSymbol
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.Symbol
import me.filby.neptune.runescript.compiler.symbol.SymbolTable
import me.filby.neptune.runescript.compiler.symbol.SymbolType
import me.filby.neptune.runescript.compiler.trigger.CommandTrigger
import me.filby.neptune.runescript.compiler.trigger.TriggerManager
import me.filby.neptune.runescript.compiler.trigger.TriggerType
import me.filby.neptune.runescript.compiler.type
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
    triggerManager: TriggerManager,
    private val rootTable: SymbolTable,
    private val dynamicCommands: MutableMap<String, DynamicCommandHandler>,
    private val diagnostics: Diagnostics,
) : AstVisitor<Unit> {
    // var Expression.type: Type
    //     get() = getAttribute<Type>("type") ?: error("type not set")
    //     set(value) {
    //         putAttribute("type", value)
    //         reportInfo("Type resolved to: '%s'", value.representation)
    //     }

    /**
     * The trigger that represents 'command'.
     */
    private val commandTrigger = triggerManager.find("command")

    /**
     * The trigger that represents `proc`.
     */
    private val procTrigger = triggerManager.find("proc")

    /**
     * The trigger that represents `clientscript`. This trigger is optional.
     */
    private val clientscriptTrigger = triggerManager.findOrNull("clientscript")

    /**
     * The trigger that represents `label`. This trigger is optional.
     */
    private val labelTrigger = triggerManager.findOrNull("label")

    /**
     * The current table. This is updated each time when entering a new script or block.
     */
    private var table: SymbolTable = rootTable

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
    }

    /**
     * Finds the first [Expression] node in the tree that is not either a [BinaryExpression] or
     * [ParenthesizedExpression]. If `null` is returned then that means the whole tree is valid
     * is all valid conditional expressions.
     */
    private fun findInvalidConditionExpression(expression: Expression): Node? = when (expression) {
        is ConditionExpression -> if (expression.operator.text == "|" || expression.operator.text == "&") {
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
            ref == null || isConstantSymbol(ref)
        }
        else -> false
    }

    /**
     * Checks if the value of [symbol] is known at compile time.
     */
    private fun isConstantSymbol(symbol: Symbol): Boolean = when (symbol) {
        is BasicSymbol -> true
        is ConstantSymbol -> true
        else -> false
    }

    override fun visitDeclarationStatement(declarationStatement: DeclarationStatement) {
        val typeName = declarationStatement.typeToken.text.removePrefix("def_")
        val name = declarationStatement.name.text
        val type = typeManager.findOrNull(typeName)

        // notify invalid type
        if (type == null) {
            declarationStatement.typeToken.reportError(DiagnosticMessage.GENERIC_INVALID_TYPE, typeName)
        } else if (!type.options.allowDeclaration) {
            declarationStatement.typeToken.reportError(
                DiagnosticMessage.LOCAL_DECLARATION_INVALID_TYPE,
                type.representation,
            )
        }

        // attempt to insert the local variable into the symbol table and display error if failed to insert
        val symbol = LocalVariableSymbol(name, type ?: MetaType.Error)
        val inserted = table.insert(SymbolType.LocalVariable, symbol)
        if (!inserted) {
            declarationStatement.name.reportError(DiagnosticMessage.SCRIPT_LOCAL_REDECLARATION, name)
        }

        // visit the initializer if it exists to resolve references in it
        val initializer = declarationStatement.initializer
        if (initializer != null) {
            // type hint that we want whatever the declarations type is then visit
            initializer.typeHint = symbol.type
            initializer.visit()

            checkTypeMatch(initializer, symbol.type, initializer.type)
        }

        declarationStatement.symbol = symbol
    }

    override fun visitArrayDeclarationStatement(arrayDeclarationStatement: ArrayDeclarationStatement) {
        val typeName = arrayDeclarationStatement.typeToken.text.removePrefix("def_")
        val name = arrayDeclarationStatement.name.text
        var type = typeManager.findOrNull(typeName)

        // notify invalid type
        if (type == null) {
            arrayDeclarationStatement.typeToken.reportError(DiagnosticMessage.GENERIC_INVALID_TYPE, typeName)
        } else if (!type.options.allowDeclaration) {
            arrayDeclarationStatement.typeToken.reportError(
                DiagnosticMessage.LOCAL_DECLARATION_INVALID_TYPE,
                type.representation,
            )
        } else if (!type.options.allowArray) {
            arrayDeclarationStatement.typeToken.reportError(
                DiagnosticMessage.LOCAL_ARRAY_INVALID_TYPE,
                type.representation,
            )
        }

        type = if (type != null) {
            // convert type into an array of type
            ArrayType(type)
        } else {
            // type doesn't exist so give it error type
            MetaType.Error
        }

        // visit the initializer if it exists to resolve references in it
        val initializer = arrayDeclarationStatement.initializer
        initializer.typeHint = PrimitiveType.INT
        initializer.visit()
        checkTypeMatch(initializer, PrimitiveType.INT, initializer.type)

        // attempt to insert the local variable into the symbol table and display error if failed to insert
        val symbol = LocalVariableSymbol(name, type)
        val inserted = table.insert(SymbolType.LocalVariable, LocalVariableSymbol(name, type))
        if (!inserted) {
            arrayDeclarationStatement.name.reportError(DiagnosticMessage.SCRIPT_LOCAL_REDECLARATION, name)
        }

        arrayDeclarationStatement.symbol = symbol
    }

    override fun visitAssignmentStatement(assignmentStatement: AssignmentStatement) {
        val vars = assignmentStatement.vars

        // visit the lhs to fetch the references
        vars.visit()

        // store the lhs types to help with type hinting
        val leftTypes = vars.map { it.type }
        val rightTypes = typeHintExpressionList(leftTypes, assignmentStatement.expressions)

        // convert types to tuple type if necessary for easy comparison
        val leftType = TupleType.fromList(leftTypes)
        val rightType = TupleType.fromList(rightTypes)

        checkTypeMatch(assignmentStatement, leftType, rightType)

        // prevent multi assignment involving arrays
        val firstArrayRef = vars.firstOrNull { it is LocalVariableExpression && it.isArray }
        if (vars.size > 1 && firstArrayRef != null) {
            firstArrayRef.reportError(DiagnosticMessage.ASSIGN_MULTI_ARRAY)
        }
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

    override fun visitConditionExpression(conditionExpression: ConditionExpression) {
        val left = conditionExpression.left
        val right = conditionExpression.right
        val operator = conditionExpression.operator

        // check for validation based on if we're within calc or condition.
        val validOperation = checkBinaryConditionOperation(left, operator, right)

        // early return if it isn't a valid operation
        if (!validOperation) {
            conditionExpression.type = MetaType.Error
            return
        }

        // conditions expect boolean
        conditionExpression.type = PrimitiveType.BOOLEAN
    }

    /**
     * Verifies the binary expression is a valid condition operation.
     */
    private fun checkBinaryConditionOperation(left: Expression, operator: Token, right: Expression): Boolean {
        // some operators expect a specific type on both sides, specify those type(s) here
        val allowedTypes = when (operator.text) {
            "&", "|" -> ALLOWED_LOGICAL_TYPES
            "<", ">", "<=", ">=" -> ALLOWED_RELATIONAL_TYPES
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

        // verify the left and right type only return 1 type that is not 'unit'.
        if (left.type is TupleType || right.type is TupleType) {
            if (left.type is TupleType) {
                left.reportError(DiagnosticMessage.BINOP_TUPLE_TYPE, "Left", left.type.representation)
            }
            if (right.type is TupleType) {
                right.reportError(DiagnosticMessage.BINOP_TUPLE_TYPE, "Right", right.type.representation)
            }
            return false
        } else if (left.type == MetaType.Unit || right.type == MetaType.Unit) {
            operator.reportError(
                DiagnosticMessage.BINOP_INVALID_TYPES,
                operator.text,
                left.type.representation,
                right.type.representation,
            )
            return false
        }

        // handle operator specific required types, this applies to all except `!` and `=`.
        if (allowedTypes != null) {
            if (
                !checkTypeMatchAny(left, allowedTypes, left.type) ||
                !checkTypeMatchAny(right, allowedTypes, right.type)
            ) {
                operator.reportError(
                    DiagnosticMessage.BINOP_INVALID_TYPES,
                    operator.text,
                    left.type.representation,
                    right.type.representation,
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
                right.type.representation,
            )
            return false
        } else if (left.type == PrimitiveType.STRING && right.type == PrimitiveType.STRING) {
            operator.reportError(
                DiagnosticMessage.BINOP_INVALID_TYPES,
                operator.text,
                left.type.representation,
                right.type.representation,
            )
            return false
        }

        // other cases are true
        return true
    }

    override fun visitArithmeticExpression(arithmeticExpression: ArithmeticExpression) {
        val left = arithmeticExpression.left
        val right = arithmeticExpression.right
        val operator = arithmeticExpression.operator

        // arithmetic expression only expect int or long return types, but just allow
        val expectedType = when (val hint = arithmeticExpression.typeHint) {
            null -> PrimitiveType.INT
            else -> hint
        }

        // visit left-hand side
        left.typeHint = expectedType
        left.visit()

        // visit right-hand side
        right.typeHint = expectedType
        right.visit()

        // verify if both sides are int or long and are of the same type
        if (
            !checkTypeMatchAny(left, ALLOWED_ARITHMETIC_TYPES, left.type) ||
            !checkTypeMatchAny(left, ALLOWED_ARITHMETIC_TYPES, right.type) ||
            !checkTypeMatch(left, expectedType, left.type, reportError = false) ||
            !checkTypeMatch(right, expectedType, right.type, reportError = false)
        ) {
            operator.reportError(
                DiagnosticMessage.BINOP_INVALID_TYPES,
                operator.text,
                left.type.representation,
                right.type.representation,
            )
            arithmeticExpression.type = MetaType.Error
            return
        }

        arithmeticExpression.type = expectedType
    }

    override fun visitCalcExpression(calcExpression: CalcExpression) {
        val typeHint = calcExpression.typeHint ?: PrimitiveType.INT
        val innerExpression = calcExpression.expression

        // hint to the expression that we expect an int
        innerExpression.typeHint = typeHint
        innerExpression.visit()

        // verify type is an int
        if (!checkTypeMatchAny(innerExpression, ALLOWED_ARITHMETIC_TYPES, innerExpression.type)) {
            innerExpression.reportError(DiagnosticMessage.ARITHMETIC_INVALID_TYPE, innerExpression.type.representation)
            calcExpression.type = MetaType.Error
        } else {
            calcExpression.type = innerExpression.type
        }
    }

    override fun visitCommandCallExpression(commandCallExpression: CommandCallExpression) {
        val name = commandCallExpression.name.text

        // attempt to call the dynamic command handlers type checker (if one exists)
        if (checkDynamicCommand(name, commandCallExpression)) {
            return
        }

        // check the command call
        checkCallExpression(commandCallExpression, commandTrigger, DiagnosticMessage.COMMAND_REFERENCE_UNRESOLVED)
    }

    override fun visitProcCallExpression(procCallExpression: ProcCallExpression) {
        // check the proc call
        checkCallExpression(procCallExpression, procTrigger, DiagnosticMessage.PROC_REFERENCE_UNRESOLVED)
    }

    override fun visitJumpCallExpression(jumpCallExpression: JumpCallExpression) {
        if (labelTrigger == null) {
            jumpCallExpression.reportError("Jump expression not allowed.")
            return
        }

        // check the jump call
        checkCallExpression(jumpCallExpression, labelTrigger, DiagnosticMessage.JUMP_REFERENCE_UNRESOLVED)
    }

    /**
     * Runs the type checking for dynamic commands if one exists with [name].
     */
    private fun checkDynamicCommand(name: String, expression: Expression): Boolean {
        val dynamicCommand = dynamicCommands[name] ?: return false
        with(dynamicCommand) {
            // invoke the custom command type checking
            val context = TypeCheckingContext(this@TypeChecking, typeManager, expression, diagnostics)
            context.typeCheck()

            // verify the type has been set
            if (expression.nullableType == null) {
                expression.reportError(DiagnosticMessage.CUSTOM_HANDLER_NOTYPE)
            }

            // if the symbol was not manually specified attempt to look up a predefined one
            @Suppress("ktlint:standard:condition-wrapping")
            if (
                expression is Identifier && expression.reference == null ||
                expression is CallExpression && expression.reference == null
            ) {
                val symbol = rootTable.find(SymbolType.ClientScript(commandTrigger), name)
                if (symbol == null) {
                    expression.reportError(DiagnosticMessage.CUSTOM_HANDLER_NOSYMBOL)
                }
                when (expression) {
                    is Identifier -> expression.reference = symbol
                    is CallExpression -> expression.reference = symbol
                }
            }
        }
        return true
    }

    /**
     * Handles looking up and type checking all call expressions.
     */
    private fun checkCallExpression(call: CallExpression, trigger: TriggerType, unresolvedSymbolMessage: String) {
        // lookup the symbol using the symbol type and name
        val name = call.name.text
        val symbolType = SymbolType.ClientScript(trigger)
        val symbol = rootTable.find(symbolType, name)
        if (symbol == null) {
            call.type = MetaType.Error
            call.reportError(unresolvedSymbolMessage, name)
        } else {
            call.reference = symbol
            call.type = symbol.returns
        }

        // verify the arguments are all valid
        typeCheckArguments(symbol, call, name)
    }

    override fun visitClientScriptExpression(clientScriptExpression: ClientScriptExpression) {
        if (clientscriptTrigger == null) {
            clientScriptExpression.reportError(DiagnosticMessage.TRIGGER_TYPE_NOT_FOUND, "clientscript")
            return
        }

        val typeHint = clientScriptExpression.typeHint
        check(typeHint is MetaType.Hook)

        // lookup the symbol by name
        val name = clientScriptExpression.name.text
        val symbolType = SymbolType.ClientScript(clientscriptTrigger)
        val symbol = rootTable.find(symbolType, name)

        // verify the clientscript exists
        if (symbol == null) {
            clientScriptExpression.reportError(DiagnosticMessage.CLIENTSCRIPT_REFERENCE_UNRESOLVED, name)
            clientScriptExpression.type = MetaType.Error
        } else {
            clientScriptExpression.reference = symbol
            clientScriptExpression.type = typeHint
        }

        // verify the arguments are all valid
        typeCheckArguments(symbol, clientScriptExpression, name)

        // disallow transmit list when not expected
        val transmitListType = typeHint.transmitListType
        if (transmitListType == MetaType.Unit && clientScriptExpression.transmitList.isNotEmpty()) {
            clientScriptExpression.transmitList.first().reportError(DiagnosticMessage.HOOK_TRANSMIT_LIST_UNEXPECTED)
            clientScriptExpression.type = MetaType.Error
            return
        }

        for (expr in clientScriptExpression.transmitList) {
            expr.typeHint = transmitListType
            expr.visit()
            checkTypeMatch(expr, transmitListType, expr.type)
        }
    }

    /**
     * Verifies that [callExpression] arguments match the parameter types from [symbol].
     */
    private fun typeCheckArguments(symbol: ScriptSymbol?, callExpression: CallExpression, name: String) {
        // Type check the parameters, use `unit` if there are no parameters
        // we will display a special message if the parameter ends up having unit
        // as the type but arguments are supplied.
        //
        // If the symbol is null then that means we failed to look up the symbol,
        // therefore we should specify the parameter types as error, so we can continue
        // analysis on all the arguments without worrying about a type mismatch.
        val parameterTypes = symbol?.parameters ?: MetaType.Error
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
                is JumpCallExpression -> DiagnosticMessage.JUMP_NOARGS_EXPECTED
                is ClientScriptExpression -> DiagnosticMessage.CLIENTSCRIPT_NOARGS_EXPECTED
                else -> error(callExpression)
            }
            callExpression.reportError(
                errorMessage,
                name,
                actualType.representation,
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
        val symbol = rootTable.findAll<BasicSymbol>(name).firstOrNull { it.type is GameVarType }
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

            // check if the expected type is a string type
            val graphicType = typeManager.findOrNull("graphic")
            val stringExpected = typeHint == PrimitiveType.STRING || graphicType != null && typeHint == graphicType

            // attempt to parse the constant value
            val parsedExpression = if (stringExpected) {
                StringLiteral(
                    NodeSourceLocation(sourceName, sourceLine - 1, sourceColumn - 1),
                    symbol.value,
                )
            } else {
                ScriptParser.invokeParser(
                    CharStreams.fromString(symbol.value, sourceName),
                    RuneScriptParser::singleExpression,
                    DISCARD_ERROR_LISTENER,
                    sourceLine - 1,
                    sourceColumn - 1,
                ) as? Expression
            }

            // verify that the expression parsed properly
            if (parsedExpression == null) {
                constantVariableExpression.reportError(
                    DiagnosticMessage.CONSTANT_PARSE_ERROR,
                    symbol.value,
                    typeHint.representation,
                )
                constantVariableExpression.type = MetaType.Error
                return
            }

            // type hint the parsed expression to the expected type and then visit it
            parsedExpression.typeHint = typeHint
            parsedExpression.visit()

            // verify the constant evaluates to a constant expression (no macros!)
            if (!isConstantExpression(parsedExpression)) {
                constantVariableExpression.reportError(DiagnosticMessage.CONSTANT_NONCONSTANT, symbol.value)
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
        if (hint != null) {
            // infer the type if the hint base type is an int OR long.
            nullLiteral.type = hint
            return
        }
        nullLiteral.type = PrimitiveType.INT
    }

    override fun visitStringLiteral(stringLiteral: StringLiteral) {
        val hint = stringLiteral.typeHint

        // These ugly conditions are here to enable special cases.
        // 1) If the hint is a hook
        // 2) If the hint is not a string, and not any of the other types
        //    representable by a literal expression. It should be possible to
        //    reference a symbol via quoting it, this enables the ability to
        //    reference a symbol without it being a valid identifier.

        if (hint == null || typeManager.check(hint, PrimitiveType.STRING)) {
            // early check if string is assignable to hint
            // this mostly exists for when the expected type is `any`, we just
            // treat it as a string
            stringLiteral.type = PrimitiveType.STRING
        } else if (hint is MetaType.Hook) {
            handleClientScriptExpression(stringLiteral, hint)
        } else if (hint !in LITERAL_TYPES) {
            stringLiteral.reference = resolveSymbol(stringLiteral, stringLiteral.value, hint)
        } else {
            stringLiteral.type = PrimitiveType.STRING
        }
    }

    /**
     * Handles parsing and checking a [ClientScriptExpression] that is parsed from withing the [stringLiteral].
     *
     * This assigns the [StringLiteral.type] to [MetaType.Hook] and stores the [ClientScriptExpression]
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
            sourceColumn,
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
        // visit the parts
        joinedStringExpression.parts.visit()
        joinedStringExpression.type = PrimitiveType.STRING
    }

    override fun visitJoinedStringPart(stringPart: StringPart) {
        if (stringPart is ExpressionStringPart) {
            // typehint, visit, check the inner expression
            val expression = stringPart.expression
            expression.typeHint = PrimitiveType.STRING
            expression.visit()
            checkTypeMatch(expression, PrimitiveType.STRING, expression.type)
        }
    }

    override fun visitIdentifier(identifier: Identifier) {
        val name = identifier.text
        val hint = identifier.typeHint

        // attempt to call the dynamic command handlers type checker (if one exists)
        if (checkDynamicCommand(name, identifier)) {
            return
        }

        // error is reported in resolveSymbol
        val symbol = resolveSymbol(identifier, name, hint) ?: return
        if (symbol is ScriptSymbol && symbol.trigger == CommandTrigger && symbol.parameters != MetaType.Unit) {
            identifier.reportError(
                DiagnosticMessage.GENERIC_TYPE_MISMATCH,
                "<unit>",
                symbol.parameters.representation,
            )
        }

        identifier.reference = symbol
    }

    override fun visitPrefixExpression(prefixExpression: PrefixExpression) {
        checkFixExpression(prefixExpression)
    }

    override fun visitPostfixExpression(postfixExpression: PostfixExpression) {
        checkFixExpression(postfixExpression)
    }

    private fun checkFixExpression(fixExpression: FixExpression) {
        val operator = fixExpression.operator
        val variable = fixExpression.variable

        // check for valid prefix/postfix operator
        if (operator.text != "++" && operator.text != "--") {
            operator.reportError(
                DiagnosticMessage.UNSUPPORTED_FIX_OPERATOR,
                if (fixExpression.isPrefix) "prefix" else "postfix",
                operator.text
            )
            fixExpression.type = MetaType.Error
            return
        }

        variable.visit()

        // check if operand is an assignable variable
        if (variable !is LocalVariableExpression && variable !is GameVariableExpression) {
            variable.reportError(
                DiagnosticMessage.FIX_OPERATOR_REQUIRES_ASSIGNABLE,
                if (fixExpression.isPrefix) "Prefix" else "Postfix",
            )
            fixExpression.type = MetaType.Error
            return
        }

        // since only increment/decrement is allowed, we need to check if the
        // expression type is an arithmetic allowed type
        if (!checkTypeMatchAny(variable, ALLOWED_ARITHMETIC_TYPES, variable.type)) {
            variable.reportError(
                DiagnosticMessage.FIX_OPERATOR_INVALID_TYPE,
                if (fixExpression.isPrefix) "Prefix" else "Postfix",
                operator.text,
                variable.type.representation
            )
            fixExpression.type = MetaType.Error
            return
        }

        // set the type to the same as the operand
        fixExpression.type = variable.type
    }

    private fun resolveSymbol(node: Expression, name: String, hint: Type?): Symbol? {
        // look through the current scopes table for a symbol with the given name and type
        var symbol: Symbol? = null
        var symbolType: Type? = null
        for (temp in table.findAll<Symbol>(name)) {
            val tempSymbolType = symbolToType(temp) ?: continue
            if (hint == null && tempSymbolType is MetaType.Script) {
                // if the hint is unknown it means we're somewhere that probably shouldn't
                // be referring to a script by only the name. this will not capture command
                // "scripts" since the symbolToType for commands returns the return value of
                // the command instead of being wrapped in MetaType.Script.
                continue
            } else if (hint == null || typeManager.check(hint, tempSymbolType)) {
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
            node.type = MetaType.Error
            node.reportError(DiagnosticMessage.GENERIC_UNRESOLVED_SYMBOL, name)
            return null
        }

        // node.reportInfo("hint=%s, symbol=%s", hint?.representation ?: "null", symbol)

        // compiler error if the symbol type isn't defined here
        if (symbolType == null) {
            node.type = MetaType.Error
            node.reportError(DiagnosticMessage.UNSUPPORTED_SYMBOLTYPE_TO_TYPE, symbol::class.java.simpleName)
            return null
        }

        node.type = symbolType
        return symbol
    }

    /**
     * Attempts to figure out the return type of [symbol].
     *
     * If the symbol is not valid for direct identifier lookup then `null` is returned.
     */
    private fun symbolToType(symbol: Symbol) = when (symbol) {
        is ScriptSymbol -> if (symbol.trigger == CommandTrigger) {
            // only commands can be referenced by an identifier and return a value
            symbol.returns
        } else {
            // all other triggers get wrapped in a script type
            MetaType.Script(symbol.trigger, symbol.parameters, symbol.returns)
        }
        is LocalVariableSymbol -> if (symbol.type is ArrayType) {
            // only local array variables are accessible by only their identifier
            symbol.type
        } else {
            null
        }
        is BasicSymbol -> symbol.type
        is ConstantSymbol -> null
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
                "<unit>"
            } else {
                actual.representation
            }
            node.reportError(
                DiagnosticMessage.GENERIC_TYPE_MISMATCH,
                actualRepresentation,
                expected.representation,
            )
        }
        return match
    }

    /**
     * Checks if the [actual] matches any of [expected], including accepted casting.
     *
     * If the types passed in are a [TupleType] they will be compared using their flattened types.
     *
     * @see TypeManager.check
     */
    private fun checkTypeMatchAny(node: Node, expected: Array<out Type>, actual: Type): Boolean {
        for (type in expected) {
            if (checkTypeMatch(node, type, actual, false)) {
                return true
            }
        }
        return false
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
         * Array of valid types allowed in logical conditional expressions.
         */
        private val ALLOWED_LOGICAL_TYPES = arrayOf(
            PrimitiveType.BOOLEAN,
        )

        /**
         * Array of valid types allowed in relational conditional expressions.
         */
        private val ALLOWED_RELATIONAL_TYPES = arrayOf(
            PrimitiveType.INT,
            PrimitiveType.LONG,
        )

        /**
         * Array of valid types allowed in arithmetic expressions.
         */
        private val ALLOWED_ARITHMETIC_TYPES = arrayOf(
            PrimitiveType.INT,
            PrimitiveType.LONG,
        )

        /**
         * Set of types that have a literal representation.
         */
        private val LITERAL_TYPES = setOf(
            PrimitiveType.INT,
            PrimitiveType.BOOLEAN,
            PrimitiveType.COORD,
            PrimitiveType.STRING,
            PrimitiveType.CHAR,
            PrimitiveType.LONG,
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
                e: RecognitionException?,
            ) {
                // no-op
            }
        }
    }
}
