package me.filby.neptune.runescript.compiler

import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Parameter
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.ScriptFile
import me.filby.neptune.runescript.ast.expr.CallExpression
import me.filby.neptune.runescript.ast.expr.ConstantVariableExpression
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.expr.StringLiteral
import me.filby.neptune.runescript.ast.expr.VariableExpression
import me.filby.neptune.runescript.ast.statement.ArrayDeclarationStatement
import me.filby.neptune.runescript.ast.statement.BlockStatement
import me.filby.neptune.runescript.ast.statement.DeclarationStatement
import me.filby.neptune.runescript.ast.statement.SwitchCase
import me.filby.neptune.runescript.ast.statement.SwitchStatement
import me.filby.neptune.runescript.compiler.symbol.BasicSymbol
import me.filby.neptune.runescript.compiler.symbol.LocalVariableSymbol
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.Symbol
import me.filby.neptune.runescript.compiler.symbol.SymbolTable
import me.filby.neptune.runescript.compiler.trigger.TriggerType
import me.filby.neptune.runescript.compiler.type.Type

/**
 * A set of [ScriptSymbol] that the file depends on.
 */
internal var ScriptFile.dependencies by Node.attribute<MutableSet<ScriptSymbol>>("dependencies") { mutableSetOf() }

/**
 * The [ScriptSymbol] associated with the script.
 */
internal var Script.symbol by Node.attribute<ScriptSymbol>("symbol")

/**
 * The scripts defined trigger type.
 */
internal var Script.triggerType by Node.attribute<TriggerType>("triggerType")

/**
 * The symbol that is associated with the subject of the script.
 */
internal var Script.subjectReference by Node.attributeOrNull<BasicSymbol>("subjectReference")

/**
 * The script parameter type(s) if it returns any.
 */
internal var Script.parameterType by Node.attribute<Type>("parameterType")

/**
 * The script return type(s) if it returns any.
 */
internal var Script.returnType by Node.attribute<Type>("returnType")

/**
 * The root [SymbolTable] of the script.
 */
internal var Script.scope by Node.attribute<SymbolTable>("block")

/**
 * The symbol that the parameter declares.
 */
internal var Parameter.symbol by Node.attribute<LocalVariableSymbol>("symbol")

/**
 * The [SymbolTable] of the block.
 */
internal var BlockStatement.scope by Node.attribute<SymbolTable>("scope")

/**
 * The type the switch statement accepts.
 */
internal var SwitchStatement.type by Node.attribute<Type>("type")

/**
 * The default case assigned to the statement.
 */
internal var SwitchStatement.defaultCase by Node.attributeOrNull<SwitchCase>("defaultCase")

/**
 * The [SymbolTable] of the case block.
 */
internal var SwitchCase.scope by Node.attribute<SymbolTable>("scope")

/**
 * The symbol that the statement declared.
 */
internal var DeclarationStatement.symbol by Node.attribute<LocalVariableSymbol>("symbol")

/**
 * The symbol that the statement defined.
 */
internal var ArrayDeclarationStatement.symbol by Node.attribute<LocalVariableSymbol>("symbol")

/**
 * The expression that the constant value parsed to.
 */
internal var ConstantVariableExpression.subExpression by Node.attributeOrNull<Expression>("subExpression")

/**
 * The symbol that the variable references.
 */
public var VariableExpression.reference: Symbol? by Node.attributeOrNull("reference")

/**
 * The symbol that the expression references.
 */
public var CallExpression.reference: Symbol? by Node.attributeOrNull("symbol")

/**
 * An optional symbol assigned to [StringLiteral]s if the string is meant to represent some other reference.
 */
public var StringLiteral.reference: Symbol? by Node.attributeOrNull("symbol")

/**
 * An optional expression that was parsed from within the string literal.
 */
public var StringLiteral.subExpression: Expression? by Node.attributeOrNull("subExpression")

/**
 * The symbol the identifier references.
 */
public var Identifier.reference: Symbol? by Node.attributeOrNull("reference")

/**
 * The type that the expression would evaluate to.
 *
 * @see Expression.nullableType
 */
public var Expression.type: Type by Node.attribute("type")

/**
 * The type that the expression would evaluate to, or `null`.
 *
 * @see Expression.type
 */
public var Expression.nullableType: Type? by Node.attributeOrNull("type")

/**
 * Allows parents of a node to define the expected type to help with identifier ambiguity.
 */
public var Expression.typeHint: Type? by Node.attributeOrNull("typeHint")
