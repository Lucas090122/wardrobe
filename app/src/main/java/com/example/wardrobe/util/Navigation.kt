package com.example.wardrobe.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.wardrobe.Screen
import com.example.wardrobe.WardrobeApp
import com.example.wardrobe.data.WeatherInfo
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.ui.Home
import com.example.wardrobe.ui.SettingsScreen
import com.example.wardrobe.ui.StatisticsScreen
import com.example.wardrobe.ui.ClothingInventoryScreen
import com.example.wardrobe.ui.TransferHistoryScreen
import com.example.wardrobe.viewmodel.MainViewModel
import com.example.wardrobe.viewmodel.MemberViewModel
import com.example.wardrobe.viewmodel.StatisticsViewModel

/**
 * Centralized navigation host for all drawer destinations.
 *
 * This NavHost controls:
 *  ▸ Home page
 *  ▸ Member wardrobe screen (WardrobeApp)
 *  ▸ Statistics
 *  ▸ Settings
 *
 * Weather is passed through so that the WardrobeApp and recommendations
 * can use it without recalculating or fetching multiple times.
 */
@Composable
fun Navigation(
    repo: WardrobeRepository,
    vm: MemberViewModel,
    navController: NavController,
    viewModel: MainViewModel,           // global MainViewModel (includes NFC state)
    statisticsViewModel: StatisticsViewModel,
    pd: PaddingValues,
    weather: WeatherInfo?
) {
    NavHost(
        navController = navController as NavHostController,
        startDestination = Screen.DrawerScreen.Home.route,
        modifier = Modifier.padding(
            top = 64.dp,                         // Offset for top drawer bar
            bottom = pd.calculateBottomPadding() // Keeps bottom bar safe area
        )
    ) {
        // -------------------------------------------------------------
        // HOME SCREEN (default)
        // -------------------------------------------------------------
        composable(Screen.DrawerScreen.Home.route) {
            Home( vm,weather)
        }

        // -------------------------------------------------------------
        // MEMBER’S PERSONAL WARDROBE SCREEN
        //
        // Navigation example:
        //   navController.navigate("member/3")
        //
        // This opens WardrobeApp with the selected memberId.
        // -------------------------------------------------------------
        composable(
            route = Screen.DrawerScreen.Member.route,
            arguments = listOf(navArgument("memberId") { type = NavType.LongType })
        ) { backStackEntry ->
            val memberId: Long = backStackEntry.arguments?.getLong("memberId") ?: return@composable

            WardrobeApp(
                memberId = memberId,
                weather = weather,
                onExit = {
                    vm.setCurrentMember(null)
                    navController.popBackStack()
                }
            )
        }

        // -------------------------------------------------------------
        // STATISTICS SCREEN
        // -------------------------------------------------------------
        composable(Screen.DrawerScreen.Statistics.route) {
            StatisticsScreen(
                navController = navController
            )
        }

        // -------------------------------------------------------------
        // SETTINGS SCREEN
        // -------------------------------------------------------------
        composable(Screen.DrawerScreen.Settings.route) {
            SettingsScreen(
                repo = repo,
                mainVm = viewModel     // pass global MainViewModel for NFC binding
            )
        }

        composable(Screen.TransferHistory.route) {
            TransferHistoryScreen(repo = repo, navController = navController)
        }
        composable(Screen.ClothingInventory.route) {
            ClothingInventoryScreen(vm = statisticsViewModel, navController = navController)
        }
    }
}