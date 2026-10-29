package com.inventraoficial.inventra.feature.suppliers.create.ui

data class CreatedSupplier(
    val id: Int,
    val name: String,
)

data class SupplierCreateUiState(
    val legalName: String = "",
    val cnpj: String = "",
    val email: String = "",
    val whatsapp: String = "",
    val rating: Int = 0,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val created: CreatedSupplier? = null,
)

data class SupplierCreateActions(
    val onLegalNameChange: (String) -> Unit = {},
    val onCnpjChange: (String) -> Unit = {},
    val onEmailChange: (String) -> Unit = {},
    val onWhatsappChange: (String) -> Unit = {},
    val onRatingSelect: (Int) -> Unit = {},
    val onSaveClick: () -> Unit = {},
    val onBackClick: () -> Unit = {},
    val onSeeSupplierClick: (Int) -> Unit = {},
)
