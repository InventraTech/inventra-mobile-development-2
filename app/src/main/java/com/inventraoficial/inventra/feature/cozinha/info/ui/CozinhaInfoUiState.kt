package com.inventraoficial.inventra.feature.cozinha.info.ui

import com.inventraoficial.inventra.data.local.GeoPoint

data class CozinhaInfoUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // Só o supervisor edita; os outros perfis só consultam.
    val canEdit: Boolean = false,
    val code: String = "",
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val acceptingRequests: Boolean = true,
    val hasLocation: Boolean = false,
    // Marcada com "Usar minha localização atual"; vai junto no próximo salvar.
    val newLocation: GeoPoint? = null,
    val isLocating: Boolean = false,
    val locationMessage: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val savedMessage: String? = null,
) {
    private val isLoaded: Boolean
        get() = !isLoading && errorMessage == null

    val showEditButton: Boolean
        get() = canEdit && !isEditing && isLoaded

    val locationStatus: String
        get() =
            when {
                newLocation != null -> "Ao salvar, o pino vai para onde você está agora."
                hasLocation -> "A cozinha aparece no mapa e na busca por perto."
                else -> "Ainda sem localização no mapa."
            }
}

@Suppress("LongParameterList")
data class CozinhaInfoActions(
    val onNameChange: (String) -> Unit,
    val onAddressChange: (String) -> Unit,
    val onCityChange: (String) -> Unit,
    val onStateChange: (String) -> Unit,
    val onAcceptingRequestsChange: (Boolean) -> Unit,
    val onUseMyLocationClick: () -> Unit,
    val onEditClick: () -> Unit,
    val onSaveClick: () -> Unit,
    val onCancelClick: () -> Unit,
    val onRetryClick: () -> Unit,
    val onBackClick: () -> Unit,
)
