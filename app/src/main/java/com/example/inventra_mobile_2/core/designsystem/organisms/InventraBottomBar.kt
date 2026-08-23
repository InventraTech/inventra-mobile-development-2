package com.example.inventra_mobile_2.core.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.ui.theme.InventraNavInk
import com.example.inventra_mobile_2.ui.theme.InventraPurple

@Composable
fun InventraBottomBar(
    onHomeClick: () -> Unit,
    onStockClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(InventraNavInk, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text("📦", fontSize = 20.sp, modifier = Modifier.clickable(onClick = onStockClick))
            Spacer(modifier = Modifier.size(44.dp))
            Text("⏱", fontSize = 20.sp, modifier = Modifier.clickable(onClick = onHistoryClick))
            Text("🔔", fontSize = 20.sp, modifier = Modifier.clickable(onClick = onNotificationsClick))
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(InventraPurple)
                .clickable(onClick = onHomeClick),
            contentAlignment = Alignment.Center
        ) {
            Text("⌂", color = Color.White, fontSize = 20.sp)
        }
    }
}

@Preview
@Composable
private fun InventraBottomBarPreview() {
    InventraBottomBar(onHomeClick = {}, onStockClick = {}, onHistoryClick = {}, onNotificationsClick = {})
}