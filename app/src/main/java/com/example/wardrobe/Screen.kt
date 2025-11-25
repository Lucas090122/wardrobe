package com.example.wardrobe



sealed class Screen(val route: String, val titleId: Int) {

    sealed class DrawerScreen(route: String, titleId: Int) :
        Screen(route, titleId) {

        data object Home : DrawerScreen("home", R.string.screen_home)

        data object Member : DrawerScreen("member/{memberId}", R.string.screen_member) {
            fun createRoute(memberId: Long) = "member/$memberId"
        }

        data object Statistics : DrawerScreen("statistics", R.string.screen_statistics)
        data object Settings : DrawerScreen("settings", R.string.screen_settings)
        data object Theme : DrawerScreen("theme", R.string.screen_theme)
    }

    data object TransferHistory :
        Screen("transfer_history", R.string.screen_transfer_history)

    data object ClothingInventory :
        Screen("clothing_inventory", R.string.screen_clothing_inventory)
}

val ScreensInDrawer = listOf(
    Screen.DrawerScreen.Statistics,
    Screen.DrawerScreen.Settings
)