package com.example.wardrobe



sealed class Screen(
    val title:String,
    val route: String,
){  //<-- To enable future development in case bottom panel is required!
    sealed class DrawerScreen(
        val dTitle: String,
        val dRoute: String,
    ): Screen(dTitle, dRoute){

        object Home: DrawerScreen(
            dTitle = "Home",
            dRoute = "home"
        )

        object Member : DrawerScreen(
            dTitle = "Member",
            dRoute = "member/{memberId}"
        ) {
            fun createRoute(memberId: Long) = "member/$memberId"
        }

        object Statistics: DrawerScreen(
            dTitle = "Statistics",
            dRoute = "statistics",
        )

        object Settings: DrawerScreen(
            dTitle = "Settings",
            dRoute = "settings",
        )

        object Theme: DrawerScreen(
            dTitle = "Theme",
            dRoute = "theme",
        )
    }
    object TransferHistory: Screen("Transfer History", "transfer_history")
}

val ScreensInDrawer = listOf(
    Screen.DrawerScreen.Statistics,
    Screen.DrawerScreen.Settings
)