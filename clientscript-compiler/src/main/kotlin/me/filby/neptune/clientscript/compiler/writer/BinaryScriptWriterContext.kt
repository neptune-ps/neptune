package me.filby.neptune.clientscript.compiler.writer

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import me.filby.neptune.clientscript.compiler.ClientScriptOpcode
import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter.Companion.getLocalCount
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter.Companion.getParameterCount

class BinaryScriptWriterContext(
    script: RuneScript,
    private val allocator: ByteBufAllocator,
) : BaseScriptWriter.BaseScriptWriterContext(script) {
    /**
     * The buffer that contains all instruction information.
     */
    private val instructionBuffer = allocator.buffer(INITIAL_CAPACITY)

    /**
     * The buffer that container all switch table information.
     */
    private val switchBuffer = allocator.buffer(INITIAL_CAPACITY)

    /**
     * Tracks the number of instructions within the script.
     */
    private var instructionCount = 0

    fun instruction(opcode: ClientScriptOpcode, operand: Int) {
        instructionCount += 1
        instructionBuffer.writeShort(opcode.id)
        if (opcode.largeOperand) {
            instructionBuffer.writeInt(operand)
        } else {
            instructionBuffer.writeByte(operand)
        }
    }

    fun instruction(opcode: Int, operand: Int) {
        instructionCount += 1
        instructionBuffer.writeShort(opcode)
        instructionBuffer.writeByte(operand)
    }

    fun instruction(opcode: ClientScriptOpcode, operand: String) {
        instructionCount += 1
        instructionBuffer.writeShort(opcode.id)
        instructionBuffer.writeString(operand)
    }

    fun switch(id: Int, block: () -> Int) {
        instruction(ClientScriptOpcode.SWITCH, id)

        val switchStartPos = switchBuffer.writerIndex()
        switchBuffer.writeShort(0)
        val totalKeyCount = block()
        switchBuffer.setShort(switchStartPos, totalKeyCount)
    }

    fun switchCase(key: Int, jump: Int) {
        switchBuffer.writeInt(key)
        switchBuffer.writeInt(jump)
    }

    fun finish(): ByteBuf {
        val buf = allocator.buffer(calculateBufferSize())
        val locals = script.locals
        val switchBufferSize = switchBuffer.writerIndex()

        buf.writeString(script.fullName)
        buf.writeBytes(instructionBuffer)
        buf.writeInt(instructionCount)
        buf.writeShort(locals.getLocalCount(BaseVarType.INTEGER))
        buf.writeShort(locals.getLocalCount(BaseVarType.STRING))
        buf.writeShort(locals.getParameterCount(BaseVarType.INTEGER))
        buf.writeShort(locals.getParameterCount(BaseVarType.STRING))
        buf.writeByte(script.switchTables.size)
        buf.writeBytes(switchBuffer)
        buf.writeShort(switchBufferSize + 1)
        return buf
    }

    private fun calculateBufferSize(): Int {
        var size = 0
        size += script.fullName.length + 1
        size += instructionBuffer.readableBytes()
        size += 4 // instruction count
        size += 2 * 4 // local var counts
        size += 1 // switch table count
        size += switchBuffer.readableBytes()
        size += 2 // switch buffer size
        return size
    }

    override fun close() {
        instructionBuffer.release()
        switchBuffer.release()
    }

    private fun ByteBuf.writeString(text: String) {
        for (char in text) {
            writeByte(char.code)
        }
        writeByte(0)
    }

    private companion object {
        /**
         * This value was determined by checking the average size of all scripts
         * in OSRS and rounding to the next power of two.
         */
        const val INITIAL_CAPACITY = 512
    }
}
