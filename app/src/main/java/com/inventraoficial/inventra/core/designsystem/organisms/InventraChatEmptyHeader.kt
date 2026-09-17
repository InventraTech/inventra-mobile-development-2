package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraChatEmptyHeader(
    greeting: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_inventra_logo),
            contentDescription = "Inventra Logo",
            modifier = Modifier.size(96.dp),
        )
        Text(greeting, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Preview
@Composable
private fun InventraChatEmptyHeaderPreview() {
    InventraChatEmptyHeader(greeting = "Olá! 👋")
}
