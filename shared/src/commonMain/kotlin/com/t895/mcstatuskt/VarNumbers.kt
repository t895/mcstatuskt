package com.t895.mcstatuskt

import io.ktor.utils.io.*

private const val SEGMENT_BITS = 0x7F
private const val CONTINUE_BIT = 0x80

/**
 * Reads the contents of a Java Minecraft Server's VarInt
 * https://wiki.vg/Protocol#VarInt_and_VarLong
 */
suspend fun ByteReadChannel.readVarInt(): Int {
    var value = 0
    var position = 0
    while (true) {
        val currentByte = readByte()
        value = value or ((currentByte.toInt() and SEGMENT_BITS) shl position)

        if ((currentByte.toInt() and CONTINUE_BIT) == 0) break

        position += 7

        if (position >= 32) {
            throw RuntimeException("VarLong is too big")
        }
    }
    return value
}

/**
 * Reads the contents of a Java Minecraft Server's VarLong
 * https://wiki.vg/Protocol#VarInt_and_VarLong
 */
suspend fun ByteReadChannel.readVarLong(byteArray: ByteArray): Long {
    var value = 0L
    var position = 0
    while (true) {
        val currentByte = readByte()
        value = value or ((currentByte.toInt() and SEGMENT_BITS).toLong() shl position)

        if ((currentByte.toInt() and CONTINUE_BIT) == 0) break

        position += 7

        if (position >= 64) {
            throw RuntimeException("VarLong is too big")
        }
    }
    return value
}

/**
 * Converts an Int to a Java Minecraft Server's VarInt
 * https://wiki.vg/Protocol#VarInt_and_VarLong
 */
fun Int.toVar(): ByteArray {
    var tempValue = this
    val bytes = mutableListOf<Byte>()
    while (true) {
        if ((tempValue and SEGMENT_BITS.inv()) == 0) {
            bytes.add(tempValue.toByte())
            return bytes.toByteArray()
        }

        bytes.add(0, ((tempValue and SEGMENT_BITS) or CONTINUE_BIT).toByte())

        tempValue = tempValue ushr 7
    }
}

/**
 * Converts a Long to a Java Minecraft Server's VarInt
 * https://wiki.vg/Protocol#VarInt_and_VarLong
 */
fun Long.toVar(): ByteArray {
    var tempValue = this
    val bytes = mutableListOf<Byte>()
    while (true) {
        if ((tempValue and SEGMENT_BITS.toLong().inv()) == 0L) {
            bytes.add(tempValue.toByte())
            return bytes.toByteArray()
        }

        bytes.add(0, ((tempValue and SEGMENT_BITS.toLong()) or CONTINUE_BIT.toLong()).toByte())

        tempValue = tempValue ushr 7
    }
}
