package me.filby.neptune.runescript.compiler.codegen

import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Parameter
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.ScriptFile
import me.filby.neptune.runescript.ast.expr.BinaryExpression
import me.filby.neptune.runescript.ast.expr.BooleanLiteral
import me.filby.neptune.runescript.ast.expr.CalcExpression
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
import me.filby.neptune.runescript.ast.statement.SwitchStatement
import me.filby.neptune.runescript.ast.statement.WhileStatement
import me.filby.neptune.runescript.compiler.codegen.script.Block
import me.filby.neptune.runescript.compiler.codegen.script.Label
import me.filby.neptune.runescript.compiler.codegen.script.LabelGenerator
import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.codegen.script.SwitchTable
import me.filby.neptune.runescript.compiler.configuration.command.CodeGeneratorContext
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.defaultCase
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostic
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticMessage
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticType
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostics
import me.filby.neptune.runescript.compiler.reference
import me.filby.neptune.runescript.compiler.returnType
import me.filby.neptune.runescript.compiler.subExpression
import me.filby.neptune.runescript.compiler.symbol
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.SymbolTable
import me.filby.neptune.runescript.compiler.trigger.ClientTriggerType
import me.filby.neptune.runescript.compiler.triggerType
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType

/**
 * A [AstVisitor] implementation that converts AST nodes into a set of instructions.
 */
