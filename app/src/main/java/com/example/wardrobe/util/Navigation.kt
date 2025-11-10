package com.example.wardrobe.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
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
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.ui.Home
import com.example.wardrobe.viewmodel.MainViewModel
import com.example.wardrobe.viewmodel.MemberViewModel
import com.example.wardrobe.ui.SettingsScreen


@Composable
fun Navigation(
    repo: WardrobeRepository,
    vm : MemberViewModel,
    navController: NavController,
    viewModel: MainViewModel,
    pd: PaddingValues
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
            Home(
                repo = repo,
                vm
            )
        }
        composable(route = Screen.DrawerScreen.Member.route,
        arguments = listOf(navArgument("memberId") { type = NavType.LongType })
        ) { backStackEntry ->
        val memberId: Long = backStackEntry.arguments?.getLong("memberId") ?: return@composable
        WardrobeApp(
            memberId = memberId,
            onExit = { navController.popBackStack() }
        )
        }
        composable(Screen.DrawerScreen.Statistics.route) {
            Text("Stats")
        }
        composable(Screen.DrawerScreen.Settings.route) {
            SettingsScreen(repo = repo)
        }
    }
}