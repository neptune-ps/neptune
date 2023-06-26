package me.filby.neptune.clientscript.compiler.writer

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.WRITE
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists
import kotlin.io.path.outputStream

/**
 * An implementation of [BinaryScriptWriter] that writes the scripts to [output].
 */
class BinaryFileScriptWriter(
    private val output: Path,
    idProvider: IdProvider,
    allocator: ByteBufAllocator = ByteBufAllocator.DEFAULT,
) : BinaryScriptWriter(idProvider, allocator) {
    init {
        if (output.notExists()) {
            output.createDirectories()
        }
        require(output.isDirectory()) { "${output.absolute()} is not a directory." }
    }

    override fun outputScript(script: RuneScript, data: ByteBuf) {
        val id = idProvider.get(script.symbol)
        val scriptOutput = output.resolve("$id")
        scriptOutput.outputStream(WRITE, TRUNCATE_EXISTING, CREATE).use {
            data.readBytes(it, data.readableBytes())
        }
    }
}
