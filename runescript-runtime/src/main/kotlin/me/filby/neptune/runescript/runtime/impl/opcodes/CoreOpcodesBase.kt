package me.filby.neptune.runescript.runtime.impl.opcodes

import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.runtime.Script
import me.filby.neptune.runescript.runtime.ScriptArray
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
public open class CoreOpcodesBase<T : ScriptState>(
    private val scriptProvider: ScriptProvider,
    private val arraysV2: Boolean = false,
) {
    private val gosubStackFramePool = GosubStackFramePool()
    private val localArraySizes = IntArray(5)
    private val localArrays = if (!arraysV2) Array(5) { IntArray(5000) } else emptyArray()

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

    @Instruction(BaseCoreOpcodes.PUSH_CONSTANT_NULL)
    public open fun T._push_constant_null() {
        pushObj(null)
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
            },
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

    @Instruction(BaseCoreOpcodes.GOSUB)
    public open fun T._gosub(id: Int) {
        if (id == -1) {
            error("Attempt gosub with 'null' script.")
        }
        gosub(id)
    }

    @Instruction(BaseCoreOpcodes.GOSUB_WITH_PARAMS)
    public open fun T._gosub_with_params() {
        gosub(intOperand)
    }

    private fun T.gosub(id: Int) {
        if (frames.size >= 100) {
            throw StackOverflowError()
        }

        // set up the gosub frame
        val frame = gosubStackFramePool.pop()
        frame.setup(this)
        frames.addLast(frame)

        // lookup the proc script by id and set up the state with the new script
        val proc = scriptProvider.get(id)
        setupNewScript(proc)
    }

    @Instruction(BaseCoreOpcodes.JUMP)
    public open fun T._jump(id: Int) {
        if (id == -1) {
            error("Attempt jump with 'null' script.")
        }
        jump(id)
    }

    @Instruction(BaseCoreOpcodes.JUMP_WITH_PARAMS)
    public open fun T._jump_with_params() {
        jump(intOperand)
    }

    private fun T.jump(id: Int) {
        // lookup the label script by id and set up the state with the new script
        val label = scriptProvider.get(id)
        setupNewScript(label)

        // release all frames in the stack
        while (frames.isNotEmpty()) {
            gosubStackFramePool.push(frames.removeLast())
        }
    }

    /**
     * Sets up the script state using the [script] information.
     */
    private fun T.setupNewScript(script: Script) {
        // set up the locals and pop parameters
        val intLocals = IntArray(script.intLocalCount)
        val longLocals = LongArray(script.longLocalCount)
        val objLocals = arrayOfNulls<Any>(script.objLocalCount)
        for (i in 0 until script.intParameterCount) {
            intLocals[script.intParameterCount - i - 1] = popInt()
        }
        for (i in 0 until script.longParameterCount) {
            longLocals[script.longParameterCount - i - 1] = popLong()
        }
        for (i in 0 until script.objParameterCount) {
            objLocals[script.objParameterCount - i - 1] = popObj()
        }

        // set up the state for new script
        this.script = script
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

    @Instruction(BaseCoreOpcodes.DEFINE_ARRAY)
    public open fun T._define_array() {
        val arrId = intOperand shr 16
        val typeCode = intOperand and 0xFFFF
        val size = popInt()
        if (size < 0 || size > 5000) {
            throw RuntimeException("Invalid array size: $size Local array: $arrId")
        }

        if (arraysV2) {
            // TODO long support and better handling for defaults
            if (typeCode == 's'.code) {
                objLocals[arrId] = ScriptArray(BaseVarType.STRING, "", size, size)
            } else if (typeCode == 'i'.code || typeCode == '1'.code) {
                objLocals[arrId] = ScriptArray(BaseVarType.INTEGER, 0, size, size)
            } else {
                objLocals[arrId] = ScriptArray(BaseVarType.INTEGER, -1, size, size)
            }
        } else {
            val defaultValue = if (typeCode == 'i'.code) 0 else -1
            localArraySizes[arrId] = size
            localArrays[arrId].fill(defaultValue)
        }
    }

    @Instruction(BaseCoreOpcodes.PUSH_ARRAY_INT)
    public open fun T._push_array_int() {
        val arrId = intOperand
        val index = popInt()

        if (arraysV2) {
            val array = objLocals[arrId] as ScriptArray
            val type = array.type
            if (type == BaseVarType.INTEGER) {
                pushInt(array.ints[index])
            } else if (type == BaseVarType.LONG) {
                pushLong(array.longs[index])
            } else {
                pushObj(array.strings[index])
            }
        } else {
            pushInt(localArrays[arrId][index])
        }
    }

    @Instruction(BaseCoreOpcodes.POP_ARRAY_INT)
    public open fun T._pop_array_int() {
        val arrId = intOperand
        if (arraysV2) {
            val array = objLocals[arrId] as ScriptArray
            check(array.mutable) { "Local array $arrId is immutable." }

            val type = array.type
            if (type == BaseVarType.INTEGER) {
                val value = popInt()
                val index = popInt()
                array.ints[index] = value
            } else if (type == BaseVarType.LONG) {
                val value = popLong()
                val index = popInt()
                array.longs[index] = value
            } else {
                val value = popObj<String>()
                val index = popInt()
                array.strings[index] = value
            }
        } else {
            val value = popInt()
            val index = popInt()
            localArrays[arrId][index] = value
        }
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
