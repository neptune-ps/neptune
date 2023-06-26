package me.filby.neptune.runescript.runtime

import me.filby.neptune.runescript.runtime.impl.opcodes.BaseCoreOpcodes

internal class ScriptBuilder private constructor() {
    private var intParameterCount = 0

    private var stringParameterCount = 0

    private var longParameterCount = 0

    private var intLocalCount = 0

    private var objLocalCount = 0

    private var longLocalCount = 0

    private val opcodes = mutableListOf<Int>()

    private val intOperands = mutableListOf<Int>()

    private val objOperands = mutableListOf<Any?>()

    fun createIntParam(): Int {
        intParameterCount++
        return createIntLocal()
    }

    fun createStringParam(): Int {
        stringParameterCount++
        return createObjLocal()
    }

    fun createLongParam(): Int {
        longParameterCount++
        return createLongLocal()
    }

    fun createIntLocal(): Int {
        return intLocalCount++
    }

    fun createObjLocal(): Int {
        return objLocalCount++
    }

    fun createLongLocal(): Int {
        return longLocalCount++
    }

    fun instruction(opcode: Int, operand: Int = 0) {
        opcodes += opcode
        intOperands += operand
        objOperands += null
    }

    fun instruction(opcode: Int, operand: Any) {
        opcodes += opcode
        intOperands += 0
        objOperands += operand
    }

    fun build(): Script {
        check(opcodes.isNotEmpty() && opcodes.last() == BaseCoreOpcodes.RETURN) {
            "`return` is required to be the last instruction"
        }

        return Script(
            null,
            opcodes.toIntArray(),
            intParameterCount,
            stringParameterCount,
            longParameterCount,
            intLocalCount,
            objLocalCount,
            longLocalCount,
            intOperands.toIntArray(),
            objOperands.toTypedArray(),
            emptyList()
        )
    }

    companion object {
        operator fun invoke(builder: ScriptBuilder.() -> Unit): Script {
            val b = ScriptBuilder()
            builder(b)
            return b.build()
        }
    }
}