public class CodeGenerator(
    private val rootTable: SymbolTable,
    private val dynamicCommands: MutableMap<String, DynamicCommandHandler>,
    private val diagnostics: Diagnostics
) : AstVisitor<Unit> {
    /**
     * An instance of a [LabelGenerator] used to created labels within the instance.
     */
    private val labelGenerator = LabelGenerator()

    /**
     * A list of scripts that were defined in the file.
     */
    private val _scripts = mutableListOf<RuneScript>()

    /**
     * An immutable list of scripts that were defined in the file.
     */
    public val scripts: List<RuneScript> get() = _scripts

    /**
     * The current active script.
     */
    private inline val script get() = _scripts.last()

    /**
     * The current active block.
     */
    private lateinit var block: Block

    /**
     * Used for source line number instructions to prevent repeating the same line number
     * for multiple expressions.
     */
    private var lastLineNumber = -1

    /**
     * Binds [block] by setting it as the active block and adding it to the active [script].
     */
    private fun bind(block: Block): Block {
        this.block = block
        return block
    }

    /**
     * Creates a new [Block] with the given name.
     */
    private fun generateBlock(name: String, generateUniqueName: Boolean = true): Block {
        val block = Block(if (generateUniqueName) labelGenerator.generate(name) else Label(name))
        script.blocks += block
        return block
    }

    private fun generateBlock(label: Label): Block {
        val block = Block(label)
        script.blocks += block
        return block
    }

    /**
     * Adds an instruction to [block], which defaults to the most recently bound [Block].
     */
    internal fun instruction(opcode: Opcode, operand: Any, block: Block = this.block) {
        block += Instruction(opcode, operand)
    }

    /**
     * Inserts a [Opcode.LINENUMBER] instruction if the source line of the node
     * does not match the previous source line number instruction that was
     * inserted.
     */
    internal fun Node.lineInstruction() {
        if (source.line != lastLineNumber) {
            instruction(Opcode.LINENUMBER, source.line)
            lastLineNumber = source.line
        }
    }

    override fun visitScriptFile(scriptFile: ScriptFile) {
        scriptFile.scripts.visit()
    }

    override fun visitScript(script: Script) {
        // skip clientscript commands
        if (script.triggerType == ClientTriggerType.COMMAND) {
            return
        }

        // add the script to the list of scripts in the file
        _scripts += RuneScript(script.source.name, script.triggerType, script.name.text)

        // visit parameters to add them to the scripts local table
        script.parameters.visit()

        // generate and bind an entry point block
        bind(generateBlock("entry", generateUniqueName = false))

        // insert source line number
        script.lineInstruction()

        // visit the statements
        script.statements.visit()

        // add the default returns
        generateDefaultReturns(script)

        // reset the internal state
        labelGenerator.reset()
        lastLineNumber = -1
    }

    override fun visitParameter(parameter: Parameter) {
        val symbol = parameter.symbol

        // add the local variable symbol to list of parameters and all locals
        script.locals.parameters += symbol
        script.locals.all += symbol
    }

    /**
     * Generates the default returns that is added to the end of every script.
     */
    private fun generateDefaultReturns(script: Script) {
        // specify the line number where the script is defined for default returns
        script.lineInstruction()

        val types = TupleType.toList(script.returnType)
        for (type in types) {
            val default = type.defaultValue
            if (default == null) {
                script.reportError(DiagnosticMessage.TYPE_HAS_NO_DEFAULT, type)
                return
            }
            instruction(Opcode.PUSH_CONSTANT, default)
        }
        instruction(Opcode.RETURN, 0)
    }

    override fun visitBlockStatement(blockStatement: BlockStatement) {
        blockStatement.statements.visit()
    }

    override fun visitReturnStatement(returnStatement: ReturnStatement) {
        returnStatement.expressions.visit()
        returnStatement.lineInstruction()
        instruction(Opcode.RETURN, 0)
    }

    override fun visitIfStatement(ifStatement: IfStatement) {
        val ifTrue = labelGenerator.generate("if_true")
        val ifElse = if (ifStatement.elseStatement != null) labelGenerator.generate("if_else") else null
        val ifEnd = labelGenerator.generate("if_end")

        // generate the condition
        generateCondition(ifStatement.condition, block, ifTrue, ifElse ?: ifEnd)

        // bind the if_true block and visit the statements within
        bind(generateBlock(ifTrue))
        ifStatement.thenStatement.visit()
        instruction(Opcode.BRANCH, ifEnd)

        // handle else statement if it exists
        if (ifElse != null) {
            // bind the if_else block and visit the statements within
            bind(generateBlock(ifElse))
            ifStatement.elseStatement.visit()

            // branch to the if_end label
            instruction(Opcode.BRANCH, ifEnd)
        }

        // bind the if_end block
        bind(generateBlock(ifEnd))
    }

    override fun visitWhileStatement(whileStatement: WhileStatement) {
        val whileStart = labelGenerator.generate("while_start")
        val whileBody = labelGenerator.generate("while_body")
        val whileEnd = labelGenerator.generate("while_end")

        // bind the start block and generate the condition in it
        val startBlock = bind(generateBlock(whileStart))
        generateCondition(whileStatement.condition, startBlock, whileBody, whileEnd)

        // generate the body and branch back up to the condition
        bind(generateBlock(whileBody))
        whileStatement.thenStatement.visit()
        instruction(Opcode.BRANCH, whileStart)

        // generate the end block that is jumped to when the condition is false
        bind(generateBlock(whileEnd))
    }

    private fun generateCondition(condition: Expression, block: Block, branchTrue: Label, branchFalse: Label) {
        if (condition is BinaryExpression) {
            val isLogical = condition.operator.text in LOGICAL_OPERATORS
            if (!isLogical) {
                // assume if we get to this point that the left and right types match and are valid
                val baseType = condition.left.type.baseType
                if (baseType == null) {
                    condition.left.reportError(DiagnosticMessage.TYPE_HAS_NO_BASETYPE, condition.left.type)
                    return
                }

                // lookup the proper branching instruction based on the base type used
                val branchOpcodes = BRANCH_MAPPINGS[baseType] ?: error("No mappings for BaseType: $baseType")
                val branchOpcode = branchOpcodes[condition.operator.text]
                    ?: error("No mappings for operator: ${condition.operator.text}")

                // visit the two sides
                condition.left.visit()
                condition.right.visit()

                // add the true branch opcode and false branch instructions
                instruction(branchOpcode, branchTrue, block)
                instruction(Opcode.BRANCH, branchFalse, block)
            } else {
                // generate the label for the next block
                val nextBlockLabel = if (condition.operator.text == LOGICAL_OR) {
                    labelGenerator.generate("condition_or")
                } else {
                    labelGenerator.generate("condition_and")
                }

                // figure out which labels should be the true and false labels
                val trueLabel = if (condition.operator.text == LOGICAL_OR) branchTrue else nextBlockLabel
                val falseLabel = if (condition.operator.text == LOGICAL_OR) nextBlockLabel else branchFalse

                generateCondition(condition.left, block, trueLabel, falseLabel)
                val nextBlock = bind(generateBlock(nextBlockLabel))
                generateCondition(condition.right, nextBlock, branchTrue, branchFalse)
            }
        } else if (condition is ParenthesizedExpression) {
            generateCondition(condition.expression, block, branchTrue, branchFalse)
        } else {
            condition.reportError(DiagnosticMessage.INVALID_CONDITION, condition::class.java.simpleName)
        }
    }

    override fun visitSwitchStatement(switchStatement: SwitchStatement) {
        val table = script.generateSwitchTable()
        val hasDefault = switchStatement.defaultCase != null
        val switchDefault = if (hasDefault) labelGenerator.generate("switch_default_case") else null
        val switchEnd = labelGenerator.generate("switch_end")

        // visit the main expression that contains the value
        switchStatement.condition.visit()

        // add the switch instruction with a reference to the table
        instruction(Opcode.SWITCH, table)

        // jump to either the default or end depending on if a default is defined
        instruction(Opcode.BRANCH, switchDefault ?: switchEnd)

        for (case in switchStatement.cases) {
            // generate a label if the case isn't a default case.
            val caseLabel = if (!case.isDefault) {
                labelGenerator.generate("switch_${table.id}_case")
            } else {
                switchDefault ?: error("switchDefault null while having a default case")
            }

            // loop over the case keys and resolve them to constants
            val keys = mutableListOf<Any>()
            for (keyExpression in case.keys) {
                val constantKey = resolveConstantValue(keyExpression)
                if (constantKey == null) {
                    // null is only returned if the constant wasn't defined or the expression wasn't supported.
                    keyExpression.reportError(DiagnosticMessage.NULL_CONSTANT, keyExpression::class.java.simpleName)
                    continue
                }

                // add the key to the temporary list of keys
                keys += constantKey
            }

            // add the case to the table
            table.addCase(SwitchTable.SwitchCase(caseLabel, keys))

            // generate the block for the case and then add the code within it
            bind(generateBlock(caseLabel))
            case.statements.visit()
            instruction(Opcode.BRANCH, switchEnd)
        }

        // bind the switch end block that all cases jump to (no fallthrough)
        bind(generateBlock(switchEnd))
    }

    /**
     * Attempts to resolve [expression] to a constant value.
     */
    private fun resolveConstantValue(expression: Expression) = when (expression) {
        is ConstantVariableExpression -> expression.reference
        is Identifier -> expression.reference
        is StringLiteral -> if (expression.type == PrimitiveType.GRAPHIC) {
            expression.reference
        } else {
            expression.value
        }
        is Literal<*> -> expression.value
        else -> null
    }

    override fun visitDeclarationStatement(declarationStatement: DeclarationStatement) {
        val symbol = declarationStatement.symbol

        // add the variable to the scripts local table
        script.locals.all += symbol

        val initializer = declarationStatement.initializer
        if (initializer != null) {
            // visit the initializer expression
            declarationStatement.initializer.visit()
        } else {
            // handle default based on the type information
            instruction(Opcode.PUSH_CONSTANT, symbol.type.defaultValue as Any)
        }
        instruction(Opcode.POP_VAR, symbol)
    }

    override fun visitArrayDeclarationStatement(arrayDeclarationStatement: ArrayDeclarationStatement) {
        val symbol = arrayDeclarationStatement.symbol

        // add the variable to the scripts local table
        script.locals.all += symbol

        // visit the initializer and add the define_array instruction
        arrayDeclarationStatement.initializer.visit()
        instruction(Opcode.DEFINE_ARRAY, symbol)
    }

    override fun visitAssignmentStatement(assignmentStatement: AssignmentStatement) {
        val vars = assignmentStatement.vars

        // special case for arrays since they need to push the index first when popping a new value
        // arrays are disallowed in multi-assignment statements in earlier steps
        val first = vars.first()
        if (first is LocalVariableExpression && first.index != null) {
            first.index.visit()
        }

        // visit the expressions from the left side
        assignmentStatement.expressions.visit()

        // loop through the variables in reverse
        for (i in vars.indices.reversed()) {
            val variable = vars[i]
            val reference = variable.reference
            if (reference == null) {
                variable.reportError(DiagnosticMessage.SYMBOL_IS_NULL)
                return
            }
            instruction(Opcode.POP_VAR, reference)
        }
    }

    override fun visitExpressionStatement(expressionStatement: ExpressionStatement) {
        val expression = expressionStatement.expression

        // visit the expression
        expression.visit()

        // discard anything that the expression returns
        val types = TupleType.toList(expression.type)
        for (type in types) {
            val baseType = type.baseType
            if (baseType == null) {
                expressionStatement.reportError(DiagnosticMessage.TYPE_HAS_NO_BASETYPE, type)
                return
            }
            instruction(Opcode.DISCARD, baseType)
        }
    }

    override fun visitEmptyStatement(emptyStatement: EmptyStatement) {
        // NO-OP
    }

    override fun visitLocalVariableExpression(localVariableExpression: LocalVariableExpression) {
        val reference = localVariableExpression.reference
        if (reference == null) {
            localVariableExpression.reportError(DiagnosticMessage.SYMBOL_IS_NULL)
            return
        }
        localVariableExpression.lineInstruction()
        localVariableExpression.index?.visit()
        instruction(Opcode.PUSH_VAR, reference)
    }

    override fun visitGameVariableExpression(gameVariableExpression: GameVariableExpression) {
        val reference = gameVariableExpression.reference
        if (reference == null) {
            gameVariableExpression.reportError(DiagnosticMessage.SYMBOL_IS_NULL)
            return
        }
        gameVariableExpression.lineInstruction()
        instruction(Opcode.PUSH_VAR, reference)
    }

    override fun visitConstantVariableExpression(constantVariableExpression: ConstantVariableExpression) {
        val subExpression = constantVariableExpression.subExpression
        if (subExpression == null) {
            constantVariableExpression.reportError(DiagnosticMessage.EXPRESSION_NO_SUBEXPR)
            return
        }
        subExpression.visit()
    }

    override fun visitParenthesizedExpression(parenthesizedExpression: ParenthesizedExpression) {
        parenthesizedExpression.lineInstruction()

        // visit the inner expression
        parenthesizedExpression.expression.visit()
    }

    override fun visitBinaryExpression(binaryExpression: BinaryExpression) {
        val operator = binaryExpression.operator.text
        val opcodes = when (val type = binaryExpression.left.type.baseType) {
            BaseVarType.INTEGER -> INT_OPERATIONS
            BaseVarType.LONG -> LONG_OPERATIONS
            else -> error("No mappings for BaseType: $type")
        }
        val opcode = opcodes[operator] ?: error("No mappings for operator: $operator")

        // visit left side
        binaryExpression.left.visit()

        // visit right side
        binaryExpression.right.visit()

        // add the instruction with the opcode based on the operator
        instruction(opcode, 0)
    }

    override fun visitCalcExpression(calcExpression: CalcExpression) {
        calcExpression.lineInstruction()
        calcExpression.expression.visit()
    }

    override fun visitCommandCallExpression(commandCallExpression: CommandCallExpression) {
        val symbol = commandCallExpression.symbol
        if (symbol == null) {
            commandCallExpression.reportError(DiagnosticMessage.SYMBOL_IS_NULL)
            return
        }

        // attempt to call the dynamic command handlers code generation (if one exists)
        val name = commandCallExpression.name.text
        dynamicCommands[name]?.run {
            val context = CodeGeneratorContext(this@CodeGenerator, commandCallExpression, diagnostics)
            if (context.generateCode()) {
                // handler says it generated code and doesn't need to fall through to default impl
                return
            }
        }

        commandCallExpression.arguments.visit()
        commandCallExpression.lineInstruction()
        instruction(Opcode.COMMAND, symbol)
    }

    override fun visitProcCallExpression(procCallExpression: ProcCallExpression) {
        val symbol = procCallExpression.symbol
        if (symbol == null) {
            procCallExpression.reportError(DiagnosticMessage.SYMBOL_IS_NULL)
            return
        }
        procCallExpression.arguments.visit()
        procCallExpression.lineInstruction()
        instruction(Opcode.GOSUB, symbol)
    }

    override fun visitClientScriptExpression(clientScriptExpression: ClientScriptExpression) {
        val symbol = clientScriptExpression.symbol as? ScriptSymbol.ClientScriptSymbol
        if (symbol == null) {
            clientScriptExpression.reportError(DiagnosticMessage.SYMBOL_IS_NULL)
            return
        }

        // convert the parameter type to a list and generate a string with all the type char codes combined
        val argumentTypes = TupleType.toList(symbol.parameters)
        var argumentTypesShort = argumentTypes.map { it.code }.joinToString("")

        // safety check in case there was a type with no char code defined
        require(argumentTypesShort.length == clientScriptExpression.arguments.size)

        // write the clientscript reference and arguments
        instruction(Opcode.PUSH_CONSTANT, symbol)
        clientScriptExpression.arguments.visit()

        // optionally handle the transmit list if it exists
        if (clientScriptExpression.transmitList.isNotEmpty()) {
            clientScriptExpression.transmitList.visit()

            // write the "type" char that signifies to read the transmit list
            argumentTypesShort += "Y"

            // write the number of things in the transmit list
            instruction(Opcode.PUSH_CONSTANT, clientScriptExpression.transmitList.size)
        }

        // write the argument types
        instruction(Opcode.PUSH_CONSTANT, argumentTypesShort)
    }

    override fun visitIntegerLiteral(integerLiteral: IntegerLiteral) {
        integerLiteral.lineInstruction()
        instruction(Opcode.PUSH_CONSTANT, integerLiteral.value)
    }

    override fun visitCoordLiteral(coordLiteral: CoordLiteral) {
        coordLiteral.lineInstruction()
        instruction(Opcode.PUSH_CONSTANT, coordLiteral.value)
    }

    override fun visitBooleanLiteral(booleanLiteral: BooleanLiteral) {
        booleanLiteral.lineInstruction()
        instruction(Opcode.PUSH_CONSTANT, if (booleanLiteral.value) 1 else 0)
    }

    override fun visitCharacterLiteral(characterLiteral: CharacterLiteral) {
        characterLiteral.lineInstruction()
        instruction(Opcode.PUSH_CONSTANT, characterLiteral.value.code)
    }

    override fun visitNullLiteral(nullLiteral: NullLiteral) {
        nullLiteral.lineInstruction()

        if (nullLiteral.type.baseType == BaseVarType.LONG) {
            instruction(Opcode.PUSH_CONSTANT, -1L)
            return
        }
        instruction(Opcode.PUSH_CONSTANT, -1)
    }

    override fun visitStringLiteral(stringLiteral: StringLiteral) {
        stringLiteral.lineInstruction()

        if (stringLiteral.type == PrimitiveType.GRAPHIC) {
            val symbol = stringLiteral.reference
            if (symbol == null) {
                stringLiteral.reportError(DiagnosticMessage.SYMBOL_IS_NULL)
                return
            }

            instruction(Opcode.PUSH_CONSTANT, symbol)
            return
        } else if (stringLiteral.type is MetaType.ClientScript) {
            val subExpression = stringLiteral.subExpression
            if (subExpression == null) {
                stringLiteral.reportError(DiagnosticMessage.EXPRESSION_NO_SUBEXPR)
                return
            }
            subExpression.visit()
            return
        }
        instruction(Opcode.PUSH_CONSTANT, stringLiteral.value)
    }

    override fun visitJoinedStringExpression(joinedStringExpression: JoinedStringExpression) {
        joinedStringExpression.parts.visit()
        joinedStringExpression.lineInstruction()
        instruction(Opcode.JOIN_STRING, joinedStringExpression.parts.size)
    }

    override fun visitIdentifier(identifier: Identifier) {
        val reference = identifier.reference
        if (reference == null) {
            identifier.reportError(DiagnosticMessage.SYMBOL_IS_NULL)
            return
        }

        identifier.lineInstruction()

        // add the instruction based on reference type
        if (reference is ScriptSymbol.ClientScriptSymbol && reference.trigger == ClientTriggerType.COMMAND) {
            // commands can be referenced by just their name if they have no arguments
            instruction(Opcode.COMMAND, reference)
        } else {
            // default to just pushing the symbol as a constant
            instruction(Opcode.PUSH_CONSTANT, reference)
        }
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
    internal fun Node?.visit() {
        if (this == null) {
            return
        }
        accept(this@CodeGenerator)
    }

    /**
     * Calls [Node.accept] on all nodes in a list.
     */
    internal fun List<Node>?.visit() {
        if (this == null) {
            return
        }
        for (n in this) {
            n.visit()
        }
    }

    private companion object {
        /**
         * The operator for logical and.
         */
        private const val LOGICAL_AND = "&"

        /**
         * The operator for logical or.
         */
        private const val LOGICAL_OR = "|"

        /**
         * Array of possible logical operators.
         */
        private val LOGICAL_OPERATORS = arrayOf(LOGICAL_AND, LOGICAL_OR)

        /**
         * Mapping of operators to their branch opcode for int based types.
         */
        private val INT_BRANCHES = mapOf(
            "=" to Opcode.BRANCH_EQUALS,
            "!" to Opcode.BRANCH_NOT,
            "<" to Opcode.BRANCH_LESS_THAN,
            ">" to Opcode.BRANCH_GREATER_THAN,
            "<=" to Opcode.BRANCH_LESS_THAN_OR_EQUALS,
            ">=" to Opcode.BRANCH_GREATER_THAN_OR_EQUALS,
        )

        /**
         * Mapping of operators to their branch opcode for object based types.
         */
        private val OBJ_BRANCHES = mapOf(
            "=" to Opcode.OBJ_BRANCH_EQUALS,
            "!" to Opcode.OBJ_BRANCH_NOT,
        )

        /**
         * Mapping of operators to their branch opcode for long based types.
         */
        private val LONG_BRANCHES = mapOf(
            "=" to Opcode.LONG_BRANCH_EQUALS,
            "!" to Opcode.LONG_BRANCH_NOT,
            "<" to Opcode.LONG_BRANCH_LESS_THAN,
            ">" to Opcode.LONG_BRANCH_GREATER_THAN,
            "<=" to Opcode.LONG_BRANCH_LESS_THAN_OR_EQUALS,
            ">=" to Opcode.LONG_BRANCH_GREATER_THAN_OR_EQUALS,
        )

        /**
         * A map for getting the branch instructions based on a base type.
         */
        private val BRANCH_MAPPINGS = mapOf(
            BaseVarType.INTEGER to INT_BRANCHES,
            BaseVarType.STRING to OBJ_BRANCHES,
            BaseVarType.LONG to LONG_BRANCHES,
        )

        /**
         * Mapping of operators to their math opcode for int based types.
         */
        private val INT_OPERATIONS = mapOf(
            "+" to Opcode.ADD,
            "-" to Opcode.SUB,
            "*" to Opcode.MULTIPLY,
            "/" to Opcode.DIVIDE,
            "%" to Opcode.MODULO,
            "&" to Opcode.AND,
            "|" to Opcode.OR,
        )

        /**
         * Mapping of operators to their math opcode for long based types.
         */
        private val LONG_OPERATIONS = mapOf(
            "+" to Opcode.LONG_ADD,
            "-" to Opcode.LONG_SUB,
            "*" to Opcode.LONG_MULTIPLY,
            "/" to Opcode.LONG_DIVIDE,
            "%" to Opcode.LONG_MODULO,
            "&" to Opcode.LONG_AND,
            "|" to Opcode.LONG_OR,
        )
    }
}
