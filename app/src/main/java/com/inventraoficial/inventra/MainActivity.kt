package com.inventraoficial.inventra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.inventraoficial.inventra.feature.notifications.ui.NotificationRoute
import com.inventraoficial.inventra.feature.stock.list.ui.StockListRoute
import com.inventraoficial.inventra.feature.suppliers.ui.SupplierRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockListRoute()
        }
    }
}
