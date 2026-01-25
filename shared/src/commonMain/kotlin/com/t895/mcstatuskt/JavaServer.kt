package com.t895.mcstatuskt

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class JavaServer private constructor(
    private val hostname: String,
    private val port: Short,
    private val timeoutMs: Long,
) : AutoCloseable {
    companion object {
        private const val PROTOCOL_VERSION = 767 // MC 1.21

        /**
         * Creates a new [JavaServer] that is connected to the target [hostname] and [port].
         *
         * @param hostname String representation of an IPv4 server address (DNS resolving is not currently supported)
         * @param port Port used to connect to the server
         * @param timeoutMs The time waited before terminating any connection to the server (e.g. Sending packets)
         */
        @Throws(CancellationException::class)
        suspend fun connect(
            hostname: String,
            port: Short = 25565,
            timeoutMs: Long = 15_000,
        ): JavaServer = JavaServer(hostname, port, timeoutMs).apply {
            withTimeout(timeoutMs) {
                socket = aSocket(selectorManager).tcp().connect(hostname, port.toInt()) {
                    socketTimeout = timeoutMs

                    // This makes sure the socket closes immediately
                    lingerSeconds = 0
                }
            }
            readChannel = socket.openReadChannel()
            writeChannel = socket.openWriteChannel(autoFlush = false)
            writeChannel.flush()
        }
    }

    private var selectorManager = SelectorManager(Dispatchers.IO)
    private lateinit var socket: Socket

    private lateinit var readChannel: ByteReadChannel
    private lateinit var writeChannel: ByteWriteChannel

    /**
     * Sends a [Packet] to this server. Typically constructed by [PacketBuilder].
     */
    suspend fun sendPacket(packet: Packet) {
        writeChannel.writeFully(packet.data)
        writeChannel.flush()
    }

    /**
     * Reads a byte of data from the internal response channel
     */
    suspend fun readByte(): Byte = readChannel.readByte()

    /**
     * Reads two bytes of data from the internal response channel
     */
    suspend fun readShort(): Short = readChannel.readShort()

    /**
     * Reads four bytes of data from the internal response channel
     */
    suspend fun readInt(): Int = readChannel.readInt()

    /**
     * Reads eight bytes of data from the internal response channel
     */
    suspend fun readLong(): Long = readChannel.readLong()

    /**
     * Reads [count] number of bytes into a [ByteArray] from the internal read channel.
     */
    suspend fun readBytes(count: Int): ByteArray = readChannel.readByteArray(count)

    /**
     * Assuming a [String] is intended to be read at this point in the response, this will read a VarInt representing
     * the length of the string and then read that number of bytes into an output [String].
     */
    suspend fun readString(): String {
        val dataLength = readVarInt()
        val stringData = StringBuilder()
        repeat(dataLength) {
            stringData.append(readByte().toInt().toChar())
        }
        return stringData.toString()
    }

    /**
     * Conducts a handshake with this server and receives the current server status if successful
     * and a default [Status] object if not.
     */
    @Throws(CancellationException::class)
    suspend fun status(): Status {
        val handshakePacket = PacketBuilder()
            .add(0.toByte())
            .add(PROTOCOL_VERSION)
            .add(hostname)
            .add(port)
            .add(1.toByte())
            .build()
        withTimeout(timeoutMs) {
            sendPacket(handshakePacket)
        }

        val requestPacket = PacketBuilder()
            .add(0.toByte())
            .build()
        withTimeout(timeoutMs) {
            sendPacket(requestPacket)
        }

        readVarInt() // Packet size

        val packetId = readChannel.readByte()
        check(packetId.toInt() == 0) { "Status packet ID does not equal 0!" }

        val statusString = readString()

        val status = try {
            Json.decodeFromString(statusString)
        } catch (e: Exception) {
            println("Failed to deserialize status - ${e.message}")
            Status()
        }

        return status
    }

    override fun close() {
        runBlocking { writeChannel.flushAndClose() }
        socket.close()
        selectorManager.close()
    }
}
