package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraFilterChip
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraTextLink
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventraFiltersSheet(
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = InventraBackground,
    ) {
        InventraFiltersContent(onClear = onClear, onApply = onApply, content = content)
    }
}

@Composable
fun InventraFiltersContent(
    onClear: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Filtros",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = InventraInk,
                modifier = Modifier.weight(1f),
            )
            InventraTextLink(text = "Limpar", onClick = onClear, color = InventraDanger)
        }
        content()
        InventraPrimaryButton(text = "Aplicar filtros", onClick = onApply, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun InventraFilterSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = InventraPurple,
        )
        content()
    }
}

@Composable
fun InventraRadioOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = InventraPurple),
        )
        Text(text = label, fontFamily = Montserrat, fontSize = 13.sp, color = InventraInk)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventraChipGroup(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraFiltersContentPreview() {
    InventraFiltersContent(onClear = {}, onApply = {}) {
        InventraFilterSection("Ordenar por") {
            InventraRadioOption(label = "Nome (A–Z)", selected = true, onClick = {})
            InventraRadioOption(label = "Melhor avaliação", selected = false, onClick = {})
        }
        InventraFilterSection("Status") {
            InventraChipGroup {
                InventraFilterChip(label = "Vencendo em breve", selected = true, onClick = {})
                InventraFilterChip(label = "Sem estoque", selected = false, onClick = {})
            }
        }
    }
}
