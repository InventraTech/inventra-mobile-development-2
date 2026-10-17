package com.inventraoficial.inventra.feature.notifications.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.inventraoficial.inventra.InventraApplication
import java.util.concurrent.TimeUnit

// Consulta os alertas da cozinha em segundo plano, mesmo com o app fechado. O Android roda no mínimo a
// cada 15 minutos e pode adiar em modo de economia de bateria.
class AlertCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as InventraApplication).container
        // Sem conexão ou com erro no servidor, tenta de novo na próxima rodada.
        container.newAlertsChecker.check()
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "alert-check"
        private const val INTERVAL_MINUTES = 15L

        fun schedule(context: Context) {
            val request =
                PeriodicWorkRequestBuilder<AlertCheckWorker>(INTERVAL_MINUTES, TimeUnit.MINUTES)
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
