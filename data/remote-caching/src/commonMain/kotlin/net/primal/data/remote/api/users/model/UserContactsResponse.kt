package net.primal.data.remote.api.users.model

import kotlinx.serialization.Serializable
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent

@Serializable
data class UserContactsResponse(
    val followListEvent: NostrEvent? = null,
    val followMetadataList: List<NostrEvent> = emptyList(),
    val userScores: PrimalEvent? = null,
    val cdnResources: List<PrimalEvent> = emptyList(),
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
)
