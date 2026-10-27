package com.inventraoficial.inventra.data.event

enum class UiClick(
    val column: String,
) {
    NavHome("nav_mb_global_home"),
    NavStock("nav_mb_global_stock"),
    NavRequisitions("nav_mb_global_requisitions"),
    NavSuppliers("nav_mb_global_suppliers"),
    NavProfile("nav_mb_global_profile"),
    FabOcrEntry("fab_mb_global_ocr_entry"),
}
