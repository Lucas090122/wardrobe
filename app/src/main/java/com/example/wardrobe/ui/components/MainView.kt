//package com.example.wardrobe.ui.components
//
//import android.annotation.SuppressLint
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Menu
//import androidx.compose.material.icons.filled.Warning
//import androidx.compose.material3.DrawerValue
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.ModalDrawerSheet
//import androidx.compose.material3.ModalNavigationDrawer
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.rememberDrawerState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.navigation.compose.rememberNavController
//import com.example.wardrobe.Screen
//import com.example.wardrobe.ScreensInDrawer
//import com.example.wardrobe.ui.theme.Theme
//import com.example.wardrobe.data.WardrobeRepository
//import com.example.wardrobe.ui.theme.appBarColor
//import com.example.wardrobe.util.AdminModeDrawerItem
//import com.example.wardrobe.util.ExpandableDrawerItem
//import com.example.wardrobe.util.Navigation
//import com.example.wardrobe.util.SimpleDrawerItem
//import com.example.wardrobe.util.ToggleDrawerItem
//import com.example.wardrobe.viewmodel.MainViewModel
//import com.example.wardrobe.viewmodel.MemberViewModel
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.launch
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.height
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.runtime.setValue
//
//
//@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MainView(
//    repo: WardrobeRepository,
//    vm: MemberViewModel,
//    theme: Theme,
//    onThemeChange: (Theme) -> Unit
//){
//    val members by vm.members.collectAsState()
//    val isAdminMode by repo.settings.isAdminMode.collectAsState(initial = false)
//    var showAdminPinDialog by remember { mutableStateOf(false) }
//
//    val viewModel : MainViewModel = viewModel()
//    val savedPin by repo.settings.adminPin.collectAsState(initial = null)
//
//    val controller: NavController = rememberNavController()
//    val navBackStackEntry by controller.currentBackStackEntryAsState()
//    val currentRoute = navBackStackEntry?.destination?.route
//
//    val title = when (currentRoute) {
//        Screen.DrawerScreen.Home.dRoute -> Screen.DrawerScreen.Home.dTitle
//        Screen.DrawerScreen.Member.dRoute -> Screen.DrawerScreen.Member.dTitle
//        Screen.DrawerScreen.Statistics.dRoute -> Screen.DrawerScreen.Statistics.dTitle
//        Screen.DrawerScreen.Settings.dRoute -> Screen.DrawerScreen.Settings.dTitle
//        else -> ""
//    }
//
//    val scope : CoroutineScope = rememberCoroutineScope()
//    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
//
//    val drawerContent = @Composable {
//        ModalDrawerSheet (
//            modifier = Modifier
//                .padding(top = 40.dp)
//                .width(280.dp)
//        ){
//            SimpleDrawerItem(
//                Screen.DrawerScreen.Home,
//                selected = currentRoute == Screen.DrawerScreen.Home.dRoute,
//            ) {
//                scope.launch {
//                    drawerState.close()
//                }
//                controller.navigate(Screen.DrawerScreen.Home.dRoute)
//            }
//            ExpandableDrawerItem(
//                item = Screen.DrawerScreen.Member,
//                subItems = members,
//                onItemClicked = {
//                    scope.launch {
//                        drawerState.close()
//                    }
//                },
//                vm = vm,
//                controller,
//            )
//            LazyColumn (
//            ) {
//                items(ScreensInDrawer){
//                    item -> SimpleDrawerItem(
//                    selected = currentRoute == item.dRoute,
//                    item = item,
//                    ) {
//                        scope.launch {
//                            drawerState.close()
//                        }
//                        controller.navigate(item.dRoute)
//                    }
//                }
//            }
//
//            AdminModeDrawerItem(
//                isAdmin = isAdminMode,
//                onAdminChange = { enabled ->
//                    if (enabled) {
//                        showAdminPinDialog = true
//                    } else {
//                        scope.launch {
//                            repo.settings.setAdminMode(false)
//                        }
//                    }
//                }
//            )
//
//            ToggleDrawerItem(currentTheme = theme) { newTheme ->
//                onThemeChange(newTheme)
//            }
//        }
//    }
//
//    ModalNavigationDrawer(
//        drawerContent = drawerContent,
//        drawerState = drawerState
//    ) {
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    modifier = Modifier.clip(
//                        RoundedCornerShape(
//                            topStart = 0.dp,
//                            topEnd = 0.dp,
//                            bottomEnd = 12.dp,
//                            bottomStart = 12.dp
//                        )
//                    ),
//                    colors = appBarColor(theme),
//                    title = { Text(title) },
//                    navigationIcon = {
//                        IconButton(
//                            onClick = {
//                                scope.launch {
//                                    if (drawerState.isClosed) drawerState.open()
//                                    else drawerState.close()
//                                }
//                            }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Menu,
//                                contentDescription = "Open Drawer",
//                                modifier = Modifier.padding(5.dp)
//                            )
//                        }
//                    },
//                    actions = {
//                        Row (
//                            verticalAlignment = Alignment.CenterVertically
//                        ){
//                            IconButton(
//                                onClick = {
//                                    //TODO("Updates the weather using weather api")
//                                }
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Warning,
//                                    contentDescription = "weather",
//                                    modifier = Modifier.padding(5.dp)
//                                )
//                            }
//                            Text("Temp")
//                        }
//                    }
//                )
//            },
//            content = { innerPadding ->
//                    Navigation(
//                        repo = repo,
//                        vm = vm,
//                        controller,
//                        viewModel,
//                        innerPadding
//                    ) // <- You control the main screen content
//
//            }
//        )
//
//        if (showAdminPinDialog) {
//            var pin by remember { mutableStateOf("") }
//            var pinError by remember { mutableStateOf<String?>(null) }
//
//            AlertDialog(
//                onDismissRequest = { showAdminPinDialog = false },
//                title = { Text(if (savedPin == null) "Set Admin PIN" else "Enter Admin PIN") },
//                text = {
//                    Column {
//                        OutlinedTextField(
//                            value = pin,
//                            onValueChange = { v ->
//                                pin = v.take(4)
//                                pinError = null
//                            },
//                            label = { Text("4-digit PIN") },
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
//                            visualTransformation = PasswordVisualTransformation()
//                        )
//                        if (pinError != null) {
//                            Spacer(Modifier.height(8.dp))
//                            Text(
//                                text = pinError!!,
//                                color = MaterialTheme.colorScheme.error,
//                                style = MaterialTheme.typography.bodySmall
//                            )
//                        }
//                    }
//                },
//                confirmButton = {
//                    TextButton(onClick = {
//                        scope.launch {
//                            if (savedPin == null) {
//                                // First time setting PIN
//                                repo.settings.setAdminPin(pin)
//                                repo.settings.setAdminMode(true)
//                                showAdminPinDialog = false
//                            } else if (pin == savedPin) {
//                                // Correct PIN
//                                repo.settings.setAdminMode(true)
//                                showAdminPinDialog = false
//                            } else {
//                                pinError = "Wrong PIN"
//                            }
//                        }
//                    }) {
//                        Text("Enter")
//                    }
//                },
//                dismissButton = {
//                    TextButton(onClick = { showAdminPinDialog = false }) {
//                        Text("Cancel")
//                    }
//                }
//            )
//        }
//    }
//}
//
//1----------
//package com.example.wardrobe.ui.components
//
//import android.annotation.SuppressLint
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Menu
//import androidx.compose.material.icons.filled.Refresh
//import androidx.compose.material3.DrawerValue
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.ModalDrawerSheet
//import androidx.compose.material3.ModalNavigationDrawer
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.rememberDrawerState
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.navigation.compose.rememberNavController
//import com.example.wardrobe.Screen
//import com.example.wardrobe.ScreensInDrawer
//import com.example.wardrobe.ui.theme.Theme
//import com.example.wardrobe.data.WardrobeRepository
//import com.example.wardrobe.ui.theme.appBarColor
//import com.example.wardrobe.util.AdminModeDrawerItem
//import com.example.wardrobe.util.ExpandableDrawerItem
//import com.example.wardrobe.util.Navigation
//import com.example.wardrobe.util.SimpleDrawerItem
//import com.example.wardrobe.util.ToggleDrawerItem
//import com.example.wardrobe.viewmodel.MainViewModel
//import com.example.wardrobe.viewmodel.MemberViewModel
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.launch
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.height
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.foundation.text.KeyboardOptions
//import com.example.wardrobe.data.WeatherRepository
//import com.example.wardrobe.data.WeatherInfo
//
//@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MainView(
//    repo: WardrobeRepository,
//    vm: MemberViewModel,
//    theme: Theme,
//    onThemeChange: (Theme) -> Unit,
//    weatherRepo: WeatherRepository // 新增
//){
//    val members by vm.members.collectAsState()
//    val isAdminMode by repo.settings.isAdminMode.collectAsState(initial = false)
//    var showAdminPinDialog by remember { mutableStateOf(false) }
//
//    val viewModel : MainViewModel = viewModel()
//    val savedPin by repo.settings.adminPin.collectAsState(initial = null)
//
//    val controller: NavController = rememberNavController()
//    val navBackStackEntry by controller.currentBackStackEntryAsState()
//    val currentRoute = navBackStackEntry?.destination?.route
//
//    val title = when (currentRoute) {
//        Screen.DrawerScreen.Home.dRoute -> Screen.DrawerScreen.Home.dTitle
//        Screen.DrawerScreen.Member.dRoute -> Screen.DrawerScreen.Member.dTitle
//        Screen.DrawerScreen.Statistics.dRoute -> Screen.DrawerScreen.Statistics.dTitle
//        Screen.DrawerScreen.Settings.dRoute -> Screen.DrawerScreen.Settings.dTitle
//        else -> ""
//    }
//
//    val scope : CoroutineScope = rememberCoroutineScope()
//    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
//
//    // ------------------ 天气状态与加载逻辑（新增） ------------------
//    var weather by remember { mutableStateOf<WeatherInfo?>(null) }
//    var weatherLoading by remember { mutableStateOf(false) }
//    var weatherError by remember { mutableStateOf<String?>(null) }
//
//    fun refreshWeather() {
//        scope.launch {
//            try {
//                weatherLoading = true
//                weatherError = null
//                weather = weatherRepo.getCurrentWeather()
//                if (weather == null) weatherError = "N/A"
//            } catch (e: Exception) {
//                weatherError = "N/A"
//            } finally {
//                weatherLoading = false
//            }
//        }
//    }
//
//    // 首次进入页面自动拉取一次
//    LaunchedEffect(Unit) { refreshWeather() }
//    // -----------------------------------------------------------
//
//    val drawerContent = @Composable {
//        ModalDrawerSheet (
//            modifier = Modifier
//                .padding(top = 40.dp)
//                .width(280.dp)
//        ){
//            SimpleDrawerItem(
//                Screen.DrawerScreen.Home,
//                selected = currentRoute == Screen.DrawerScreen.Home.dRoute,
//            ) {
//                scope.launch {
//                    drawerState.close()
//                }
//                controller.navigate(Screen.DrawerScreen.Home.dRoute)
//            }
//            ExpandableDrawerItem(
//                item = Screen.DrawerScreen.Member,
//                subItems = members,
//                onItemClicked = {
//                    scope.launch {
//                        drawerState.close()
//                    }
//                },
//                vm = vm,
//                controller,
//            )
//            LazyColumn {
//                items(ScreensInDrawer){
//                        item -> SimpleDrawerItem(
//                    selected = currentRoute == item.dRoute,
//                    item = item,
//                ) {
//                    scope.launch {
//                        drawerState.close()
//                    }
//                    controller.navigate(item.dRoute)
//                }
//                }
//            }
//
//            AdminModeDrawerItem(
//                isAdmin = isAdminMode,
//                onAdminChange = { enabled ->
//                    if (enabled) {
//                        showAdminPinDialog = true
//                    } else {
//                        scope.launch {
//                            repo.settings.setAdminMode(false)
//                        }
//                    }
//                }
//            )
//
//            ToggleDrawerItem(currentTheme = theme) { newTheme ->
//                onThemeChange(newTheme)
//            }
//        }
//    }
//
//    ModalNavigationDrawer(
//        drawerContent = drawerContent,
//        drawerState = drawerState
//    ) {
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    modifier = Modifier.clip(
//                        RoundedCornerShape(
//                            topStart = 0.dp,
//                            topEnd = 0.dp,
//                            bottomEnd = 12.dp,
//                            bottomStart = 12.dp
//                        )
//                    ),
//                    colors = appBarColor(theme),
//                    title = { Text(title) },
//                    navigationIcon = {
//                        IconButton(
//                            onClick = {
//                                scope.launch {
//                                    if (drawerState.isClosed) drawerState.open()
//                                    else drawerState.close()
//                                }
//                            }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Menu,
//                                contentDescription = "Open Drawer",
//                                modifier = Modifier.padding(5.dp)
//                            )
//                        }
//                    },
//                    actions = {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            // 刷新按钮
//                            IconButton(onClick = { refreshWeather() }) {
//                                Icon(
//                                    imageVector = Icons.Default.Refresh,
//                                    contentDescription = "Refresh weather",
//                                    modifier = Modifier.padding(5.dp)
//                                )
//                            }
//                            // 显示天气：图标 + 温度
//                            val tempText = when {
//                                weatherLoading -> "..."
//                                weatherError != null -> "N/A"
//                                weather != null -> "${weather!!.temperature.toInt()}°"
//                                else -> "N/A"
//                            }
//                            Text(
//                                text = "${weather?.icon ?: ""} $tempText".trim()
//                            )
//                        }
//                    }
//                )
//            },
//            content = { innerPadding ->
//                Navigation(
//                    repo = repo,
//                    vm = vm,
//                    navController = controller,
//                    viewModel = viewModel,
//                    pd = innerPadding
//                )
//            }
//        )
//
//        if (showAdminPinDialog) {
//            var pin by remember { mutableStateOf("") }
//            var pinError by remember { mutableStateOf<String?>(null) }
//
//            AlertDialog(
//                onDismissRequest = { showAdminPinDialog = false },
//                title = { Text(if (savedPin == null) "Set Admin PIN" else "Enter Admin PIN") },
//                text = {
//                    Column {
//                        OutlinedTextField(
//                            value = pin,
//                            onValueChange = { v ->
//                                pin = v.take(4)
//                                pinError = null
//                            },
//                            label = { Text("4-digit PIN") },
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
//                            visualTransformation = PasswordVisualTransformation()
//                        )
//                        if (pinError != null) {
//                            Spacer(Modifier.height(8.dp))
//                            Text(
//                                text = pinError!!,
//                                color = MaterialTheme.colorScheme.error
//                            )
//                        }
//                    }
//                },
//                confirmButton = {
//                    TextButton(onClick = {
//                        scope.launch {
//                            if (savedPin == null) {
//                                repo.settings.setAdminPin(pin)
//                                repo.settings.setAdminMode(true)
//                                showAdminPinDialog = false
//                            } else if (pin == savedPin) {
//                                repo.settings.setAdminMode(true)
//                                showAdminPinDialog = false
//                            } else {
//                                pinError = "Wrong PIN"
//                            }
//                        }
//                    }) {
//                        Text("Enter")
//                    }
//                },
//                dismissButton = {
//                    TextButton(onClick = { showAdminPinDialog = false }) {
//                        Text("Cancel")
//                    }
//                }
//            )
//        }
//    }
//}
//2---------------------
package com.example.wardrobe.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
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
import com.example.wardrobe.ui.theme.Theme
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.ui.theme.appBarColor
import com.example.wardrobe.util.AdminModeDrawerItem
import com.example.wardrobe.util.ExpandableDrawerItem
import com.example.wardrobe.util.Navigation
import com.example.wardrobe.util.SimpleDrawerItem
import com.example.wardrobe.util.ToggleDrawerItem
import com.example.wardrobe.viewmodel.MainViewModel
import com.example.wardrobe.viewmodel.MemberViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import com.example.wardrobe.data.WeatherRepository
import com.example.wardrobe.data.WeatherInfo

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    repo: WardrobeRepository,
    vm: MemberViewModel,
    theme: Theme,
    onThemeChange: (Theme) -> Unit,
    weatherRepo: WeatherRepository,
    hasLocationPermission: Boolean
){
    val members by vm.members.collectAsState()
    val isAdminMode by repo.settings.isAdminMode.collectAsState(initial = false)
    var showAdminPinDialog by remember { mutableStateOf(false) }

    val viewModel : MainViewModel = viewModel()
    val savedPin by repo.settings.adminPin.collectAsState(initial = null)

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

    // ---- 天气状态 ----
    var weather by remember { mutableStateOf<WeatherInfo?>(null) }
    var weatherLoading by remember { mutableStateOf(false) }
    var weatherError by remember { mutableStateOf<String?>(null) }

    fun refreshWeather() {
        scope.launch {
            try {
                weatherLoading = true
                weatherError = null
                weather = weatherRepo.getCurrentWeather()
                if (weather == null) weatherError = "N/A"
            } catch (e: Exception) {
                weatherError = "N/A"
            } finally {
                weatherLoading = false
            }
        }
    }

    // 仅在有权限时加载；权限从 false→true 时也会触发
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) refreshWeather()
    }
    // -------------------

    val drawerContent = @Composable {
        ModalDrawerSheet (
            modifier = Modifier
                .padding(top = 40.dp)
                .width(280.dp)
        ){
            SimpleDrawerItem(
                Screen.DrawerScreen.Home,
                selected = currentRoute == Screen.DrawerScreen.Home.dRoute,
            ) {
                scope.launch { drawerState.close() }
                controller.navigate(Screen.DrawerScreen.Home.dRoute)
            }
            ExpandableDrawerItem(
                item = Screen.DrawerScreen.Member,
                subItems = members,
                onItemClicked = { scope.launch { drawerState.close() } },
                vm = vm,
                controller,
            )
            LazyColumn {
                items(ScreensInDrawer){ item ->
                    SimpleDrawerItem(
                        selected = currentRoute == item.dRoute,
                        item = item,
                    ) {
                        scope.launch { drawerState.close() }
                        controller.navigate(item.dRoute)
                    }
                }
            }

            AdminModeDrawerItem(
                isAdmin = isAdminMode,
                onAdminChange = { enabled ->
                    if (enabled) {
                        showAdminPinDialog = true
                    } else {
                        scope.launch { repo.settings.setAdminMode(false) }
                    }
                }
            )

            ToggleDrawerItem(currentTheme = theme) { newTheme -> onThemeChange(newTheme) }
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
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Drawer",
                                modifier = Modifier.padding(5.dp)
                            )
                        }
                    },
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!hasLocationPermission) {
                                // 无权限时只显示提示，不弹窗打扰用户
                                Text(
                                    text = "Location off",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                IconButton(onClick = { refreshWeather() }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh weather",
                                        modifier = Modifier.padding(5.dp)
                                    )
                                }
                                val tempText = when {
                                    weatherLoading -> "..."
                                    weatherError != null -> "N/A"
                                    weather != null -> "${weather!!.temperature.toInt()}°"
                                    else -> "N/A"
                                }
                                Text(text = "${weather?.icon ?: ""} $tempText".trim())
                            }
                        }
                    }
                )
            },
            content = { innerPadding ->
                Navigation(
                    repo = repo,
                    vm = vm,
                    navController = controller,
                    viewModel = viewModel,
                    pd = innerPadding
                )
            }
        )

        if (showAdminPinDialog) {
            var pin by remember { mutableStateOf("") }
            var pinError by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showAdminPinDialog = false },
                title = { Text(if (savedPin == null) "Set Admin PIN" else "Enter Admin PIN") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { v ->
                                pin = v.take(4)
                                pinError = null
                            },
                            label = { Text("4-digit PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation()
                        )
                        if (pinError != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(text = pinError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            if (savedPin == null) {
                                repo.settings.setAdminPin(pin)
                                repo.settings.setAdminMode(true)
                                showAdminPinDialog = false
                            } else if (pin == savedPin) {
                                repo.settings.setAdminMode(true)
                                showAdminPinDialog = false
                            } else {
                                pinError = "Wrong PIN"
                            }
                        }
                    }) { Text("Enter") }
                },
                dismissButton = {
                    TextButton(onClick = { showAdminPinDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
