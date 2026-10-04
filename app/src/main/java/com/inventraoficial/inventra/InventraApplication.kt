package com.inventraoficial.inventra

import android.app.Application
import com.inventraoficial.inventra.di.AppContainer
import com.inventraoficial.inventra.di.DefaultAppContainer

class InventraApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
