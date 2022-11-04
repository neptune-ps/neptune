package me.filby.neptune.runescript.runtime

import me.filby.neptune.runescript.runtime.impl.BaseScriptRunner
import me.filby.neptune.runescript.runtime.impl.ScriptProvider
import me.filby.neptune.runescript.runtime.impl.opcodes.BaseCoreOpcodes
import me.filby.neptune.runescript.runtime.impl.opcodes.CoreOpcodesBase
import me.filby.neptune.runescript.runtime.impl.opcodes.MathOpcodesBase
import me.filby.neptune.runescript.runtime.state.ScriptState
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseScriptRunnerTest {
    @Test
    fun `script should return nothing`() {
        val script = ScriptBuilder {
            instruction(BaseCoreOpcodes.RETURN)
        }

        runner.execute(script) {
            assertEquals(0, intPointer)
            assertEquals(0, objPointer)
            assertEquals(0, longPointer)
        }
    }

    @Test
    fun `script should return an integer`() {
        val script = ScriptBuilder {
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 1234)
            instruction(BaseCoreOpcodes.RETURN)
        }

        runner.execute(script) {
            assertEquals(1234, popInt())
            assertEquals(0, objPointer)
            assertEquals(0, longPointer)
        }
    }

    @Test
    fun `script should return an integer and long`() {
        val script = ScriptBuilder {
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 1234)
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_LONG, 1L)
            instruction(BaseCoreOpcodes.RETURN)
        }

        runner.execute(script) {
            assertEquals(1234, popInt())
            assertEquals(0, objPointer)
            assertEquals(1L, popLong())
        }
    }

    @Test
    fun `script should return the value of 1 + 1`() {
        val script = ScriptBuilder {
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 1)
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 1)
            instruction(BaseCoreOpcodes.ADD)
            instruction(BaseCoreOpcodes.RETURN)
        }

        runner.execute(script) {
            assertEquals(2, popInt())
            assertEquals(0, objPointer)
            assertEquals(0, longPointer)
        }
    }

    private companion object {
        val scriptProvider = object : ScriptProvider {
            override fun get(id: Int): Script {
                error("unsupported")
            }
        }

        val runner = kotlin.run {
            val runner = object : BaseScriptRunner<ScriptState>() {
                override fun createState(): ScriptState {
                    return ScriptState()
                }
            }
            runner.registerHandlersFrom(CoreOpcodesBase<ScriptState>(scriptProvider))
            runner.registerHandlersFrom(MathOpcodesBase<ScriptState>())
            runner
        }
    }
}
