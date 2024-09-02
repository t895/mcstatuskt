package com.t895.mcstatuskt

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

/**
 * Class to allow for connections to a Java Minecraft Server.
 *
 * @param address String representation of an IPv4 server address (DNS resolving is not currently supported)
 * @param port Port used to connect to the server
 * @param timeoutMs The time waited before terminating any connection to the server (e.g. Sending packets)
 */
class JavaServer(
    private val address: String,
    private val port: Short = 25565,
    private val timeoutMs: Long = 15000,
) : AutoCloseable {
    private var selectorManager = SelectorManager(Dispatchers.IO)
    private lateinit var socket: Socket

    private lateinit var readChannel: ByteReadChannel
    private lateinit var writeChannel: ByteWriteChannel

    private var connected by atomic(false)

    /**
     * Begins initial connection to [address]:[port].
     * Must be run before any other methods in this class.
     */
    @Throws(CancellationException::class)
    suspend fun connect() {
        if (connected) {
            return
        }

        withTimeout(timeoutMs) {
            socket = aSocket(selectorManager).tcp().connect(address, port.toInt()) {
                socketTimeout = timeoutMs

                // This makes sure the socket closes immediately
                lingerSeconds = 0
            }
        }
        readChannel = socket.openReadChannel()
        writeChannel = socket.openWriteChannel(autoFlush = false)
        writeChannel.flush()
        connected = true
    }

    /**
     * Sends a [Packet] to this server. Typically constructed by [PacketBuilder].
     */
    suspend fun sendPacket(packet: Packet) {
        if (!connected) {
            error("Server is not connected!")
        }

        writeChannel.writeFully(packet.data)
        writeChannel.flush()
    }

    /**
     * Gives access to the underlying [ByteReadChannel] that allows you to read responses after
     * sending each packet.
     */
    fun getReadChannel(): ByteReadChannel {
        if (!connected) {
            error("Server is not connected!")
        }

        return readChannel
    }

    private suspend fun ByteReadChannel.readMCString(): String {
        val dataLength = readChannel.readVarInt()
        val stringData = StringBuilder()
        for (i in 0 until dataLength) {
            stringData.append(readChannel.readByte().toInt().toChar())
        }
        return stringData.toString()
    }

    /**
     * Conducts a handshake with this server and receives the current server status if successful
     * and a default Status object if not.
     */
    @Throws(CancellationException::class)
    suspend fun status(): Status {
        if (!connected) {
            error("Server is not connected!")
        }

        val handshakePacket = PacketBuilder()
            .add(0.toByte())
            .add(PROTOCOL_VERSION)
            .add(address)
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

        readChannel.readVarInt() // Packet size

        val packetId = readChannel.readByte()
        check(packetId.toInt() == 0) { "Status packet ID does not equal 0!" }

        val statusString = readChannel.readMCString()

        val status = try {
            Json.decodeFromString(statusString)
        } catch (e: Exception) {
            println("Failed to deserialize status - ${e.message}")
            Status()
        }

        return status
    }

    override fun close() {
        if (!connected) {
            return
        }

        runBlocking { writeChannel.flushAndClose() }
        socket.close()
        selectorManager.close()
        connected = false
    }

    companion object {
        private const val PROTOCOL_VERSION = 767 // MC 1.21
    }
}
