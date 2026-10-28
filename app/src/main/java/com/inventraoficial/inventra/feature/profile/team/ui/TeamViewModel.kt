package com.inventraoficial.inventra.feature.profile.team.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.MembersRepository
import com.inventraoficial.inventra.feature.profile.toLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TeamViewModel(
    private val membersRepository: MembersRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetryClick() {
        load()
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val currentUserId = authRepository.currentUser.first()?.id
            membersRepository
                .members()
                .onSuccess { users ->
                    val members =
                        users
                            .filter { it.active }
                            .map { it.toTeamMember(currentUserId) }
                            .sortedWith(compareByDescending<TeamMember> { it.isCurrentUser }.thenBy { it.name })
                    _uiState.update { it.copy(isLoading = false, members = members) }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    TeamViewModel(
                        membersRepository = application.container.membersRepository,
                        authRepository = application.container.authRepository,
                    )
                }
            }
    }
}

private fun UserResponse.toTeamMember(currentUserId: String?): TeamMember {
    val isCurrentUser = id == currentUserId
    return TeamMember(
        id = id,
        initials = name.initials(),
        name = if (isCurrentUser) "$name (você)" else name,
        email = email,
        roleLabel = profile.accessType.toLabel(),
        isCurrentUser = isCurrentUser,
        photoUrl = photoUrl,
    )
}

internal fun String.initials(): String {
    val words = trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when (words.size) {
        0 -> ""
        1 -> words.first().take(1).uppercase()
        else -> (words.first().take(1) + words.last().take(1)).uppercase()
    }
}
