package com.inventraoficial.inventra.feature.requirements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraBrand
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

private val tabs = listOf("Requisições", "Histórico")

@Composable
fun RequisitionTabs(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 56.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEachIndexed { index, tab ->
            FilterChip(
                selected = selectedIndex == index,
                onClick = { if (index != selectedIndex) onSelect(index) },
                label = {
                    Text(text = tab, fontFamily = Montserrat, fontWeight = FontWeight.Medium, fontSize = 18.sp)
                },
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = InventraBrand,
                        selectedLabelColor = Color.White,
                        labelColor = InventraPurple,
                    ),
                modifier = Modifier.weight(1f).heightIn(min = 56.dp),
            )
        }
    }
}
