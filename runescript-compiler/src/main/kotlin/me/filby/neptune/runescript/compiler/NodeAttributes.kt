package me.filby.neptune.runescript.compiler

import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Parameter
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.expr.CallExpression
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.expr.StringLiteral
import me.filby.neptune.runescript.ast.expr.VariableExpression
import me.filby.neptune.runescript.ast.statement.ArrayDeclarationStatement
import me.filby.neptune.runescript.ast.statement.DeclarationStatement
import me.filby.neptune.runescript.ast.statement.SwitchCase
import me.filby.neptune.runescript.ast.statement.SwitchStatement
import me.filby.neptune.runescript.compiler.symbol.BasicSymbol
import me.filby.neptune.runescript.compiler.symbol.LocalVariableSymbol
import me.filby.neptune.runescript.compiler.symbol.Symbol
import me.filby.neptune.runescript.compiler.trigger.TriggerType
import me.filby.neptune.runescript.compiler.type.Type

/**
 * The scripts defined trigger type.
 */
internal var Script.triggerType by Node.attribute<TriggerType>("triggerType")

/**
 * The script parameter type(s) if it returns any.
 */
internal var Script.parameterType by Node.attribute<Type?>("parameterType")

/**
 * The script return type(s) if it returns any.
 */
internal var Script.returnType by Node.attribute<Type>("returnType")

/**
 * The symbol that the parameter declares.
 */
internal var Parameter.symbol by Node.attribute<LocalVariableSymbol>("symbol")

/**
 * The type the switch statement accepts.
 */
internal var SwitchStatement.type by Node.attribute<Type>("type")

/**
 * The default case assigned to the statement.
 */
internal var SwitchStatement.defaultCase by Node.attributeOrNull<SwitchCase>("defaultCase")

/**
 * The symbol that the statement declared.
 */
internal var DeclarationStatement.symbol by Node.attribute<LocalVariableSymbol>("symbol")

/**
 * The symbol that the statement defined.
 */
internal var ArrayDeclarationStatement.symbol by Node.attribute<LocalVariableSymbol>("symbol")

/**
 * The symbol that the variable references.
 */
internal var VariableExpression.reference by Node.attributeOrNull<Symbol>("reference")

/**
 * The symbol that the expression references.
 */
internal var CallExpression.symbol by Node.attributeOrNull<Symbol>("symbol")

/**
 * An optional symbol assigned to [StringLiteral]s if the string is meant to represent a graphic.
 */
internal var StringLiteral.graphicSymbol by Node.attributeOrNull<BasicSymbol>("graphicSymbol")

/**
 * The symbol the identifier references.
 */
internal var Identifier.reference by Node.attributeOrNull<Symbol>("reference")

/**
 * The type that the expression would evaluate to.
 *
 * @see Expression.nullableType
 */
internal var Expression.type by Node.attribute<Type>("type")

/**
 * The type that the expression would evaluate to, or `null`.
 *
 * @see Expression.type
 */
internal var Expression.nullableType by Node.attributeOrNull<Type>("type")

/**
 * Allows parents of a node to define the expected type to help with identifier ambiguity.
 */
internal var Expression.typeHint by Node.attributeOrNull<Type>("typeHint")
