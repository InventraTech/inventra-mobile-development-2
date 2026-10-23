package com.inventraoficial.inventra.core.designsystem.organisms

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSurface
import com.inventraoficial.inventra.ui.theme.Montserrat
import java.text.Normalizer

// Escolha numa lista grande (milhares de unidades ou categorias): busca sem acento e mostra só os primeiros
// resultados, para a lista não travar.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventraSearchPickerSheet(
    title: String,
    options: List<InventraPickerOption>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    noneLabel: String? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = InventraSurface,
    ) {
        InventraSearchPickerContent(
            title = title,
            options = options,
            selectedId = selectedId,
            onSelect = { id ->
                onSelect(id)
                onDismiss()
            },
            noneLabel = noneLabel,
        )
    }
}

@Composable
fun InventraSearchPickerContent(
    title: String,
    options: List<InventraPickerOption>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    noneLabel: String? = null,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val keys = remember(options) { options.map { "${it.title} ${it.subtitle.orEmpty()}".normalized() } }
    val matches =
        remember(options, keys, query) {
            val terms = query.normalized().split(' ').filter { it.isNotEmpty() }
            options.filterIndexed { index, _ -> terms.all { it in keys[index] } }
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight(SHEET_HEIGHT)
                .imePadding()
                .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = InventraPurple)
        SearchField(query = query, onQueryChange = { query = it })
        LazyColumn(modifier = Modifier.weight(1f)) {
            if (noneLabel != null && query.isBlank()) {
                item { OptionRow(title = noneLabel, subtitle = null, selected = selectedId == null) { onSelect(null) } }
            }
            items(matches.take(MAX_RESULTS), key = { it.id }) { option ->
                OptionRow(option.title, option.subtitle, selected = option.id == selectedId) { onSelect(option.id) }
            }
            item { ResultsFooter(shown = minOf(matches.size, MAX_RESULTS), total = matches.size) }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = InventraMuted)
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(fontFamily = Montserrat, fontSize = 15.sp, color = InventraInk),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (query.isEmpty()) Text("Buscar", fontFamily = Montserrat, fontSize = 15.sp, color = InventraMuted)
                inner()
            },
        )
    }
}

@Composable
private fun OptionRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontFamily = Montserrat, fontSize = 15.sp)
            subtitle?.let { Text(it, fontFamily = Montserrat, fontSize = 12.sp, color = InventraMuted) }
        }
        if (selected) Icon(Icons.Default.Check, contentDescription = "Selecionado", tint = InventraPurple)
    }
    HorizontalDivider(color = InventraPurple.copy(alpha = 0.08f))
}

@Composable
private fun ResultsFooter(
    shown: Int,
    total: Int,
) {
    val text =
        when {
            total == 0 -> "Nada encontrado com essa busca."
            shown < total -> "Mostrando $shown de $total. Digite para encontrar mais rápido."
            else -> null
        }
    text?.let {
        Text(
            it,
            fontFamily = Montserrat,
            fontSize = 13.sp,
            color = InventraMuted,
            modifier = Modifier.padding(vertical = 16.dp),
        )
    }
}

private fun String.normalized(): String =
    Normalizer
        .normalize(lowercase(), Normalizer.Form.NFD)
        .replace(diacritics, "")

private val diacritics = Regex("\\p{Mn}+")
private const val MAX_RESULTS = 50
private const val SHEET_HEIGHT = 0.85f

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun InventraSearchPickerContentPreview() {
    InventraSearchPickerContent(
        title = "Unidade de medida",
        options =
            listOf(
                InventraPickerOption(1, "Quilograma", "KG"),
                InventraPickerOption(2, "Litro", "L"),
                InventraPickerOption(0, "Unidade", "UN"),
            ),
        selectedId = 2,
        onSelect = {},
    )
}
