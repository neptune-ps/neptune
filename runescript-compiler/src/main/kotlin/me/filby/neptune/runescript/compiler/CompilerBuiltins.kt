package me.filby.neptune.runescript.compiler

import me.filby.neptune.runescript.compiler.type.Type

/**
 * The foundational types required by the compiler.
 *
 * Embeddings provide these types so the compiler core does not depend on a
 * particular game's type names or type implementation.
 */
public interface CompilerBuiltins {
    public val int: Type
    public val boolean: Type
    public val string: Type
    public val char: Type
    public val long: Type
    public val coordgrid: Type
}
