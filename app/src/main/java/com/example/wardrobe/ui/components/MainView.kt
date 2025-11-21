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
import com.example.wardrobe.viewmodel.StatisticsViewModel
import com.example.wardrobe.viewmodel.StatisticsViewModelFactory
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
import com.example.wardrobe.util.AiModeDrawerItem

/**
 * Main top-level view that contains:
 *  - Navigation drawer (home / members / statistics / settings)
 *  - Theme toggle
 *  - Admin mode toggle with PIN
 *  - AI mode toggle with privacy consent
 *  - Weather fetching and display in the top app bar
 *
 * This composable is used as the root of the app after MainActivity.
 */
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
    val currentMemberName by vm.currentMemberName.collectAsState()

    // Admin mode state from SettingsRepository
    val isAdminMode by repo.settings.isAdminMode.collectAsState(initial = false)
    var showAdminPinDialog by remember { mutableStateOf(false) }

    // AI mode state and privacy dialog
    val isAiEnabled by repo.settings.isAiEnabled.collectAsState(initial = false)
    var showAiPrivacyDialog by remember { mutableStateOf(false) }

    val viewModel: MainViewModel = viewModel()
    val savedPin by repo.settings.adminPin.collectAsState(initial = null)

    val statsVmFactory = StatisticsViewModelFactory(repo)
    val statisticsViewModel: StatisticsViewModel = viewModel(factory = statsVmFactory)

    // Navigation and current route
    val controller: NavController = rememberNavController()
    val navBackStackEntry by controller.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Dynamic title based on current route and current member
    val title = when (currentRoute) {
        Screen.DrawerScreen.Home.dRoute -> {
            if (currentMemberName.isNotBlank()) {
                "${currentMemberName}'s Wardrobe"
            } else {
                Screen.DrawerScreen.Home.dTitle
            }
        }
        Screen.DrawerScreen.Member.dRoute -> {
            if (currentMemberName.isNotBlank()) {
                "${currentMemberName}'s Wardrobe"
            } else {
                Screen.DrawerScreen.Member.dTitle
            }
        }
        Screen.DrawerScreen.Statistics.dRoute -> Screen.DrawerScreen.Statistics.dTitle
        Screen.DrawerScreen.Settings.dRoute -> Screen.DrawerScreen.Settings.dTitle
        else -> ""
    }

    val scope: CoroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // ---- Weather state ----
    var weather by remember { mutableStateOf<WeatherInfo?>(null) }

    // If needed in future, we can re-enable manual refresh with loading/error states:
    // var weatherLoading by remember { mutableStateOf(false) }
    // var weatherError by remember { mutableStateOf<String?>(null) }

    // fun refreshWeather() { ... }

    /**
     * Fetch weather only when we have permission.
     * This will also re-trigger if permission changes from false → true.
     */
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            weather = weatherRepo.getCurrentWeather()
        }
    }

    // ------------------- Drawer content definition -------------------
    val drawerContent = @Composable {
        ModalDrawerSheet(
            modifier = Modifier
                .padding(top = 40.dp)
                .width(280.dp)
        ) {
            // Home
            SimpleDrawerItem(
                Screen.DrawerScreen.Home,
                selected = currentRoute == Screen.DrawerScreen.Home.dRoute,
            ) {
                scope.launch { drawerState.close() }
                controller.navigate(Screen.DrawerScreen.Home.dRoute)
            }

            // Members (expandable list)
            ExpandableDrawerItem(
                item = Screen.DrawerScreen.Member,
                subItems = members,
                onItemClicked = { scope.launch { drawerState.close() } },
                vm = vm,
                controller = controller,
            )

            // Other screens defined in ScreensInDrawer (Statistics, Settings, etc.)
            LazyColumn {
                items(ScreensInDrawer) { item ->
                    SimpleDrawerItem(
                        selected = currentRoute == item.dRoute,
                        item = item,
                    ) {
                        scope.launch { drawerState.close() }
                        controller.navigate(item.dRoute)
                    }
                }
            }

            // Admin mode toggle that requires PIN when enabling
            AdminModeDrawerItem(
                isAdmin = isAdminMode,
                onAdminChange = { enabled ->
                    if (enabled) {
                        // When turning ON, ask for PIN
                        showAdminPinDialog = true
                    } else {
                        // Turning OFF does not require PIN
                        scope.launch { repo.settings.setAdminMode(false) }
                    }
                }
            )

            // AI mode toggle with privacy dialog before enabling
            AiModeDrawerItem(
                isEnabled = isAiEnabled,
                onToggle = { enabled ->
                    if (enabled) {
                        // Do not enable AI immediately; first show privacy notice
                        showAiPrivacyDialog = true
                    } else {
                        scope.launch { repo.settings.setAiEnabled(false) }
                    }
                }
            )

            // Theme toggle (light / dark / system, depending on your implementation)
            ToggleDrawerItem(currentTheme = theme) { newTheme -> onThemeChange(newTheme) }
        }
    }

    // ------------------- Main Scaffold with top app bar & navigation -------------------
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
                                // When location is off, just show a passive text hint.
                                // We do not pop up dialogs to avoid annoying the user.
                                Text(
                                    text = "Location off",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                // Manual refresh + error states can be re-enabled later if needed.
                                // IconButton(onClick = { refreshWeather() }) { ... }

                                val tempText =
                                    if (weather != null) "${weather!!.temperature.toInt()}°" else "N/A"
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
                    statisticsViewModel = statisticsViewModel,
                    pd = innerPadding,
                    weather = weather
                )
            }
        )

        // ------------------- Admin PIN dialog -------------------
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
                            Text(
                                text = pinError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            when {
                                // No PIN set yet → this becomes the initial PIN and enables admin mode.
                                savedPin == null -> {
                                    repo.settings.setAdminPin(pin)
                                    repo.settings.setAdminMode(true)
                                    showAdminPinDialog = false
                                }
                                // Existing PIN → check input before enabling admin mode.
                                pin == savedPin -> {
                                    repo.settings.setAdminMode(true)
                                    showAdminPinDialog = false
                                }
                                else -> {
                                    pinError = "Wrong PIN"
                                }
                            }
                        }
                    }) { Text("Enter") }
                },
                dismissButton = {
                    TextButton(onClick = { showAdminPinDialog = false }) { Text("Cancel") }
                }
            )
        }

        // ------------------- AI privacy notice dialog -------------------
        if (showAiPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showAiPrivacyDialog = false },
                title = { Text("Enable AI mode?") },
                text = {
                    Text(
                        "When AI mode is enabled, your clothing photos will be sent to Google's Gemini service " +
                                "for analysis in order to auto-fill item details. " +
                                "Please only upload photos you are comfortable sharing."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showAiPrivacyDialog = false
                            scope.launch { repo.settings.setAiEnabled(true) }
                        }
                    ) {
                        Text("Agree and enable")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAiPrivacyDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}