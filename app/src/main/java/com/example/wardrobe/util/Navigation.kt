//package com.example.wardrobe.util
//
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.padding
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import androidx.navigation.NavHostController
//import androidx.navigation.NavType
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.navArgument
//import com.example.wardrobe.Screen
//import com.example.wardrobe.WardrobeApp
//import com.example.wardrobe.data.WardrobeRepository
//import com.example.wardrobe.ui.Home
//import com.example.wardrobe.ui.SettingsScreen
//import com.example.wardrobe.ui.StatisticsScreen
//import com.example.wardrobe.ui.TransferHistoryScreen
//import com.example.wardrobe.ui.ClothingInventoryScreen
//import com.example.wardrobe.viewmodel.MainViewModel
//import com.example.wardrobe.viewmodel.MemberViewModel
//import com.example.wardrobe.viewmodel.StatisticsViewModel
//
//
//@Composable
//fun Navigation(
//    repo: WardrobeRepository,
//    vm : MemberViewModel,
//    navController: NavController,
//    viewModel: MainViewModel,
//    statisticsViewModel: StatisticsViewModel,
//    pd: PaddingValues
//){
//    NavHost(
//        navController = navController as NavHostController,
//        startDestination = Screen.DrawerScreen.Home.route,
//        modifier = Modifier.padding(
//            top = 64.dp,
//            bottom = pd.calculateBottomPadding()
//        )
//    ) {
//        composable(Screen.DrawerScreen.Home.route){
//            Home(
//                repo = repo,
//                vm
//            )
//        }
//        composable(route = Screen.DrawerScreen.Member.route,
//        arguments = listOf(navArgument("memberId") { type = NavType.LongType })
//        ) { backStackEntry ->
//        val memberId: Long = backStackEntry.arguments?.getLong("memberId") ?: return@composable
//        WardrobeApp(
//            memberId = memberId,
//            onExit = {
//                vm.setCurrentMember(null)
//                navController.popBackStack() }
//        )
//        }
//        composable(Screen.DrawerScreen.Statistics.route) {
//            StatisticsScreen(repo = repo, navController = navController)
//        }
//        composable(Screen.DrawerScreen.Settings.route) {
//            SettingsScreen(repo = repo)
//        }
//        composable(Screen.TransferHistory.route) {
//            TransferHistoryScreen(repo = repo, navController = navController)
//        }
//        composable(Screen.ClothingInventory.route) {
//            ClothingInventoryScreen(vm = statisticsViewModel, navController = navController)
//        }
//    }
//}
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
import com.example.wardrobe.viewmodel.MainViewModel
import com.example.wardrobe.viewmodel.MemberViewModel
import com.example.wardrobe.viewmodel.StatisticsViewModel

@Composable
fun Navigation(
    repo: WardrobeRepository,
    vm : MemberViewModel,
    navController: NavController,
    viewModel: MainViewModel,
    statisticsViewModel: StatisticsViewModel,
    pd: PaddingValues,
    weather: WeatherInfo?
){
    NavHost(
        navController = navController as NavHostController,
        startDestination = Screen.DrawerScreen.Home.route,
        modifier = Modifier.padding(
            top = 64.dp,
            bottom = pd.calculateBottomPadding()
        )
    ) {
        composable(Screen.DrawerScreen.Home.route){
            Home(repo = repo, vm)
        }

        composable(
            route = Screen.DrawerScreen.Member.route,
            arguments = listOf(navArgument("memberId") { type = NavType.LongType })
        ) { backStackEntry ->
            val memberId: Long = backStackEntry.arguments?.getLong("memberId") ?: return@composable
            WardrobeApp(
                memberId = memberId,
                weather = weather,      // ← 关键传递！
                onExit = {
                    vm.setCurrentMember(null)
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.DrawerScreen.Statistics.route) {
            StatisticsScreen(repo = repo, navController = navController)
        }

        composable(Screen.DrawerScreen.Settings.route) {
            SettingsScreen(repo = repo)
        }
    }
}
