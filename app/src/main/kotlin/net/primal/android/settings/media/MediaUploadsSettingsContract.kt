package net.primal.android.settings.media

import net.primal.android.core.errors.UiError

interface MediaUploadsSettingsContract {
    data class UiState(
        val isLoadingBlossomServerUrls: Boolean = true,
        val blossomServerUrl: String = "",
        val mirrorBlossomServerUrls: List<String> = emptyList(),
        val newBlossomServerUrl: String = "",
        val newBlossomServerMirrorUrl: String = "",
        val suggestedBlossomServers: List<String> = emptyList(),
        val blossomMirrorEnabled: Boolean = false,
        val mode: MediaUploadsMode = MediaUploadsMode.View,
        val error: UiError? = null,
    )

    sealed class UiEvent {
        data class UpdateNewBlossomServerUrl(val url: String) : UiEvent()
        data class ConfirmBlossomServerUrl(val url: String) : UiEvent()
        data class UpdateNewBlossomMirrorServerUrl(val url: String) : UiEvent()
        data class ConfirmBlossomMirrorServerUrl(val url: String) : UiEvent()
        data class UpdateMediaUploadsMode(val mode: MediaUploadsMode) : UiEvent()
        data class UpdateBlossomMirrorEnabled(val enabled: Boolean) : UiEvent()
        data class RemoveBlossomMirrorServerUrl(val url: String) : UiEvent()
        data object RestoreDefaultBlossomServer : UiEvent()
        class DismissError : UiEvent()
    }
}

enum class MediaUploadsMode {
    View,
    EditBlossomServer,
    EditBlossomMirrorServer,
}
