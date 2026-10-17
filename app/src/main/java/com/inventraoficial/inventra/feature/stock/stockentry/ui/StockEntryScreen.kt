package com.inventraoficial.inventra.feature.stock.stockentry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.atoms.InventraBackButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPrimaryButton
import com.inventraoficial.inventra.core.designsystem.atoms.InventraSecondaryButton
import com.inventraoficial.inventra.core.designsystem.organisms.InventraTopBar
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.Montserrat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun StockEntryScreen(
    uiState: StockEntryUiState,
    onQuantityChange: (String) -> Unit,
    onBatchNumberChange: (String) -> Unit,
    onExpirationDateSelect: (LocalDate) -> Unit,
    onEntryDateSelect: (LocalDate) -> Unit,
    onSupplierSelect: (Int?) -> Unit,
    onInvoiceNumberChange: (String) -> Unit,
    onUnitPriceChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        InventraTopBar(
            navigationIcon = {
                InventraBackButton(onClick = onBackClick)
            },
            title = {
                Text(
                    "Dar Entrada",
                    color = Color.White,
                    fontFamily = Montserrat,
                    fontSize = 17.sp,
                )
            },
        )
        val contentModifier = Modifier.weight(1f).fillMaxWidth()
        when {
            uiState.isLoading ->
                Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = InventraPurple)
                }
            uiState.errorMessage != null ->
                Column(
                    modifier = contentModifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(uiState.errorMessage, fontFamily = Montserrat, fontSize = 15.sp, textAlign = TextAlign.Center)
                    InventraSecondaryButton(text = "Tentar novamente", onClick = onRetryClick)
                }
            uiState.registeredBatch != null ->
                StockEntryDone(
                    productName = uiState.productName,
                    batch = uiState.registeredBatch,
                    onDoneClick = onBackClick,
                    modifier = contentModifier,
                )
            else ->
                StockEntryForm(
                    uiState = uiState,
                    onQuantityChange = onQuantityChange,
                    onBatchNumberChange = onBatchNumberChange,
                    onExpirationDateSelect = onExpirationDateSelect,
                    onEntryDateSelect = onEntryDateSelect,
                    onSupplierSelect = onSupplierSelect,
                    onInvoiceNumberChange = onInvoiceNumberChange,
                    onUnitPriceChange = onUnitPriceChange,
                    onConfirmClick = onConfirmClick,
                    modifier = contentModifier,
                )
        }
    }
}

private enum class DateField { Expiration, Entry }

@Composable
private fun StockEntryForm(
    uiState: StockEntryUiState,
    onQuantityChange: (String) -> Unit,
    onBatchNumberChange: (String) -> Unit,
    onExpirationDateSelect: (LocalDate) -> Unit,
    onEntryDateSelect: (LocalDate) -> Unit,
    onSupplierSelect: (Int?) -> Unit,
    onInvoiceNumberChange: (String) -> Unit,
    onUnitPriceChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var openDateField by remember { mutableStateOf<DateField?>(null) }

    Column(
        modifier =
            modifier
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                uiState.productName,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = InventraPurple,
            )
            uiState.details?.let { Text(it, fontFamily = Montserrat, fontSize = 13.sp, color = InventraMuted) }
        }
        InventraLabeledField(
            label = "Quantidade recebida (${uiState.unitSymbol})",
            value = uiState.quantity,
            onValueChange = onQuantityChange,
            keyboardType = KeyboardType.Decimal,
            placeholder = "Ex.: 12",
            modifier = Modifier.fillMaxWidth(),
        )
        InventraLabeledField(
            label = "Número do lote",
            value = uiState.batchNumber,
            onValueChange = onBatchNumberChange,
            placeholder = "Como está na embalagem",
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PickerField(
                label = "Validade",
                value = uiState.expirationDate?.format(dateFormatter),
                placeholder = "dd/mm/aaaa",
                icon = Icons.Default.CalendarMonth,
                onClick = { openDateField = DateField.Expiration },
                modifier = Modifier.weight(1f),
            )
            PickerField(
                label = "Entrada",
                value = uiState.entryDate?.format(dateFormatter),
                placeholder = "dd/mm/aaaa",
                icon = Icons.Default.CalendarMonth,
                onClick = { openDateField = DateField.Entry },
                modifier = Modifier.weight(1f),
            )
        }
        if (uiState.suppliers.isNotEmpty()) {
            SupplierField(
                suppliers = uiState.suppliers,
                selectedName = uiState.supplierName,
                onSupplierSelect = onSupplierSelect,
            )
        }
        InventraLabeledField(
            label = "Nota fiscal (opcional)",
            value = uiState.invoiceNumber,
            onValueChange = onInvoiceNumberChange,
            placeholder = "Número da nota",
            modifier = Modifier.fillMaxWidth(),
        )
        InventraLabeledField(
            label = "Preço unitário em R$ (opcional)",
            value = uiState.unitPrice,
            onValueChange = onUnitPriceChange,
            keyboardType = KeyboardType.Decimal,
            placeholder = "Ex.: 4,50",
            modifier = Modifier.fillMaxWidth(),
        )
        uiState.submitError?.let {
            Text(it, fontFamily = Montserrat, fontSize = 14.sp, color = InventraDanger)
        }
        InventraPrimaryButton(
            text = "Confirmar entrada",
            onClick = onConfirmClick,
            isLoading = uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    when (openDateField) {
        DateField.Expiration ->
            DatePickerSheet(
                initialDate = uiState.expirationDate ?: uiState.entryDate,
                onDateSelect = onExpirationDateSelect,
                onDismiss = { openDateField = null },
            )
        DateField.Entry ->
            DatePickerSheet(
                initialDate = uiState.entryDate,
                onDateSelect = onEntryDateSelect,
                onDismiss = { openDateField = null },
            )
        null -> Unit
    }
}

