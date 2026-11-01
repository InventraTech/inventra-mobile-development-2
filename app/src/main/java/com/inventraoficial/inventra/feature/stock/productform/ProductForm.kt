package com.inventraoficial.inventra.feature.stock.productform

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPickerField
import com.inventraoficial.inventra.core.designsystem.organisms.InventraPickerOption
import com.inventraoficial.inventra.core.designsystem.organisms.InventraSearchPickerSheet
import com.inventraoficial.inventra.data.remote.dto.product.CategoryResponse
import com.inventraoficial.inventra.data.remote.dto.product.UnitResponse
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class ProductFormState(
    val name: String = "",
    val brand: String = "",
    val barcode: String = "",
    val units: List<InventraPickerOption> = emptyList(),
    val unitId: Int? = null,
    val categories: List<InventraPickerOption> = emptyList(),
    val categoryId: Int? = null,
    val minStock: String = "",
) {
    val unitLabel: String?
        get() = units.firstOrNull { it.id == unitId }?.label()

    val categoryName: String?
        get() = categories.firstOrNull { it.id == categoryId }?.title
}

private fun InventraPickerOption.label(): String = subtitle?.let { "$title ($it)" } ?: title

data class ProductFormActions(
    val onNameChange: (String) -> Unit,
    val onBrandChange: (String) -> Unit,
    val onBarcodeChange: (String) -> Unit,
    val onUnitSelect: (Int?) -> Unit,
    val onCategorySelect: (Int?) -> Unit,
    val onMinStockChange: (String) -> Unit,
)

private enum class OpenPicker { Unit, Category }

@Composable
fun ProductFormFields(
    form: ProductFormState,
    actions: ProductFormActions,
) {
    var openPicker by remember { mutableStateOf<OpenPicker?>(null) }

    InventraLabeledField(
        label = "Nome do produto",
        value = form.name,
        onValueChange = actions.onNameChange,
        placeholder = "Ex.: Arroz branco 5kg",
        modifier = Modifier.fillMaxWidth(),
    )
    InventraLabeledField(
        label = "Marca (opcional)",
        value = form.brand,
        onValueChange = actions.onBrandChange,
        placeholder = "Ex.: Tio João",
        modifier = Modifier.fillMaxWidth(),
    )
    InventraPickerField(
        label = "Unidade de medida",
        value = form.unitLabel,
        placeholder = "Escolha a unidade",
        onClick = { openPicker = OpenPicker.Unit },
        modifier = Modifier.fillMaxWidth(),
    )
    if (form.categories.isNotEmpty()) {
        InventraPickerField(
            label = "Categoria (opcional)",
            value = form.categoryName,
            placeholder = "Escolha a categoria",
            onClick = { openPicker = OpenPicker.Category },
            modifier = Modifier.fillMaxWidth(),
        )
    }
    InventraLabeledField(
        label = "Estoque mínimo na cozinha (opcional)",
        value = form.minStock,
        onValueChange = actions.onMinStockChange,
        keyboardType = KeyboardType.Decimal,
        placeholder = "Avisamos quando ficar abaixo disso",
        modifier = Modifier.fillMaxWidth(),
    )
    InventraLabeledField(
        label = "Código de barras (opcional)",
        value = form.barcode,
        onValueChange = actions.onBarcodeChange,
        keyboardType = KeyboardType.Number,
        placeholder = "Só os números",
        modifier = Modifier.fillMaxWidth(),
    )

    when (openPicker) {
        OpenPicker.Unit ->
            InventraSearchPickerSheet(
                title = "Unidade de medida",
                options = form.units,
                selectedId = form.unitId,
                onSelect = actions.onUnitSelect,
                onDismiss = { openPicker = null },
            )
        OpenPicker.Category ->
            InventraSearchPickerSheet(
                title = "Categoria",
                options = form.categories,
                selectedId = form.categoryId,
                onSelect = actions.onCategorySelect,
                onDismiss = { openPicker = null },
                noneLabel = "Sem categoria",
            )
        null -> Unit
    }
}

internal const val MAX_NAME_LENGTH = 150
internal const val MAX_BRAND_LENGTH = 80
internal const val MAX_BARCODE_LENGTH = 50
private const val MIN_STOCK_DECIMALS = 3

private val decimalInput = Regex("^\\d{0,9}([.,]\\d{0,$MIN_STOCK_DECIMALS})?$")

internal fun String.isMinStockInput(): Boolean = matches(decimalInput)

internal fun String.toDecimal(): Double? = trim().replace(',', '.').toDoubleOrNull()

internal fun Double.toMinStockText(): String {
    val format = DecimalFormat("0.###", DecimalFormatSymbols(Locale.forLanguageTag("pt-BR")))
    return format.format(this)
}

internal fun ProductFormState.validationError(): String? =
    when {
        name.isBlank() -> "Informe o nome do produto"
        unitId == null -> "Escolha a unidade de medida"
        minStock.isNotBlank() && minStock.toDecimal() == null -> "Informe o estoque mínimo só com números"
        else -> null
    }

internal fun List<UnitResponse>.toUnitOptions(): List<InventraPickerOption> =
    sortedBy { unit -> unit.symbol.any { it.isDigit() } }
        .map { unit ->
            val description = unit.description
            if (description == null) {
                InventraPickerOption(unit.id, unit.symbol)
            } else {
                InventraPickerOption(unit.id, description, unit.symbol)
            }
        }

internal fun List<CategoryResponse>.toCategoryOptions(): List<InventraPickerOption> =
    sortedBy { it.name.lowercase() }.map { InventraPickerOption(it.id, it.name) }
