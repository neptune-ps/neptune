package me.filby.neptune.runescript.runtime.impl

import com.github.michaelbull.logging.InlineLogger
import me.filby.neptune.runescript.runtime.Script
import me.filby.neptune.runescript.runtime.ScriptRunner
import me.filby.neptune.runescript.runtime.state.ScriptFinishHandler
import me.filby.neptune.runescript.runtime.state.ScriptState
import me.filby.neptune.runescript.runtime.state.ScriptState.ExecutionState
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod

/**
 * A base [ScriptRunner] implementation that allows registering functions annotated with
 * [Instruction] when registered using [registerHandlersFrom].
 */
public abstract class BaseScriptRunner<T : ScriptState> : ScriptRunner<T> {
    private val logger = InlineLogger()
    private val handlers = mutableMapOf<Int, MethodHandle>()
    private val handlerNames = mutableMapOf<Int, String>()

    public fun registerHandlersFrom(ref: Any) {
        for (function in ref::class.functions) {
            val instruction = function.findAnnotation<Instruction>() ?: continue

            // TODO verify function signature is valid (must end with state and verify the argument types are valid?)
            if (instruction.opcode in handlers) {
                error("duplicate handler for opcode ${instruction.opcode}: $function")
            }
            handlers[instruction.opcode] = MethodHandlerHelper.buildHandle(ref, function)
            handlerNames[instruction.opcode] = function.name
            println("Registered ${function.name} to ${instruction.opcode}")
        }
    }

    /**
     * Creates a new [ScriptState] instance of [T].
     */
    protected abstract fun createState(): T

    @Suppress("UNCHECKED_CAST")
    override fun execute(script: Script, vararg args: Any, onComplete: ScriptFinishHandler<T>?): T? {
        val state = createState()
        state.setup(script)
        state.onComplete = onComplete as ScriptFinishHandler<ScriptState>?

        for (arg in args) {
            when (arg) {
                is Int -> state.pushInt(arg)
                is Long -> state.pushLong(arg)
                else -> state.pushObj(arg)
            }
        }
        return executeInner(state)
    }

    override fun resume(state: T): T? {
        state.execution = ExecutionState.RUNNING
        return executeInner(state)
    }

    private fun executeInner(state: T): T? {
        while (state.execution == ExecutionState.RUNNING) {
            state.opcount++
            executeOpcode(state.opcodes[++state.pc], state)
        }

        if (state.execution == ExecutionState.SUSPENDED) {
            return state
        } else if (state.execution == ExecutionState.FINISHED || state.execution == ExecutionState.ABORTED) {
            state.onComplete?.invoke(state)
        }
        state.close()
        return null
    }

    private fun executeOpcode(opcode: Int, state: ScriptState) {
        val handler = handlers[opcode] ?: error("Unhandled opcode: $opcode")
        try {
            handler.invoke(state)
        } catch (e: Throwable) {
            state.execution = ExecutionState.ABORTED
            logger.error(e) {
                "Error during script execution: script=${state.script.name}, pc=${state.pc}, opcode=$opcode, " +
                    "operands=(${state.intOperand}, ${state.objOperand})"
            }
        }
    }

    private object MethodHandlerHelper {
        private val POPINT_HANDLE = MethodHandles.lookup().unreflect(ScriptState::popInt.javaMethod)
        private val POPLONG_HANDLE = MethodHandles.lookup().unreflect(ScriptState::popLong.javaMethod)
        private val POPOBJ_HANDLE = MethodHandles.lookup().findVirtual(
            ScriptState::class.java, "popObj", MethodType.methodType(Any::class.java)
        )

        fun buildHandle(instance: Any, func: KFunction<*>): MethodHandle {
            var handle = MethodHandles.lookup().unreflect(func.javaMethod)

            // bind the object that contains the function
            handle = handle.bindTo(instance)

            // reverse the arguments
            val paramCount = handle.type().parameterCount()
            handle = permuteArguments(handle, IntArray(paramCount) { paramCount - it - 1 })

            // loop over all arguments while folding the 2nd to last with the last (state)
            while (handle.type().parameterCount() > 1) {
                val paramIndex = handle.type().parameterCount() - 2
                var combiner = when (handle.type().parameterType(paramIndex)) {
                    Int::class.javaPrimitiveType -> POPINT_HANDLE
                    Long::class.javaPrimitiveType -> POPLONG_HANDLE
                    else -> {
                        val type = handle.type().parameterType(paramIndex)
                        POPOBJ_HANDLE.asType(POPOBJ_HANDLE.type().changeReturnType(type))
                    }
                }
                // set the type of the script state argument to what the function actually uses
                combiner = combiner.asType(combiner.type().changeParameterType(0, handle.type().lastParameterType()))

                // folder the argument with the script state to pop from the proper stack
                handle = MethodHandles.foldArguments(handle, paramIndex, combiner)
            }
            return handle
        }

        private fun permuteArguments(target: MethodHandle, permutation: IntArray): MethodHandle? {
            val parameters = target.type().parameterArray()
            val permutedParameters = arrayOfNulls<Class<*>>(parameters.size)
            for (i in parameters.indices) {
                permutedParameters[permutation[i]] = parameters[i]
            }
            return MethodHandles.permuteArguments(
                target,
                MethodType.methodType(target.type().returnType(), permutedParameters),
                *permutation
            )
        }
    }
}
