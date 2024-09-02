package com.t895.mcstatuskt

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaConnectTest {
    /**
     * Connects to a 1.21.1 Java Minecraft Server running on this machine and validates that all of
     * the default status values are received.
     */
    @Test
    fun testConnect() {
        lateinit var status: Status
        runBlocking {
            JavaServer("127.0.0.1", 25565).use {
                it.connect()
                status = it.status()
            }
        }

        assertEquals(status.version.name, "1.21.1", "Incorrect version!")
        assertEquals(status.version.protocol, 767, "Incorrect protocol!")

        assertEquals(status.description, "A Minecraft Server", "Incorrect description!")

        assertEquals(status.players!!.max, 20, "Incorrect max player count!")
        assertEquals(status.players!!.online, 0, "Incorrect player count!")
        assertEquals(status.players!!.sample, null, "Incorrect player sample!")

        assertEquals(status.favicon, null, "Incorrect favicon!")
        assertEquals(status.enforcesSecureChat, true, "Incorrect secure chat enforcement!")
    }
}
