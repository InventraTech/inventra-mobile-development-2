package com.example.inventra_mobile_2.core.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.core.designsystem.atoms.InventraBrandIcon
import com.example.inventra_mobile_2.ui.theme.InventraPurple
import com.example.inventra_mobile_2.ui.theme.Montserrat

@Composable
fun InventraAuthHeader(
    appName: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InventraBrandIcon(
            badgeContent = { Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        )
        Text(appName, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = InventraPurple)
        Text(subtitle, fontFamily = Montserrat, fontSize = 14.sp)
    }
}

@Preview
@Composable
private fun InventraAuthHeaderPreview() {
    InventraAuthHeader(appName = "Inventra", subtitle = "Seja bem-vindo(a) ao Inventra!")
}