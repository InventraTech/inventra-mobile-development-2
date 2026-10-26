package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraProductImage
import com.inventraoficial.inventra.ui.theme.InventraBrand
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraOnBrand
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraPreListCard(
    dateText: String,
    summaryText: String,
    photoUrls: List<String?>,
    extraCount: Int,
    onClick: () -> Unit,
    onConvertClick: () -> Unit,
    modifier: Modifier = Modifier,
    isConverting: Boolean = false,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape)
                .background(InventraBrand)
                .clickable(onClick = onClick)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ListAlt,
                contentDescription = null,
                tint = InventraGold,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "PRÉ-LISTA · $dateText",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                color = InventraGold,
                modifier = Modifier.weight(1f),
            )
            Text("›", fontWeight = FontWeight.Bold, color = InventraOnBrand.copy(alpha = 0.6f), fontSize = 20.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = summaryText,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = InventraOnBrand,
                modifier = Modifier.weight(1f),
            )
            StackedPhotos(photoUrls = photoUrls, extraCount = extraCount)
        }
        Button(
            onClick = { if (!isConverting) onConvertClick() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(percent = 25),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = InventraOnBrand,
                    contentColor = InventraBrand,
                ),
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 24.dp),
        ) {
            if (isConverting) {
                CircularProgressIndicator(color = InventraBrand, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text("Transformar em compra", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun StackedPhotos(
    photoUrls: List<String?>,
    extraCount: Int,
) {
    val photoModifier =
        Modifier
            .size(36.dp)
            .border(2.dp, InventraBrand, CircleShape)
            .clip(CircleShape)
    Row(horizontalArrangement = Arrangement.spacedBy(-10.dp)) {
        photoUrls.forEach { url ->
            InventraProductImage(
                photoUrl = url,
                shape = CircleShape,
                iconSize = 16.dp,
                modifier = photoModifier.background(InventraOnBrand),
            )
        }
        if (extraCount > 0) {
            Box(modifier = photoModifier.background(InventraGold), contentAlignment = Alignment.Center) {
                Text(
                    text = "+$extraCount",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = InventraBrand,
                )
            }
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraPreListCardPreview() {
    Box(modifier = Modifier.background(Color.White).padding(16.dp)) {
        InventraPreListCard(
            dateText = "24/10",
            summaryText = "12 itens · 4 fornecedores",
            photoUrls = listOf(null, null, null, null),
            extraCount = 8,
            onClick = {},
            onConvertClick = {},
        )
    }
}
