package com.example.inventra_mobile_2.core.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.ui.theme.InventraPurple
import com.example.inventra_mobile_2.ui.theme.Montserrat

@Composable
fun InventraQuantityStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StepperCircle(symbol = "−", onClick = { if (value > min) onValueChange(value - 1) })
        Text(text = value.toString(), fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        StepperCircle(symbol = "+", onClick = { onValueChange(value + 1) })
    }
}

@Composable
private fun StepperCircle(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .border(1.5.dp, InventraPurple, CircleShape)
            .background(androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, color = InventraPurple, fontWeight = FontWeight.Bold)
    }
}

@Preview
@Composable
private fun InventraQuantityStepperPreview() {
    InventraQuantityStepper(value = 0, onValueChange = {})
}