// Campo que abre uma escolha (data ou fornecedor) em vez de receber texto digitado.
@Composable
private fun PickerField(
    label: String,
    value: String?,
    placeholder: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, fontFamily = Montserrat, fontSize = 18.sp, color = InventraPurple)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value ?: placeholder,
                fontFamily = Montserrat,
                fontSize = if (value == null) 13.sp else 15.sp,
                color = if (value == null) InventraMuted else Color.Unspecified,
                modifier = Modifier.weight(1f),
            )
            Icon(imageVector = icon, contentDescription = null, tint = InventraPurple)
        }
    }
}

@Composable
private fun SupplierField(
    suppliers: List<SupplierOption>,
    selectedName: String?,
    onSupplierSelect: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PickerField(
            label = "Fornecedor (opcional)",
            value = selectedName,
            placeholder = "Escolha o fornecedor",
            icon = Icons.Default.ArrowDropDown,
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Nenhum", fontFamily = Montserrat, color = InventraMuted) },
                onClick = {
                    onSupplierSelect(null)
                    expanded = false
                },
            )
            suppliers.forEach { supplier ->
                DropdownMenuItem(
                    text = { Text(supplier.name, fontFamily = Montserrat) },
                    onClick = {
                        onSupplierSelect(supplier.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerSheet(
    initialDate: LocalDate?,
    onDateSelect: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    // O DatePicker trabalha com a meia-noite UTC do dia escolhido.
    val state =
        rememberDatePickerState(
            initialSelectedDateMillis = initialDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onDateSelect(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    onDismiss()
                },
            ) {
                Text("OK", color = InventraPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = InventraMuted)
            }
        },
    ) {
        DatePicker(state = state)
    }
}

@Composable
private fun StockEntryDone(
    productName: String,
    batch: RegisteredBatch,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(96.dp)
                    .background(InventraSuccess.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = InventraSuccess,
                modifier = Modifier.size(48.dp),
            )
        }
        Text("Entrada registrada", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(productName, fontFamily = Montserrat, fontSize = 14.sp, color = InventraMuted)
        Text(
            "Lote ${batch.batchNumber}: +${batch.quantity}\nValidade ${batch.expiration}",
            fontFamily = Montserrat,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        InventraPrimaryButton(
            text = "Concluir",
            onClick = onDoneClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
        )
    }
}

@Composable
fun StockEntryRoute(
    productId: Int,
    navigator: Navigator,
    viewModel: StockEntryViewModel =
        viewModel(key = "stock-entry-$productId", factory = StockEntryViewModel.factory(productId)),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    StockEntryScreen(
        uiState = uiState,
        onQuantityChange = viewModel::onQuantityChange,
        onBatchNumberChange = viewModel::onBatchNumberChange,
        onExpirationDateSelect = viewModel::onExpirationDateSelect,
        onEntryDateSelect = viewModel::onEntryDateSelect,
        onSupplierSelect = viewModel::onSupplierSelect,
        onInvoiceNumberChange = viewModel::onInvoiceNumberChange,
        onUnitPriceChange = viewModel::onUnitPriceChange,
        onConfirmClick = viewModel::onConfirmClick,
        onRetryClick = viewModel::onRetryClick,
        onBackClick = { navigator.back() },
        modifier = modifier,
    )
}

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun StockEntryScreenPreview() {
    StockEntryScreen(
        uiState =
            StockEntryUiState(
                isLoading = false,
                productName = "Leite Integral",
                details = "Italac · Laticínios",
                unitSymbol = "L",
                entryDate = LocalDate.parse("2026-10-17"),
                suppliers = listOf(SupplierOption(1, "Laticínios Bom Leite")),
            ),
        onQuantityChange = {},
        onBatchNumberChange = {},
        onExpirationDateSelect = {},
        onEntryDateSelect = {},
        onSupplierSelect = {},
        onInvoiceNumberChange = {},
        onUnitPriceChange = {},
        onConfirmClick = {},
        onRetryClick = {},
        onBackClick = {},
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun StockEntryDonePreview() {
    StockEntryScreen(
        uiState =
            StockEntryUiState(
                isLoading = false,
                productName = "Leite Integral",
                registeredBatch = RegisteredBatch("L2026-010", "12 L", "30/11/2026"),
            ),
        onQuantityChange = {},
        onBatchNumberChange = {},
        onExpirationDateSelect = {},
        onEntryDateSelect = {},
        onSupplierSelect = {},
        onInvoiceNumberChange = {},
        onUnitPriceChange = {},
        onConfirmClick = {},
        onRetryClick = {},
        onBackClick = {},
    )
}
