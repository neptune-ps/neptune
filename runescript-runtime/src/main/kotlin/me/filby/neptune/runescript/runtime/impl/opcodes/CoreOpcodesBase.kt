package me.filby.neptune.runescript.runtime.impl.opcodes

import me.filby.neptune.runescript.runtime.impl.Instruction
import me.filby.neptune.runescript.runtime.impl.ScriptProvider
import me.filby.neptune.runescript.runtime.state.GosubStackFramePool
import me.filby.neptune.runescript.runtime.state.ScriptStackType
import me.filby.neptune.runescript.runtime.state.ScriptState

/**
 * A base implementation of the core opcodes needed by the runtime.
 *
 * If using for a game, [_push_game] and [_pop_game] should be implemented manually.
 *
 * @see MathOpcodesBase
 */
@Suppress("FunctionName")
public open class CoreOpcodesBase<T : ScriptState>(private val scriptProvider: ScriptProvider) {
    private val gosubStackFramePool = GosubStackFramePool()

    @Instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT)
    public open fun T._push_constant_int() {
        pushInt(intOperand)
    }

    @Instruction(BaseCoreOpcodes.PUSH_CONSTANT_STRING)
    public open fun T._push_constant_string() {
        pushObj(objOperand)
    }

    @Instruction(BaseCoreOpcodes.PUSH_CONSTANT_LONG)
    public open fun T._push_constant_long() {
        pushLong(objOperand as Long)
    }

    @Instruction(BaseCoreOpcodes.PUSH_LOCAL)
    public open fun T._push_local() {
        val index = intOperand shr 16
        when (ScriptStackType.forId(intOperand and 0xFFFF)) {
            ScriptStackType.INTEGER -> pushInt(intLocals[index])
            ScriptStackType.OBJECT -> pushObj(objLocals[index])
            ScriptStackType.LONG -> pushLong(longLocals[index])
        }
    }

    @Instruction(BaseCoreOpcodes.POP_LOCAL)
    public open fun T._pop_local() {
        val index = intOperand shr 16
        when (ScriptStackType.forId(intOperand and 0xFFFF)) {
            ScriptStackType.INTEGER -> intLocals[index] = popInt()
            ScriptStackType.OBJECT -> objLocals[index] = popObj()
            ScriptStackType.LONG -> longLocals[index] = popLong()
        }
    }

    @Instruction(BaseCoreOpcodes.PUSH_GAME)
    public open fun T._push_game() {
        error("push_game has not been implemented")
    }

    @Instruction(BaseCoreOpcodes.POP_GAME)
    public open fun T._pop_game() {
        error("pop_game has not been implemented")
    }

    @Instruction(BaseCoreOpcodes.BRANCH)
    public open fun T._branch() {
        pc += intOperand
    }

    // int branches

    @Instruction(BaseCoreOpcodes.BRANCH_NOT)
    public open fun T._branch_not(a: Int, b: Int) {
        if (a != b) {
            pc += intOperand
        }
    }

    @Instruction(BaseCoreOpcodes.BRANCH_EQUALS)
    public open fun T._branch_equals(a: Int, b: Int) {
        if (a == b) {
            pc += intOperand
        }
    }

    @Instruction(BaseCoreOpcodes.BRANCH_LESS_THAN)
    public open fun T._branch_less_than(a: Int, b: Int) {
        if (a < b) {
            pc += intOperand
        }
    }

    @Instruction(BaseCoreOpcodes.BRANCH_GREATER_THAN)
    public open fun T._branch_greater_than(a: Int, b: Int) {
        if (a > b) {
            pc += intOperand
        }
    }

    @Instruction(BaseCoreOpcodes.BRANCH_LESS_THAN_OR_EQUALS)
    public open fun T._branch_less_than_or_equals(a: Int, b: Int) {
        if (a <= b) {
            pc += intOperand
        }
    }

    @Instruction(BaseCoreOpcodes.BRANCH_GREATER_THAN_OR_EQUALS)
    public open fun T._branch_greater_than_or_equals(a: Int, b: Int) {
        if (a >= b) {
            pc += intOperand
        }
    }

    // long branches

    @Instruction(BaseCoreOpcodes.LONG_BRANCH_NOT)
    public open fun T._branch_not(a: Long, b: Long) {
        if (a != b) {
            pc += intOperand
        }
    }

    @Instruction(BaseCoreOpcodes.LONG_BRANCH_EQUALS)
    public open fun T._branch_equals(a: Long, b: Long) {
        if (a == b) {
            pc += intOperand
        }
    }

    @Instruction(BaseCoreOpcodes.LONG_BRANCH_LESS_THAN)
    public open fun T._branch_less_than(a: Long, b: Long) {
        if (a < b) {
            pc += intOperand
        }
    }

    @Instruction(BaseCoreOpcodes.LONG_BRANCH_GREATER_THAN)
    public open fun T._branch_greater_than(a: Long, b: Long) {
        if (a > b) {
            pc += intOperand
        }
    }

    @Instruction(BaseCoreOpcodes.LONG_BRANCH_LESS_THAN_OR_EQUALS)
    public open fun T._branch_less_than_or_equals(a: Long, b: Long) {
        if (a <= b) {
            pc += intOperand
        }
    }

    @Instruction(BaseCoreOpcodes.LONG_BRANCH_GREATER_THAN_OR_EQUALS)
    public open fun T._branch_greater_than_or_equals(a: Long, b: Long) {
        if (a >= b) {
            pc += intOperand
        }
    }

    // object branches

    @Instruction(BaseCoreOpcodes.OBJ_BRANCH_NOT)
    public open fun T._branch_not(a: Any, b: Any) {
        if (a != b) {
            pc += intOperand
        }
    }

    @Instruction(BaseCoreOpcodes.OBJ_BRANCH_EQUALS)
    public open fun T._branch_equals(a: Any, b: Any) {
        if (a == b) {
            pc += intOperand
        }
    }

    @Instruction(BaseCoreOpcodes.JOIN_STRING)
    public open fun T._join_string() {
        pushObj(
            buildString {
                val strings = Array<String>(intOperand) { popObj() }
                for (i in 0 until intOperand) {
                    append(strings[intOperand - i - 1])
                }
            }
        )
    }

    @Instruction(BaseCoreOpcodes.POP_DISCARD)
    public open fun T._pop_discard() {
        when (ScriptStackType.forId(intOperand)) {
            ScriptStackType.INTEGER -> popInt()
            ScriptStackType.OBJECT -> popObj()
            ScriptStackType.LONG -> popLong()
        }
    }

    @Instruction(BaseCoreOpcodes.GOSUB_WITH_PARAMS)
    public open fun T._gosub_with_params() {
        // lookup the script that is being called
        val proc = scriptProvider.get(intOperand)

        // only allow up to 100 frames
        if (frames.size >= 100) {
            throw StackOverflowError()
        }

        // set up the locals and pop parameters
        val intLocals = IntArray(proc.intLocalCount)
        val longLocals = LongArray(proc.longLocalCount)
        val objLocals = arrayOfNulls<Any>(proc.objLocalCount)
        for (i in 0 until proc.intParameterCount) {
            intLocals[proc.intParameterCount - i - 1] = popInt()
        }
        for (i in 0 until proc.longParameterCount) {
            longLocals[proc.longParameterCount - i - 1] = popLong()
        }
        for (i in 0 until proc.objParameterCount) {
            objLocals[proc.objParameterCount - i - 1] = popObj()
        }

        // set up the gosub frame
        val frame = gosubStackFramePool.pop()
        frame.setup(this)
        frames.addLast(frame)

        // set up the state for new script
        this.script = proc
        this.pc = -1
        this.intLocals = intLocals
        this.longLocals = longLocals
        this.objLocals = objLocals
    }

    @Instruction(BaseCoreOpcodes.RETURN)
    public open fun T._return() {
        if (frames.isEmpty()) {
            // no more gosub frames means this is the top level script call, and it is returning
            // mark as finished to stop execution
            execution = ScriptState.ExecutionState.FINISHED
            return
        }

        val frame = frames.removeLast()
        script = frame.script
        pc = frame.pc
        intLocals = frame.intLocals
        longLocals = frame.longLocals
        objLocals = frame.objLocals

        // push the frame back to the pool
        gosubStackFramePool.push(frame)
    }

    @Instruction(BaseCoreOpcodes.SWITCH)
    public open fun T._switch(key: Int) {
        val table = script.switchTables[intOperand]
        val result = table[key] ?: return
        pc += result
    }

    // TODO remove the below implementations

    @Instruction(BaseCoreOpcodes.TOSTRING)
    public open fun T._tostring(value: Int) {
        pushObj(value.toString())
    }

    @Instruction(BaseCoreOpcodes.TOSTRING_LONG)
    public open fun T._tostring_long(value: Long) {
        pushObj(value.toString())
    }

    @Instruction(BaseCoreOpcodes.STRING_LENGTH)
    public open fun T._string_length(string: String) {
        pushInt(string.length)
    }

    @Instruction(BaseCoreOpcodes.SUBSTRING)
    public open fun T._substring(string: String, start: Int, end: Int) {
        pushObj(string.substring(start, end))
    }

    @Instruction(BaseCoreOpcodes.STRING_INDEXOF_STRING)
    public open fun T._string_indexof_string(string: String, subString: String, fromIndex: Int) {
        pushInt(string.indexOf(subString, fromIndex))
    }

    @Instruction(BaseCoreOpcodes.APPEND)
    public open fun T._append(string: String, otherString: String) {
        pushObj(string + otherString)
    }
}
