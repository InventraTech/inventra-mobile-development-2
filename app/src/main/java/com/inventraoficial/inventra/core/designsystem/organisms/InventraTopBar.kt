package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(InventraPurple, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.CenterStart) {
            navigationIcon()
        }
        Box(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            title()
        }
        Row(horizontalArrangement = Arrangement.End, content = actions)
    }
}

@Preview
@Composable
private fun InventraTopBarBackTitlePreview() {
    InventraTopBar(
        navigationIcon = { Text("←", color = Color.White, fontSize = 20.sp) },
        title = { Text("Dar Baixa", color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        actions = { Text("Cancelar", color = Color.White, fontFamily = Montserrat, fontSize = 13.sp) },
    )
}

@Preview
@Composable
private fun InventraTopBarGreetingPreview() {
    InventraTopBar(
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Seja bem-vindo, Jones!", color = Color.White, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Quinta, 2 de julho de 2026", color = Color.White.copy(alpha = 0.8f), fontFamily = Montserrat, fontSize = 12.sp)
            }
        },
        actions = { Text("🔔", fontSize = 20.sp) },
    )
}
