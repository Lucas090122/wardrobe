package com.example.wardrobe.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import com.example.wardrobe.data.ClothingItem
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import com.example.wardrobe.data.WeatherRepository
import com.example.wardrobe.data.WeatherInfo
import com.example.wardrobe.util.AiModeDrawerItem
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.example.wardrobe.R
/**
 * Simple UI model for "items stored in a location", used by NFC dialog.
 */
data class NfcLocationItemsUi(
    val locationName: String,
    val items: List<ClothingItem>
)

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
    hasLocationPermission: Boolean,
    mainVm: MainViewModel
){
    val members by vm.members.collectAsState()
    val currentMemberName by vm.currentMemberName.collectAsState()

    // Admin mode state from SettingsRepository
    val isAdminMode by repo.settings.isAdminMode.collectAsState(initial = false)
    var showAdminPinDialog by remember { mutableStateOf(false) }

    // AI mode state and privacy dialog
    val isAiEnabled by repo.settings.isAiEnabled.collectAsState(initial = false)
    var showAiPrivacyDialog by remember { mutableStateOf(false) }

    val savedPin by repo.settings.adminPin.collectAsState(initial = null)

    val statsVmFactory = StatisticsViewModelFactory(repo)
    val statisticsViewModel: StatisticsViewModel = viewModel(factory = statsVmFactory)

    // Navigation and current route
    val controller: NavController = rememberNavController()
    val navBackStackEntry by controller.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Dynamic title based on current route and current member
    val title = when (currentRoute) {
        Screen.DrawerScreen.Home.route -> {
            if (currentMemberName.isNotBlank()) {
                stringResource(R.string.wardrobe_title, currentMemberName)
            } else {
                stringResource(Screen.DrawerScreen.Home.titleId)
            }
        }
        Screen.DrawerScreen.Member.route -> {
            if (currentMemberName.isNotBlank()) {
                stringResource(R.string.wardrobe_title, currentMemberName)
            } else {
                stringResource(Screen.DrawerScreen.Member.titleId)
            }
        }
        Screen.DrawerScreen.Statistics.route ->
            stringResource(Screen.DrawerScreen.Statistics.titleId)

        Screen.DrawerScreen.Settings.route ->
            stringResource(Screen.DrawerScreen.Settings.titleId)
        else -> ""
    }

    val scope: CoroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // ---- NFC navigation: when a known tag is scanned in Idle mode ----
    val navigateLocationId by mainVm.navigateToLocationId
    var nfcLocationDialogState by remember { mutableStateOf<NfcLocationItemsUi?>(null) }

    LaunchedEffect(navigateLocationId) {
        val targetId = navigateLocationId ?: return@LaunchedEffect

        // Load location + items from repository
        val location = repo.getLocationById(targetId)
        val items = repo.getItemsByLocation(targetId)

        nfcLocationDialogState = NfcLocationItemsUi(
            locationName = location?.name ?: "Unknown location",
            items = items
        )
    }

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
    @SuppressLint("MissingPermission")
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                // Home
                SimpleDrawerItem(
                    Screen.DrawerScreen.Home,
                    selected = currentRoute == Screen.DrawerScreen.Home.route
                ) {
                    scope.launch { drawerState.close() }
                    vm.clearCurrentMember()
                    controller.navigate(Screen.DrawerScreen.Home.route)
                }

                // Members (expandable list)
                ExpandableDrawerItem(
                    item = Screen.DrawerScreen.Member,
                    subItems = members,
                    onItemClicked = { scope.launch { drawerState.close() } },
                    vm = vm,
                    controller = controller,
                )

                // Statistics, Settings
                ScreensInDrawer.forEach { item ->
                    SimpleDrawerItem(
                        selected = currentRoute == item.route,
                        item = item
                    ) {
                        scope.launch { drawerState.close() }
                        controller.navigate(item.route)
                    }
                }

//                Spacer(Modifier.height(8.dp))

                AdminModeDrawerItem(
                    isAdmin = isAdminMode,
                    onAdminChange = { enabled ->
                        if (enabled) showAdminPinDialog = true
                        else scope.launch { repo.settings.setAdminMode(false) }
                    }
                )

                AiModeDrawerItem(
                    isEnabled = isAiEnabled,
                    onToggle = { enabled ->
                        if (enabled) {
                            showAiPrivacyDialog = true
                        } else {
                            scope.launch { repo.settings.setAiEnabled(false) }
                        }
                    }
                )

                ToggleDrawerItem(
                    currentTheme = theme,
                    onThemeChange = onThemeChange
                )
            }
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
                                contentDescription = stringResource(R.string.drawer_open),
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
                                    text = stringResource(R.string.drawer_location_off),
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
                    viewModel = mainVm,
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
            val wrongPinText = stringResource(R.string.admin_pin_wrong)

            AlertDialog(
                onDismissRequest = { showAdminPinDialog = false },
                title = {
                    Text(
                        if (savedPin == null)
                            stringResource(R.string.admin_set_pin)
                        else
                            stringResource(R.string.admin_enter_pin)
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { v ->
                                pin = v.take(4)
                                pinError = null
                            },
                            label = { Text(stringResource(R.string.admin_pin_label)) },
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
//                    pinError = stringResource(R.string.admin_pin_wrong)
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
                                    pinError = wrongPinText
                                }
                            }
                        }
                    }) { Text(stringResource(R.string.admin_enter)) }
                },
                dismissButton = {
                    TextButton(onClick = { showAdminPinDialog = false }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }

        // ------------------- AI privacy notice dialog -------------------
        if (showAiPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showAiPrivacyDialog = false },
                title = { Text(stringResource(R.string.ai_title)) },
                text = { Text(stringResource(R.string.ai_description)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showAiPrivacyDialog = false
                            scope.launch { repo.settings.setAiEnabled(true) }
                        }
                    ) {
                        Text(stringResource(R.string.ai_agree))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAiPrivacyDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // ------------------- NFC location items dialog -------------------
        val nfcDialogData = nfcLocationDialogState
        if (nfcDialogData != null) {
            AlertDialog(
                onDismissRequest = {
                    nfcLocationDialogState = null
                    mainVm.clearNavigationRequest()
                },
                title = {
                    Text(stringResource(R.string.nfc_items_in, nfcDialogData.locationName))
                },
                text = {
                    if (nfcDialogData.items.isEmpty()) {
                        Text(
                            stringResource(R.string.nfc_no_items),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            nfcDialogData.items.forEach { item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Thumbnail on the left
                                    AsyncImage(
                                        model = item.imageUri?.toUri(),
                                        contentDescription = item.description,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(Modifier.width(12.dp))

                                    // Text block on the right
                                    Column {
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (!item.sizeLabel.isNullOrBlank()) {
                                            Text(
                                                text = stringResource(R.string.size_label, item.sizeLabel),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            nfcLocationDialogState = null
                            mainVm.clearNavigationRequest()
                        }
                    ) {
                        Text(stringResource(R.string.close))
                    }
                }
            )
        }
    }
}