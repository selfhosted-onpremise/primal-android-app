package net.primal.data.remote.api.importing.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
internal data class ImportRequestBody(
    @SerialName("events") val nostrEvents: JsonArray,
)
