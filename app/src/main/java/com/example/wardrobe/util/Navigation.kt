package com.example.wardrobe.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wardrobe.Screen
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.ui.Home
import com.example.wardrobe.viewmodel.MainViewModel
import com.example.wardrobe.viewmodel.MemberViewModel


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
        modifier = Modifier.padding(pd)
    ) {
        composable(Screen.DrawerScreen.Home.route){
            Home(
                repo = repo,
                vm
            )
        }
        composable(Screen.DrawerScreen.Member.route){
            Text("Members")
        }
        composable(Screen.DrawerScreen.Statistics.route) {
            Text("Stats")
        }
        composable(Screen.DrawerScreen.Settings.route) {
            Text("Settings")
        }
    }
}