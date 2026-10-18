package com.inventraoficial.inventra.feature.notifications.background

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.inventraoficial.inventra.MainActivity
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.data.remote.dto.alert.AlertResponse
import com.inventraoficial.inventra.data.remote.dto.alert.AlertSeverity
import com.inventraoficial.inventra.feature.notifications.REQUISITION_PREFIX
import com.inventraoficial.inventra.feature.notifications.canPostNotifications
import com.inventraoficial.inventra.feature.notifications.stockTypes
import com.inventraoficial.inventra.feature.notifications.subtitle
import com.inventraoficial.inventra.feature.notifications.title

// Mostra os alertas como notificações do Android. Tocar em qualquer uma abre a tela de Notificações.
class SystemAlertNotifier(
    private val context: Context,
) : AlertNotifier {
    private enum class Channel(
        val id: String,
        val title: String,
        val importance: Int,
    ) {
        Stock("stock_alerts", "Estoque e validade", NotificationManager.IMPORTANCE_HIGH),
        Requisitions("requisitions", "Requisições", NotificationManager.IMPORTANCE_DEFAULT),
        Kitchen("kitchen", "Cozinha", NotificationManager.IMPORTANCE_DEFAULT),
    }

    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannels(
            Channel.entries.map { NotificationChannel(it.id, it.title, it.importance) },
        )
    }

    @SuppressLint("MissingPermission") // conferida em canPostNotifications
    override fun show(alerts: List<AlertResponse>) {
        if (!context.canPostNotifications()) return

        val manager = NotificationManagerCompat.from(context)
        if (alerts.size > MAX_SEPARATE) {
            manager.notify(SUMMARY_ID, summary(alerts))
        } else {
            alerts.forEach { manager.notify(it.id, single(it)) }
        }
    }

    @SuppressLint("MissingPermission") // conferida em canPostNotifications
    override fun showAccessDecision(
        cozinhaName: String,
        approved: Boolean,
    ) {
        if (!context.canPostNotifications()) return
        val title = if (approved) "Você entrou na cozinha $cozinhaName!" else "Pedido de entrada recusado"
        val text =
            if (approved) "Toque para começar a usar o app." else "O supervisor da $cozinhaName recusou seu pedido."
        val notification =
            builder(Channel.Kitchen, openNotifications = false)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        NotificationManagerCompat.from(context).notify(ACCESS_DECISION_ID, notification)
    }

    private fun single(alert: AlertResponse) =
        builder(alert.channel())
            .setContentTitle(alert.title())
            .setContentText(alert.subtitle().ifEmpty { null })
            .setPriority(alert.priority())
            .build()

    // Muitos alertas de uma vez (ex.: vários lotes vencendo) viram uma notificação só.
    private fun summary(alerts: List<AlertResponse>) =
        builder(if (alerts.any { it.type in stockTypes }) Channel.Stock else Channel.Kitchen)
            .setContentTitle("${alerts.size} avisos novos na cozinha")
            .setContentText(alerts.first().title())
            .setStyle(
                NotificationCompat.InboxStyle().also { style ->
                    alerts.take(MAX_SEPARATE).forEach { style.addLine(it.title()) }
                    if (alerts.size > MAX_SEPARATE) style.setSummaryText("e mais ${alerts.size - MAX_SEPARATE}")
                },
            ).setPriority(alerts.maxOf { it.priority() })
            .build()

    private fun builder(
        channel: Channel,
        openNotifications: Boolean = true,
    ) = NotificationCompat
        .Builder(context, channel.id)
        .setSmallIcon(R.drawable.ic_notification)
        .setColor(BRAND_COLOR)
        .setContentIntent(openApp(openNotifications))
        .setAutoCancel(true)

    // Abre o app; com openNotifications, já na tela de Notificações.
    private fun openApp(openNotifications: Boolean): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_OPEN_NOTIFICATIONS, openNotifications)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return PendingIntent.getActivity(
            context,
            if (openNotifications) 0 else 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun AlertResponse.channel(): Channel =
        when {
            type in stockTypes -> Channel.Stock
            type.startsWith(REQUISITION_PREFIX) -> Channel.Requisitions
            else -> Channel.Kitchen
        }

    private fun AlertResponse.priority(): Int =
        when (severity) {
            AlertSeverity.CRITICAL, AlertSeverity.HIGH -> NotificationCompat.PRIORITY_HIGH
            AlertSeverity.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            AlertSeverity.LOW -> NotificationCompat.PRIORITY_LOW
        }

    private companion object {
        const val MAX_SEPARATE = 5
        const val SUMMARY_ID = -1
        const val ACCESS_DECISION_ID = -2
        const val BRAND_COLOR = 0xFF4B0F91.toInt()
    }
}
