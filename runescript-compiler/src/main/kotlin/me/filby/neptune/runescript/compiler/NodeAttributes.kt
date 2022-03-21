package me.filby.neptune.runescript.compiler

import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Parameter
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.expr.Expression
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
 * The type that the expression would evaluate to.
 */
internal var Expression.type by Node.attribute<Type>("type")

/**
 * The scope defined for the node. This should only ever be set for node types that
 * would create a new scope.
 */
internal var Node.scope by Node.attribute<SymbolTable>("scope")
