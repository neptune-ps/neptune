package me.filby.neptune.runescript.compiler.writer

import me.filby.neptune.runescript.compiler.codegen.script.RuneScript

public interface ScriptWriter {
    public fun write(script: RuneScript)
}
