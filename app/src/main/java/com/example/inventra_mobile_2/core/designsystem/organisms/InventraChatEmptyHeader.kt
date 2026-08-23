package com.example.inventra_mobile_2.core.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.core.designsystem.atoms.InventraBrandIcon
import com.example.inventra_mobile_2.ui.theme.Montserrat

@Composable
fun InventraChatEmptyHeader(
    greeting: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InventraBrandIcon()
        Text(greeting, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Preview
@Composable
private fun InventraChatEmptyHeaderPreview() {
    InventraChatEmptyHeader(greeting = "Olá! 👋")
}