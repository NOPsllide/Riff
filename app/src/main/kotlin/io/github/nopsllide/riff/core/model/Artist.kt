package io.github.nopsllide.riff.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    val id: String = "",
    val name: String,
    val pictureUrl: String = ""
)
