package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraAuthHeader(
    appName: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        val logoSize = (maxWidth * LOGO_WIDTH_FRACTION).coerceIn(MinLogoSize, MaxLogoSize)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_inventra_logo),
                contentDescription = "Inventra Logo",
                modifier = Modifier.size(logoSize),
            )
            Text(
                appName,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = InventraPurple,
            )
            Text(subtitle, fontFamily = Montserrat, fontSize = 14.sp)
        }
    }
}

private const val LOGO_WIDTH_FRACTION = 0.4f
private val MinLogoSize = 96.dp
private val MaxLogoSize = 160.dp

@Preview
@Composable
private fun InventraAuthHeaderPreview() {
    InventraAuthHeader(appName = "Inventra", subtitle = "Seja bem-vindo(a) ao Inventra!")
}
