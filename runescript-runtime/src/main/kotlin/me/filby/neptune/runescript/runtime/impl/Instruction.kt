package me.filby.neptune.runescript.runtime.impl

/**
 * Marks a function specifying it is a handler for the given [opcode]. To be used
 * with [BaseScriptRunner.registerHandlersFrom] to register functions with an
 * instruction opcode number.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class Instruction(public val opcode: Int)
