package com.inventraoficial.inventra

import android.app.Application
import com.inventraoficial.inventra.di.AppContainer
import com.inventraoficial.inventra.di.DefaultAppContainer
import com.inventraoficial.inventra.feature.notifications.background.AlertCheckWorker

class InventraApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        container.alertNotifier.createChannels()
        container.clickTracker.sendOnLogin()
        // Sem sessão ou sem cozinha, cada rodada só termina sem fazer nada.
        AlertCheckWorker.schedule(this)
    }
}
