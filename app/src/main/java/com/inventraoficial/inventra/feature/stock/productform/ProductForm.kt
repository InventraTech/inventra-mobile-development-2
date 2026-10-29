package com.inventraoficial.inventra.feature.stock.productform

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.core.designsystem.atoms.InventraLabeledField
import com.inventraoficial.inventra.core.designsystem.atoms.InventraPickerField
import com.inventraoficial.inventra.core.designsystem.organisms.InventraPickerOption
import com.inventraoficial.inventra.core.designsystem.organisms.InventraSearchPickerSheet
import com.inventraoficial.inventra.data.remote.dto.product.CategoryResponse
import com.inventraoficial.inventra.data.remote.dto.product.UnitResponse
import com.inventraoficial.inventra.data.remote.dto.supplier.SupplierResponse
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.Montserrat
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
    val suppliers: List<InventraPickerOption> = emptyList(),
    val supplierId: Int? = null,
    val referencePrice: String = "",
    val isSupplierRequired: Boolean = false,
) {
    val unitLabel: String?
        get() = units.firstOrNull { it.id == unitId }?.label()

    val categoryName: String?
        get() = categories.firstOrNull { it.id == categoryId }?.title

    val supplierName: String?
        get() = suppliers.firstOrNull { it.id == supplierId }?.title
}

private fun InventraPickerOption.label(): String = subtitle?.let { "$title ($it)" } ?: title

data class ProductFormActions(
    val onNameChange: (String) -> Unit = {},
    val onBrandChange: (String) -> Unit = {},
    val onBarcodeChange: (String) -> Unit = {},
    val onUnitSelect: (Int?) -> Unit = {},
    val onCategorySelect: (Int?) -> Unit = {},
    val onMinStockChange: (String) -> Unit = {},
    val onSupplierSelect: (Int?) -> Unit = {},
    val onReferencePriceChange: (String) -> Unit = {},
)

fun productFormActions(update: ((ProductFormState) -> ProductFormState) -> Unit) =
    ProductFormActions(
        onNameChange = { name -> update { it.copy(name = name.take(MAX_NAME_LENGTH)) } },
        onBrandChange = { brand -> update { it.copy(brand = brand.take(MAX_BRAND_LENGTH)) } },
        onBarcodeChange = { barcode ->
            if (barcode.all { it.isDigit() }) update { it.copy(barcode = barcode.take(MAX_BARCODE_LENGTH)) }
        },
        onUnitSelect = { unitId -> update { it.copy(unitId = unitId) } },
        onCategorySelect = { categoryId -> update { it.copy(categoryId = categoryId) } },
        onMinStockChange = { minStock ->
            if (minStock.matches(minStockInput)) update { it.copy(minStock = minStock) }
        },
        onSupplierSelect = { supplierId ->
            update { form ->
                val price = if (supplierId == null) "" else form.referencePrice
                form.copy(supplierId = supplierId, referencePrice = price)
            }
        },
        onReferencePriceChange = { price ->
            if (price.matches(priceInput)) update { it.copy(referencePrice = price) }
        },
    )

private enum class OpenPicker { Unit, Category, Supplier }

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
    if (form.suppliers.isNotEmpty()) {
        InventraPickerField(
            label = if (form.isSupplierRequired) "Fornecedor" else "Fornecedor (opcional)",
            value = form.supplierName,
            placeholder = "De quem a cozinha compra",
            onClick = { openPicker = OpenPicker.Supplier },
            modifier = Modifier.fillMaxWidth(),
        )
    } else if (form.isSupplierRequired) {
        Text(
            text = NO_SUPPLIERS_MESSAGE,
            fontFamily = Montserrat,
            fontSize = 14.sp,
            color = InventraDanger,
        )
    }
    if (form.supplierId != null) {
        InventraLabeledField(
            label = "Preço de referência (opcional)",
            value = form.referencePrice,
            onValueChange = actions.onReferencePriceChange,
            keyboardType = KeyboardType.Decimal,
            placeholder = "Sugerido nas compras desse fornecedor",
            modifier = Modifier.fillMaxWidth(),
        )
    }

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
        OpenPicker.Supplier ->
            InventraSearchPickerSheet(
                title = "Fornecedor",
                options = form.suppliers,
                selectedId = form.supplierId,
                onSelect = actions.onSupplierSelect,
                onDismiss = { openPicker = null },
                noneLabel = if (form.isSupplierRequired) null else "Sem fornecedor",
            )
        null -> Unit
    }
}

internal const val MAX_NAME_LENGTH = 150
internal const val MAX_BRAND_LENGTH = 80
internal const val MAX_BARCODE_LENGTH = 50
private const val NO_SUPPLIERS_MESSAGE =
    "Nenhum fornecedor ativo cadastrado. Cadastre um fornecedor antes de cadastrar o produto."
private val minStockInput = Regex("^\\d{0,9}([.,]\\d{0,3})?$")
private val priceInput = Regex("^\\d{0,10}([.,]\\d{0,2})?$")
private val brazilianSymbols = DecimalFormatSymbols(Locale.forLanguageTag("pt-BR"))

internal fun String.toDecimal(): Double? = trim().replace(',', '.').toDoubleOrNull()

internal fun Double.toMinStockText(): String = DecimalFormat("0.###", brazilianSymbols).format(this)

internal fun Double.toPriceText(): String = DecimalFormat("0.00", brazilianSymbols).format(this)

internal fun ProductFormState.validationError(): String? =
    when {
        name.isBlank() -> "Informe o nome do produto"
        unitId == null -> "Escolha a unidade de medida"
        isSupplierRequired && supplierId == null -> "Escolha o fornecedor do produto"
        minStock.isNotBlank() && minStock.toDecimal() == null -> "Informe o estoque mínimo só com números"
        referencePrice.isNotBlank() && referencePrice.toDecimal() == null -> "Informe o preço só com números"
        else -> null
    }

internal fun List<SupplierResponse>.toSupplierOptions(): List<InventraPickerOption> =
    sortedBy { it.legalName.lowercase() }.map { InventraPickerOption(it.id, it.legalName) }

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
