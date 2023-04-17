package me.filby.neptune.runescript.compiler.incremental

import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import java.io.InputStream
import java.io.OutputStream

internal data class IncrementalData(val files: List<IncrementalFile>) {
    fun pack(output: OutputStream) {
        output.writeInt(VERSION)

        output.writeInt(files.size)
        for (file in files) {
            output.writeString(file.name)
            output.writeLong(file.meta.size)
            output.writeLong(file.meta.lastModified)

            output.writeShort(file.scripts.size)
            for (script in file.scripts) {
                output.writeString(script)
            }

            output.writeShort(file.dependents.size)
            for (dependent in file.dependents) {
                output.writeString(dependent)
            }
        }
    }

    companion object {
        private const val VERSION = 1

        fun unpack(input: InputStream): IncrementalData? {
            val version = input.readInt()
            if (version != VERSION) {
                return null
            }

            val files = mutableListOf<IncrementalFile>()
            val size = input.readInt()
            for (i in 0 until size) {
                val name = input.readString()
                val meta = IncrementalFile.MetaData(input.readLong(), input.readLong())

                val scriptsSize = input.readShort()
                val scripts = ArrayList<String>()
                for (j in 0 until scriptsSize) {
                    scripts += input.readString()
                }

                val dependentsSize = input.readShort()
                val dependents = HashSet<String>(dependentsSize)
                for (j in 0 until dependentsSize) {
                    dependents += input.readString()
                }

                files += IncrementalFile(name, meta, scripts, dependents)
            }

            return IncrementalData(files)
        }

        private fun OutputStream.writeShort(value: Int) {
            write(Shorts.toByteArray(value.toShort()))
        }

        private fun OutputStream.writeInt(value: Int) {
            write(Ints.toByteArray(value))
        }

        private fun OutputStream.writeLong(value: Long) {
            write(Longs.toByteArray(value))
        }

        private fun OutputStream.writeString(text: String) {
            val data = text.toByteArray()
            writeShort(data.size)
            write(data)
        }

        private fun InputStream.readShort(): Int {
            return Shorts.fromByteArray(readNBytes(2)).toInt()
        }

        private fun InputStream.readInt(): Int {
            return Ints.fromByteArray(readNBytes(4))
        }

        private fun InputStream.readLong(): Long {
            return Longs.fromByteArray(readNBytes(8))
        }

        private fun InputStream.readString(): String {
            return String(readNBytes(readShort()))
        }
    }
}
