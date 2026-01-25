package com.t895.mcstatuskt

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JavaConnectTest {
    /**
     * Connects to a 1.21.X Java Minecraft Server running on this machine and validates that all the default
     * status values are received.
     */
    @Test
    fun testConnect() {
        lateinit var status: Status
        runBlocking {
            JavaServer.connect("127.0.0.1", 25565).use {
                status = it.status()
            }
        }

        assertTrue("Incorrect version!") { status.version.name.contains("1.21") }
        assertTrue("Incorrect protocol!") { (767..774).contains(status.version.protocol) }

        assertEquals(status.description, "A Minecraft Server", "Incorrect description!")

        assertEquals(status.players?.max, 20, "Incorrect max player count!")
        assertEquals(status.players?.online, 0, "Incorrect player count!")
        assertEquals(status.players?.sample, null, "Incorrect player sample!")

        assertEquals(status.favicon, null, "Incorrect favicon!")
        assertEquals(status.enforcesSecureChat, true, "Incorrect secure chat enforcement!")
    }
}
