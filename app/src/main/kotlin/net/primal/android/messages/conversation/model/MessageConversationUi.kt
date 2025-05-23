package net.primal.android.messages.conversation.model

import java.time.Instant
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.notes.feed.model.NoteNostrUriUi
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.links.CdnImage

data class MessageConversationUi(
    val participantId: String,
    val participantUsername: String,
    val lastMessageId: String?,
    val lastMessageSnippet: String?,
    val lastMessageAttachments: List<EventUriUi>,
    val lastMessageNostrUris: List<NoteNostrUriUi>,
    val lastMessageAt: Instant?,
    val isLastMessageFromUser: Boolean,
    val participantInternetIdentifier: String? = null,
    val participantAvatarCdnImage: CdnImage? = null,
    val participantLegendaryCustomization: LegendaryCustomization? = null,
    val unreadMessagesCount: Int = 0,
)
