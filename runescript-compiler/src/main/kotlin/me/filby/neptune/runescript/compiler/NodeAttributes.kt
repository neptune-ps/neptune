package me.filby.neptune.runescript.compiler

import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Parameter
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.LocalVariableExpression
import me.filby.neptune.runescript.ast.statement.DeclarationStatement
import me.filby.neptune.runescript.compiler.symbol.LocalVariableSymbol
import me.filby.neptune.runescript.compiler.symbol.SymbolTable
import me.filby.neptune.runescript.compiler.type.Type

/**
 * The script parameter type(s) if it returns any.
 */
internal var Script.parameterType by Node.attributeOrNull<Type>("parameterType")

/**
 * The script return type(s) if it returns any.
 */
internal var Script.returnType by Node.attributeOrNull<Type>("returnType")

/**
 * The defined type of the parameter.
 */
internal var Parameter.type by Node.attribute<Type>("type")

/**
 * The symbol that the statement declared.
 */
internal var DeclarationStatement.symbol by Node.attribute<LocalVariableSymbol>("symbol")

/**
 * The symbol that the node references.
 */
internal var LocalVariableExpression.reference by Node.attributeOrNull<LocalVariableSymbol>("reference")

/**
 * The type that the expression would evaluate to.
 */
internal var Expression.type by Node.attribute<Type>("type")

/**
 * The scope defined for the node. This should only ever be set for node types that
 * would create a new scope.
 */
internal var Node.scope by Node.attribute<SymbolTable>("scope")
