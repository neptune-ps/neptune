package me.filby.neptune.runescript.runtime

import me.filby.neptune.runescript.runtime.state.ScriptFinishHandler
import me.filby.neptune.runescript.runtime.state.ScriptState
import me.filby.neptune.runescript.runtime.state.ScriptState.ExecutionState

/**
 * A script runner is allowed to execute and resume a script. Both
 * of which should iterate over the [ScriptState] until suspension
 * or a `return` instruction (with no active gosubs) is hit.
 */
public interface ScriptRunner<T : ScriptState> {
    /**
     * Executes [script] with the given [args] (if any).
     *
     * The [onFinish] callback is executed when the script finishes with [ExecutionState.FINISHED]. This
     * callback can be used to extract the return values from the script that is executed. If the script
     * is suspended, it will still call the callback when resuming assuming it is marked finished.
     *
     * Returns the [ScriptState] if the script was suspended, which can be
     * resumed later using [resume]. If the script finishes execution, `null`
     * is returned.
     */
    public fun execute(script: Script, vararg args: Any, onFinish: ScriptFinishHandler<T>? = null): T?

    /**
     * Resumes the execution of a [ScriptState].
     *
     * Returns [state] if the script was suspended again. If the script
     * finishes execution, `null` is returned.
     */
    public fun resume(state: T): T?
}
