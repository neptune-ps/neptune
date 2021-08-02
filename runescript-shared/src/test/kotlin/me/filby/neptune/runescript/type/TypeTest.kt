package me.filby.neptune.runescript.type

import kotlin.test.Test
import kotlin.test.assertContentEquals

class TypeTest {
    @Test
    fun testTupleType() {
        val type = TupleType(
            TupleType(
                PrimitiveType.INT,
                PrimitiveType.STRING,
                PrimitiveType.STRING,
            ),
            PrimitiveType.STRING,
            TupleType(
                PrimitiveType.INT,
                PrimitiveType.STRING,
            ),
            PrimitiveType.LONG,
            PrimitiveType.BOOLEAN,
            TupleType(
                TupleType(
                    PrimitiveType.LONG,
                    PrimitiveType.BOOLEAN,
                ),
                PrimitiveType.INT
            )
        )

        // test if tuple type flattens properly
        assertContentEquals(
            arrayOf(
                PrimitiveType.INT,
                PrimitiveType.STRING,
                PrimitiveType.STRING,
                PrimitiveType.STRING,
                PrimitiveType.INT,
                PrimitiveType.STRING,
                PrimitiveType.LONG,
                PrimitiveType.BOOLEAN,
                PrimitiveType.LONG,
                PrimitiveType.BOOLEAN,
                PrimitiveType.INT
            ),
            type.children
        )
    }
}
