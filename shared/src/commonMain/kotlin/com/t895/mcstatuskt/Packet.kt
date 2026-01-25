package com.t895.mcstatuskt

import io.ktor.utils.io.core.toByteArray
import kotlin.jvm.JvmInline

/**
 * Packet intended to be created by [PacketBuilder] but this is exposed if more control is needed.
 * Note: The underlying ByteArray can ultimately be larger than a packet on your system, but this
 * will be broken up appropriately by the underlying backend.
 */
@JvmInline
value class Packet internal constructor(val data: ByteArray)

/**
 * Builds packets that comply with the [Java Minecraft Server protocol](https://wiki.vg/Protocol).
 */
class PacketBuilder {
    private val bytes = mutableListOf<Byte>()

    fun build(): Packet {
        val newList = bytes.toMutableList()
        newList.addAll(0, bytes.size.toVar().toList())
        return Packet(newList.toByteArray())
    }

    fun clear() = bytes.clear()

    fun add(value: String): PacketBuilder {
        bytes.addAll(value.length.toVar().toList())
        bytes.addAll(value.toByteArray().toList())
        return this
    }

    fun add(value: Byte): PacketBuilder {
        bytes.add(value)
        return this
    }

    fun add(value: Short): PacketBuilder {
        bytes.addAll(value.toByteArray().toList())
        return this
    }

    fun add(value: Int): PacketBuilder {
        bytes.addAll(value.toVar().toList())
        return this
    }

    fun add(value: Long): PacketBuilder {
        bytes.addAll(value.toVar().toList())
        return this
    }

    private fun Short.toByteArray(): ByteArray {
        val newArray = mutableListOf<Byte>()
        val thisInt = this.toInt()

        val topBits = 0xFF00 and thisInt
        newArray.add((topBits shr 8).toByte())
        val bottomBits = 0xFF and thisInt
        newArray.add(bottomBits.toByte())

        return newArray.toByteArray()
    }
}
