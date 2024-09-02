package com.t895.mcstatuskt

import io.ktor.utils.io.core.toByteArray

/**
 * Packet intended to be created by [PacketBuilder] but this is exposed if more control is needed.
 * Note: The underlying ByteArray can ultimately be larger than a packet on your system, but this
 * will be broken up appropriately by the underlying ktor library.
 */
data class Packet(val data: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        other as Packet

        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}

/**
 * Builds packets that comply with the Java Minecraft Server protocol.
 * https://wiki.vg/Protocol
 */
class PacketBuilder {
    private val byteList = mutableListOf<Byte>()

    fun build(): Packet {
        val newList = byteList
        newList.addAll(0, byteList.size.toVar().toList())
        return Packet(newList.toByteArray())
    }

    fun clear() = byteList.clear()

    fun add(value: String): PacketBuilder {
        byteList.addAll(value.length.toVar().toList())
        byteList.addAll(value.toByteArray().toList())
        return this
    }

    fun add(value: Byte): PacketBuilder {
        byteList.add(value)
        return this
    }

    fun add(value: Short): PacketBuilder {
        byteList.addAll(value.toByteArray().toList())
        return this
    }

    fun add(value: Int): PacketBuilder {
        byteList.addAll(value.toVar().toList())
        return this
    }

    fun add(value: Long): PacketBuilder {
        byteList.addAll(value.toVar().toList())
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
