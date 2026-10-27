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
    LoginSignIn("bt_mb_login_sign_in"),
    LoginGoogle("bt_mb_login_google"),
    LoginForgotPassword("link_mb_login_forgot_password"),
    SignupRoleSupervisor("bt_mb_signup_role_supervisor"),
    SignupRoleStockist("bt_mb_signup_role_stockist"),
    SignupCreate("bt_mb_signup_create"),
    SignupHaveAccount("link_mb_signup_have_account"),
    KitchensClose("icon_mb_kitchens_close"),
    KitchensFilter("icon_mb_kitchens_filter"),
    KitchensTabList("tab_mb_kitchens_list"),
    KitchensTabMap("tab_mb_kitchens_map"),
    KitchensRequestEntry("bt_mb_kitchens_request_entry"),
    KitchensMapPin("icon_mb_kitchens_map_pin"),
    KitchenCreateClose("icon_mb_kitchen_create_close"),
    KitchenCreateCreate("bt_mb_kitchen_create_create"),
    KitchenEditBack("icon_mb_kitchen_edit_back"),
    KitchenEditSave("bt_mb_kitchen_edit_save"),
}
