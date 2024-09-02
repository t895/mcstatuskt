package com.t895.mcstatuskt

import kotlinx.serialization.Serializable

@Serializable
data class Version(
    val name: String,
    val protocol: Int,
)

@Serializable
data class Player(
    val id: String,
    val name: String,
)

@Serializable
data class Players(
    val max: Int,
    val online: Int,
    val sample: List<Player>? = null,
)

@Serializable
data class Status(
    val version: Version = Version("Unknown", 0),
    val description: String? = null,
    val players: Players? = null,
    val favicon: String? = null,
    val enforcesSecureChat: Boolean? = false,
)
