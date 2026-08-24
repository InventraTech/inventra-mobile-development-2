package com.example.inventra_mobile_2.core.designsystem.molecules

import android.security.KeyStoreException
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.R
import com.example.inventra_mobile_2.core.designsystem.atoms.InventraThumbnail
import com.example.inventra_mobile_2.ui.theme.InventraDanger
import com.example.inventra_mobile_2.ui.theme.InventraPurple
import com.example.inventra_mobile_2.ui.theme.Montserrat

enum class InventraUrgency { Normal, Tomorrow }

@Composable
fun InventraPriorityRow(
    painter: Painter,
    title: String,
    statusText: String,
    urgency: InventraUrgency,
    modifier: Modifier = Modifier,
    batch: String
) {
    val statusColor = if (urgency == InventraUrgency.Tomorrow) InventraDanger else InventraPurple
    Row(
        modifier = modifier
            .border(1.dp, color = Color.LightGray, shape = RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .height(100.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InventraThumbnail(painter = painter, modifier = Modifier.padding(15.dp, 0.dp, 15.dp, 0.dp))
        Column {
            Text(title, fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = InventraPurple, fontSize =  20.sp)
            Text(statusText, fontFamily = Montserrat, color = statusColor, fontSize = 16.sp)
            Text(batch, fontFamily = Montserrat, color = statusColor, fontSize = 18.sp)
        }
    }
}

@Preview
@Composable
private fun InventraPriorityRowPreview() {
    InventraPriorityRow(
        painter = painterResource(R.drawable.ic_inventra_logo),
        title = "Arroz Camil",
        statusText = "Vence em 2 dias",
        urgency = InventraUrgency.Normal,
        batch = "Lote bla bla bla"
    )
}