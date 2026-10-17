package com.inventraoficial.inventra.feature.notifications.background

import com.inventraoficial.inventra.data.local.AppPreferences
import com.inventraoficial.inventra.data.remote.dto.alert.AlertResponse
import com.inventraoficial.inventra.data.repository.AlertRepository
import com.inventraoficial.inventra.data.repository.AuthRepository
import kotlinx.coroutines.flow.first

interface AlertNotifier {
    fun show(alerts: List<AlertResponse>)
}

// Procura alertas não lidos que ainda não viraram notificação no celular. Roda pelo AlertCheckWorker.
class NewAlertsChecker(
    private val authRepository: AuthRepository,
    private val alertRepository: AlertRepository,
    private val appPreferences: AppPreferences,
    private val store: LastNotifiedAlertStore,
    private val notifier: AlertNotifier,
) {
    // Devolve quantos alertas foram avisados.
    suspend fun check(): Result<Int> {
        val user = authRepository.currentUser.first()
        val kitchenId = user?.kitchenId
        if (kitchenId == null || !appPreferences.alertNotificationsEnabled.first()) return Result.success(0)

        return alertRepository.unreadAlerts(kitchenId).map { alerts ->
            val lastNotified = store.lastNotifiedId(user.id)
            // Na primeira vez só marca até onde já existe: o que estava na lista antes não vira notificação.
            val fresh =
                if (lastNotified == null) emptyList() else alerts.filter { it.id > lastNotified }.sortedBy { it.id }
            if (fresh.isNotEmpty()) notifier.show(fresh)

            val newest = maxOf(lastNotified ?: 0, alerts.maxOfOrNull { it.id } ?: 0)
            if (newest != lastNotified) store.save(user.id, newest)
            fresh.size
        }
    }
}
