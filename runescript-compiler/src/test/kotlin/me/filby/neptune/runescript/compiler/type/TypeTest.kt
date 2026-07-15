package me.filby.neptune.runescript.compiler.type

import me.filby.neptune.runescript.compiler.ScriptVarType
import kotlin.test.Test
import kotlin.test.assertContentEquals

class TypeTest {
    @Test
    fun testTupleType() {
        val type = TupleType(
            TupleType(
                ScriptVarType.INT,
                ScriptVarType.STRING,
                ScriptVarType.STRING,
            ),
            ScriptVarType.STRING,
            TupleType(
                ScriptVarType.INT,
                ScriptVarType.STRING,
            ),
            ScriptVarType.LONG,
            ScriptVarType.BOOLEAN,
            TupleType(
                TupleType(
                    ScriptVarType.LONG,
                    ScriptVarType.BOOLEAN,
                ),
                ScriptVarType.INT,
            ),
        )

        // test if tuple type flattens properly
        assertContentEquals(
            arrayOf(
                ScriptVarType.INT,
                ScriptVarType.STRING,
                ScriptVarType.STRING,
                ScriptVarType.STRING,
                ScriptVarType.INT,
                ScriptVarType.STRING,
                ScriptVarType.LONG,
                ScriptVarType.BOOLEAN,
                ScriptVarType.LONG,
                ScriptVarType.BOOLEAN,
                ScriptVarType.INT,
            ),
            type.children,
        )
    }
}
