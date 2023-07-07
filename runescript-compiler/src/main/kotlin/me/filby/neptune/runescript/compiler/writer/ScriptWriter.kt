package me.filby.neptune.runescript.compiler.writer

import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import java.io.Closeable

public interface ScriptWriter : Closeable {
    public fun write(script: RuneScript)
}
