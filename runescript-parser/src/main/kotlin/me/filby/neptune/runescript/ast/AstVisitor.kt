package me.filby.neptune.runescript.ast

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
import me.filby.neptune.runescript.ast.expr.StringPart
import me.filby.neptune.runescript.ast.expr.VariableExpression
import me.filby.neptune.runescript.ast.statement.ArrayDeclarationStatement
import me.filby.neptune.runescript.ast.statement.AssignmentStatement
import me.filby.neptune.runescript.ast.statement.BlockStatement
import me.filby.neptune.runescript.ast.statement.DeclarationStatement
import me.filby.neptune.runescript.ast.statement.EmptyStatement
import me.filby.neptune.runescript.ast.statement.ExpressionStatement
import me.filby.neptune.runescript.ast.statement.IfStatement
import me.filby.neptune.runescript.ast.statement.ReturnStatement
import me.filby.neptune.runescript.ast.statement.Statement
import me.filby.neptune.runescript.ast.statement.SwitchCase
import me.filby.neptune.runescript.ast.statement.SwitchStatement
import me.filby.neptune.runescript.ast.statement.WhileStatement

public interface AstVisitor<R> {
    public fun visitScriptFile(scriptFile: ScriptFile): R = visitNode(scriptFile)

    public fun visitScript(script: Script): R = visitNode(script)

    public fun visitParameter(parameter: Parameter): R = visitNode(parameter)

    public fun visitBlockStatement(blockStatement: BlockStatement): R = visitStatement(blockStatement)

    public fun visitReturnStatement(returnStatement: ReturnStatement): R = visitStatement(returnStatement)

    public fun visitIfStatement(ifStatement: IfStatement): R = visitStatement(ifStatement)

    public fun visitWhileStatement(whileStatement: WhileStatement): R = visitStatement(whileStatement)

    public fun visitSwitchStatement(switchStatement: SwitchStatement): R = visitStatement(switchStatement)

    public fun visitSwitchCase(switchCase: SwitchCase): R = visitNode(switchCase)

    public fun visitDeclarationStatement(declarationStatement: DeclarationStatement): R =
        visitStatement(declarationStatement)

    public fun visitArrayDeclarationStatement(arrayDeclarationStatement: ArrayDeclarationStatement): R =
        visitStatement(arrayDeclarationStatement)

    public fun visitAssignmentStatement(assignmentStatement: AssignmentStatement): R =
        visitStatement(assignmentStatement)

    public fun visitExpressionStatement(expressionStatement: ExpressionStatement): R =
        visitStatement(expressionStatement)

    public fun visitEmptyStatement(emptyStatement: EmptyStatement): R = visitStatement(emptyStatement)

    public fun visitStatement(statement: Statement): R = visitNode(statement)

    public fun visitParenthesizedExpression(parenthesizedExpression: ParenthesizedExpression): R =
        visitExpression(parenthesizedExpression)

    public fun visitConditionExpression(conditionExpression: ConditionExpression): R =
        visitBinaryExpression(conditionExpression)

    public fun visitArithmeticExpression(arithmeticExpression: ArithmeticExpression): R =
        visitBinaryExpression(arithmeticExpression)

    public fun visitBinaryExpression(binaryExpression: BinaryExpression): R = visitExpression(binaryExpression)

    public fun visitCalcExpression(calcExpression: CalcExpression): R = visitExpression(calcExpression)

    public fun visitCommandCallExpression(commandCallExpression: CommandCallExpression): R =
        visitCallExpression(commandCallExpression)

    public fun visitProcCallExpression(procCallExpression: ProcCallExpression): R =
        visitCallExpression(procCallExpression)

    public fun visitJumpCallExpression(jumpCallExpression: JumpCallExpression): R =
        visitCallExpression(jumpCallExpression)

    public fun visitCallExpression(callExpression: CallExpression): R = visitExpression(callExpression)

    public fun visitClientScriptExpression(clientScriptExpression: ClientScriptExpression): R =
        visitExpression(clientScriptExpression)

    public fun visitLocalVariableExpression(localVariableExpression: LocalVariableExpression): R =
        visitVariableExpression(localVariableExpression)

    public fun visitGameVariableExpression(gameVariableExpression: GameVariableExpression): R =
        visitVariableExpression(gameVariableExpression)

    public fun visitConstantVariableExpression(constantVariableExpression: ConstantVariableExpression): R =
        visitVariableExpression(constantVariableExpression)

    public fun visitVariableExpression(variableExpression: VariableExpression): R = visitExpression(variableExpression)

    public fun visitExpression(expression: Expression): R = visitNode(expression)

    public fun visitIntegerLiteral(integerLiteral: IntegerLiteral): R = visitLiteral(integerLiteral)

    public fun visitCoordLiteral(coordLiteral: CoordLiteral): R = visitLiteral(coordLiteral)

    public fun visitBooleanLiteral(booleanLiteral: BooleanLiteral): R = visitLiteral(booleanLiteral)

    public fun visitCharacterLiteral(characterLiteral: CharacterLiteral): R = visitLiteral(characterLiteral)

    public fun visitNullLiteral(nullLiteral: NullLiteral): R = visitLiteral(nullLiteral)

    public fun visitStringLiteral(stringLiteral: StringLiteral): R = visitLiteral(stringLiteral)

    public fun visitLiteral(literal: Literal<*>): R = visitExpression(literal)

    public fun visitJoinedStringExpression(joinedStringExpression: JoinedStringExpression): R =
        visitExpression(joinedStringExpression)

    public fun visitJoinedStringPart(stringPart: StringPart): R = visitNode(stringPart)

    public fun visitIdentifier(identifier: Identifier): R = visitExpression(identifier)

    public fun visitToken(token: Token): R = visitNode(token)

    public fun visitNode(node: Node): R =
        throw UnsupportedOperationException("not implemented: ${node::class.simpleName}")
}
