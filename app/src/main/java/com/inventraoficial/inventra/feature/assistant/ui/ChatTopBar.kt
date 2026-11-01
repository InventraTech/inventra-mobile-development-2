package com.inventraoficial.inventra.feature.assistant.ui

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun ChatTopBar(
    onMenuClick: () -> Unit,
    onInicioClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    InventraTopBar(
        modifier = modifier,
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Abrir conversas anteriores",
                tint = Color.White,
                modifier = Modifier.clickable(onClick = onMenuClick),
            )
        },
        title = {
            Text(
                text = "IAI",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
            )
        },
        actions = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar ao início",
                tint = Color.White,
                modifier = Modifier.clickable(onClick = onInicioClick),
            )
        },
    )
}
