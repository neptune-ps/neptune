package me.filby.neptune.runescript.compiler.writer

import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.symbol.LocalVariableSymbol
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.wrapped.ArrayType

/**
 * A basic implementation of [ScriptWriter] with some utility functions for writing
 * a script.
 */
public abstract class BaseScriptWriter : ScriptWriter {
    // RuneScript.LocalTable helpers

    /**
     * Returns the total number of parameters with a base var type of [baseType].
     */
    public fun RuneScript.LocalTable.getParameterCount(baseType: BaseVarType): Int {
        return parameters.count { it.type.baseType == baseType }
    }

    /**
     * Returns the total number of local variables with a base var type of [baseType].
     */
    public fun RuneScript.LocalTable.getLocalCount(baseType: BaseVarType): Int {
        return all.count { it.type.baseType == baseType }
    }

    /**
     * Finds the unique identifier for the given [local] variable.
     */
    public fun RuneScript.LocalTable.getVariableId(local: LocalVariableSymbol): Int {
        val isArray = local.type is ArrayType
        return all.asSequence()
            .filter { isArray && it.type is ArrayType || !isArray && it.type.baseType == local.type.baseType }
            .indexOf(local)
    }
}
