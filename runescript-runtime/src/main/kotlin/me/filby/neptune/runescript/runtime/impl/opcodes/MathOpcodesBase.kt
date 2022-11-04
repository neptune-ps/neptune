package me.filby.neptune.runescript.runtime.impl.opcodes

import me.filby.neptune.runescript.runtime.impl.Instruction
import me.filby.neptune.runescript.runtime.state.ScriptState

/**
 * A class that implements the core math operations needed by the runtime.
 *
 * This implements the following operations for [Int] and [Long]:
 *  - Addition
 *  - Subtraction
 *  - Multiplication
 *  - Division
 *  - Modulo (actually is remainder)
 *  - Bitwise OR
 *  - Bitwise AND
 */
@Suppress("FunctionName")
public open class MathOpcodesBase<T : ScriptState> {
    // int math

    @Instruction(BaseCoreOpcodes.ADD)
    public open fun T._add(a: Int, b: Int) {
        pushInt(a + b)
    }

    @Instruction(BaseCoreOpcodes.SUB)
    public open fun T._sub(a: Int, b: Int) {
        pushInt(a - b)
    }

    @Instruction(BaseCoreOpcodes.MULTIPLY)
    public open fun T._multiply(a: Int, b: Int) {
        pushInt(a * b)
    }

    @Instruction(BaseCoreOpcodes.DIVIDE)
    public open fun T._div(a: Int, b: Int) {
        pushInt(a / b)
    }

    @Instruction(BaseCoreOpcodes.MODULO)
    public open fun T._mod(a: Int, b: Int) {
        pushInt(a % b)
    }

    @Instruction(BaseCoreOpcodes.OR)
    public open fun T._or(a: Int, b: Int) {
        pushInt(a or b)
    }

    @Instruction(BaseCoreOpcodes.AND)
    public open fun T._and(a: Int, b: Int) {
        pushInt(a and b)
    }

    // long math

    @Instruction(BaseCoreOpcodes.ADD_LONG)
    public open fun T._add(a: Long, b: Long) {
        pushLong(a + b)
    }

    @Instruction(BaseCoreOpcodes.SUB_LONG)
    public open fun T._sub(a: Long, b: Long) {
        pushLong(a - b)
    }

    @Instruction(BaseCoreOpcodes.MULTIPLY_LONG)
    public open fun T._multiply(a: Long, b: Long) {
        pushLong(a * b)
    }

    @Instruction(BaseCoreOpcodes.DIVIDE_LONG)
    public open fun T._div(a: Long, b: Long) {
        pushLong(a / b)
    }

    @Instruction(BaseCoreOpcodes.MODULO_LONG)
    public open fun T._mod(a: Long, b: Long) {
        pushLong(a % b)
    }

    @Instruction(BaseCoreOpcodes.OR_LONG)
    public open fun T._or(a: Long, b: Long) {
        pushLong(a or b)
    }

    @Instruction(BaseCoreOpcodes.AND_LONG)
    public open fun T._and(a: Long, b: Long) {
        pushLong(a and b)
    }
}
