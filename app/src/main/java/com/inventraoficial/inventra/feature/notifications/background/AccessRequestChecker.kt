package com.inventraoficial.inventra.feature.notifications.background

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.inventraoficial.inventra.data.local.AppPreferences
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.KitchenAccessRequestStatus
import com.inventraoficial.inventra.data.repository.AuthRepository
import com.inventraoficial.inventra.data.repository.KitchenAccessRepository
import kotlinx.coroutines.flow.first

// Guarda, por usuário, o último pedido de entrada visto ("id:STATUS"), para avisar só quando ele mudar.
interface KnownAccessRequestStore {
    suspend fun known(userId: String): String?

    suspend fun save(
        userId: String,
        request: String,
    )
}

class DataStoreKnownAccessRequestStore(
    private val context: Context,
) : KnownAccessRequestStore {
    override suspend fun known(userId: String): String? =
        context.alertNotificationsDataStore.data
            .first()[key(userId)]

    override suspend fun save(
        userId: String,
        request: String,
    ) {
        context.alertNotificationsDataStore.edit { preferences -> preferences[key(userId)] = request }
    }

    private fun key(userId: String) = stringPreferencesKey("access_request_$userId")
}

// O backend só manda alertas para quem já está numa cozinha. Para o estoquista que ainda espera a
// resposta do pedido de entrada, este verificador confere o pedido e avisa quando o supervisor responde.
class AccessRequestChecker(
    private val authRepository: AuthRepository,
    private val kitchenAccessRepository: KitchenAccessRepository,
    private val appPreferences: AppPreferences,
    private val store: KnownAccessRequestStore,
    private val notifier: AlertNotifier,
) {
    suspend fun check(): Result<Unit> {
        val user = authRepository.currentUser.first()
        val waiting = user?.accessType == AccessType.ESTOQUISTA && user.kitchenId == null
        val enabled = appPreferences.alertNotificationsEnabled.first()
        if (user == null || !waiting || !enabled) return Result.success(Unit)

        return kitchenAccessRepository.myRequest().mapCatching { request ->
            if (request == null) return@mapCatching
            val current = "${request.id}:${request.status}"
            val known = store.known(user.id)
            if (current == known) return@mapCatching

            store.save(user.id, current)
            // Só avisa a mudança de um pedido que já estava pendente (não na primeira vez que vê o pedido).
            val wasPending = known == "${request.id}:${KitchenAccessRequestStatus.PENDING}"
            if (!wasPending || request.status == KitchenAccessRequestStatus.PENDING) return@mapCatching

            val approved = request.status == KitchenAccessRequestStatus.APPROVED
            notifier.showAccessDecision(request.kitchen.name, approved)
            // Aprovado: a sessão passa a ter a cozinha, e o app já abre na Home.
            if (approved) authRepository.refreshCurrentUser()
        }
    }
}
