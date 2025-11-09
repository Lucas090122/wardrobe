package com.example.wardrobe.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wardrobe.Screen
import com.example.wardrobe.ScreensInDrawer
import com.example.wardrobe.data.Theme
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.data.appBarColor
import com.example.wardrobe.util.ExpandableDrawerItem
import com.example.wardrobe.util.Navigation
import com.example.wardrobe.util.SimpleDrawerItem
import com.example.wardrobe.util.ToggleDrawerItem
import com.example.wardrobe.viewmodel.MainViewModel
import com.example.wardrobe.viewmodel.MemberViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    repo: WardrobeRepository,
    vm: MemberViewModel,
){
    val members by vm.members.collectAsState()


    val viewModel : MainViewModel = viewModel()


    val controller: NavController = rememberNavController()
    val navBackStackEntry by controller.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val title = when (currentRoute) {
        Screen.DrawerScreen.Home.dRoute -> Screen.DrawerScreen.Home.dTitle
        Screen.DrawerScreen.Member.dRoute -> Screen.DrawerScreen.Member.dTitle
        Screen.DrawerScreen.Statistics.dRoute -> Screen.DrawerScreen.Statistics.dTitle
        Screen.DrawerScreen.Settings.dRoute -> Screen.DrawerScreen.Settings.dTitle
        else -> ""
    }

    val scope : CoroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var theme by remember { mutableStateOf(Theme.LIGHT) }

    val drawerContent = @Composable {
        ModalDrawerSheet (
            modifier = Modifier.padding(top = 40.dp)
        ){
            SimpleDrawerItem(
                Screen.DrawerScreen.Home,
                selected = currentRoute == Screen.DrawerScreen.Home.dRoute,
            ) {
                scope.launch {
                    drawerState.close()
                }
                controller.navigate(Screen.DrawerScreen.Home.dRoute)
            }
            ExpandableDrawerItem(
                item = Screen.DrawerScreen.Member,
                subItems = members,
                onItemClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                },
                vm = vm
            )
            LazyColumn (
            ) {
                items(ScreensInDrawer){
                    item -> SimpleDrawerItem(
                    selected = currentRoute == item.dRoute,
                    item = item,
                    ) {
                        scope.launch {
                            drawerState.close()
                        }
                        controller.navigate(item.dRoute)
                    }
                }
            }

            ToggleDrawerItem(theme) { newTheme ->
                theme = newTheme
            }
        }
    }

    ModalNavigationDrawer(
        drawerContent = drawerContent,
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomEnd = 12.dp,
                            bottomStart = 12.dp
                        )
                    ),
                    colors = appBarColor(theme),
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Open Drawer",
                                modifier = Modifier.padding(5.dp)
                            )
                        }
                    },
                    actions = {
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            IconButton(
                                onClick = {
                                    //TODO("Updates the weather using weather api")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "weather",
                                    modifier = Modifier.padding(5.dp)
                                )
                            }
                            Text("Temp")
                        }
                    }
                )
            },
            content = { innerPadding ->
                    Navigation(
                        repo = repo,
                        vm = vm,
                        controller,
                        viewModel,
                        innerPadding
                    ) // <- You control the main screen content

            }
        )
    }
}

