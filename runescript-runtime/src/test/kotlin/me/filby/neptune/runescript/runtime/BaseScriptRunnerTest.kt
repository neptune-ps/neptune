package me.filby.neptune.runescript.runtime

import me.filby.neptune.runescript.compiler.type.PrimitiveType
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

    @Test
    fun `script should return 2 int values from intarray`() {
        val script = ScriptBuilder {
            val arrayId = createObjLocal()

            // def_int $array(2);
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 2)
            instruction(BaseCoreOpcodes.DEFINE_ARRAY, (arrayId shl 16) or PrimitiveType.INT.code!!.code)

            // $array(0) = 1337;
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 0)
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 1337)
            instruction(BaseCoreOpcodes.POP_ARRAY_INT, arrayId)

            // $array(1) = 42;
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 1)
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 42)
            instruction(BaseCoreOpcodes.POP_ARRAY_INT, arrayId)

            // return($array(0), $array(1));
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 0)
            instruction(BaseCoreOpcodes.PUSH_ARRAY_INT, arrayId)
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 1)
            instruction(BaseCoreOpcodes.PUSH_ARRAY_INT, arrayId)
            instruction(BaseCoreOpcodes.RETURN)
        }

        runner(arraysV2 = false).execute(script) {
            assertEquals(42, popInt()) // 2nd return value
            assertEquals(1337, popInt()) // 1st return value
            assertEquals(0, objPointer)
            assertEquals(0, longPointer)
        }

        runner(arraysV2 = true).execute(script) {
            assertEquals(42, popInt()) // 2nd return value
            assertEquals(1337, popInt()) // 1st return value
            assertEquals(0, objPointer)
            assertEquals(0, longPointer)
        }
    }

    @Test
    fun `script should return 2 string values from stringarray`() {
        val script = ScriptBuilder {
            val arrayId = createObjLocal()

            // def_string $array(2);
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 2)
            instruction(BaseCoreOpcodes.DEFINE_ARRAY, (arrayId shl 16) or PrimitiveType.STRING.code!!.code)

            // $array(0) = "Hello world!";
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 0)
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_STRING, "Hello world!")
            instruction(BaseCoreOpcodes.POP_ARRAY_INT, arrayId)

            // $array(1) = "Hello world 2!";
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 1)
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_STRING, "Hello world 2!")
            instruction(BaseCoreOpcodes.POP_ARRAY_INT, arrayId)

            // return($array(0), $array(1));
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 0)
            instruction(BaseCoreOpcodes.PUSH_ARRAY_INT, arrayId)
            instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, 1)
            instruction(BaseCoreOpcodes.PUSH_ARRAY_INT, arrayId)
            instruction(BaseCoreOpcodes.RETURN)
        }

        // pre-arrays v2 does not support stringarray

        runner(arraysV2 = true).execute(script) {
            assertEquals(0, intPointer)
            assertEquals("Hello world 2!", popObj()) // 2nd return value
            assertEquals("Hello world!", popObj()) // 1st return value
            assertEquals(0, longPointer)
        }
    }

    private companion object {
        val scriptProvider = object : ScriptProvider {
            override fun get(id: Int): Script {
                error("unsupported")
            }
        }

        val runner = runner()

        fun runner(arraysV2: Boolean = false): ScriptRunner<ScriptState> {
            val runner = object : BaseScriptRunner<ScriptState>() {
                override fun createState(): ScriptState = ScriptState()
            }
            runner.registerHandlersFrom(CoreOpcodesBase<ScriptState>(scriptProvider, arraysV2))
            runner.registerHandlersFrom(MathOpcodesBase<ScriptState>())
            return runner
        }
    }
}
