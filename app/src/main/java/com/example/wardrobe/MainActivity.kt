package com.example.wardrobe

import android.Manifest
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wardrobe.data.AppDatabase
import com.example.wardrobe.ui.theme.Theme
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.data.WeatherRepository
import com.example.wardrobe.ui.components.MainView
import com.example.wardrobe.ui.theme.WardrobeTheme
import com.example.wardrobe.viewmodel.MemberViewModel
import com.example.wardrobe.viewmodel.WardrobeViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import com.example.wardrobe.data.WeatherInfo
import com.example.wardrobe.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import androidx.core.content.edit

class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var appRepository: WardrobeRepository
    private lateinit var mainVm: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.get(this)
        appRepository = WardrobeRepository(db.clothesDao(), db.settingsRepository)
        val weatherRepo = WeatherRepository(this)

        // Factory for MemberViewModel (because it requires a custom constructor)
        val memberVmFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MemberViewModel(appRepository) as T
            }
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // --- Location permission persistence ---
        val prefs = getSharedPreferences("wardrobe_prefs", MODE_PRIVATE)

        setContent {
            var theme by remember { mutableStateOf(Theme.LIGHT) }

            mainVm = viewModel()

            // ----- Location Permission State -----
            var hasLocationPermission by remember { mutableStateOf(false) }
            var askedOnce by remember { mutableStateOf(prefs.getBoolean("askedOnce", false)) }

            // On app launch: check if the permission is already granted
            LaunchedEffect(Unit) {
                hasLocationPermission = ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            }

            // Permission request launcher
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                hasLocationPermission = granted
            }

            // Show permission request ONLY once on first launch
            // (or after clearing app data)
            LaunchedEffect(hasLocationPermission, askedOnce) {
                if (!hasLocationPermission && !askedOnce) {
                    askedOnce = true
                    prefs.edit { putBoolean("askedOnce", true) }

                    // Trigger permission dialog
                    permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            }

            // Continuously monitor whether user manually changed the permission in system settings.
            // This check runs every 5 seconds while the MainView is active.
            LaunchedEffect(Unit) {
                while (true) {
                    val granted = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    // Update state only if permission actually changed
                    if (granted != hasLocationPermission) {
                        hasLocationPermission = granted
                    }
                    delay(5000)
                }
            }

            // ------------- NFC ReaderMode Setup --------------
            LaunchedEffect(Unit) {
                enableNfcReader()
            }

            WardrobeTheme(darkTheme = theme == Theme.DARK) {
                val memberViewModel: MemberViewModel = viewModel(factory = memberVmFactory)

                MainView(
                    repo = appRepository,
                    vm = memberViewModel,
                    theme = theme,
                    onThemeChange = { theme = it },
                    weatherRepo = weatherRepo,
                    hasLocationPermission = hasLocationPermission,
                    mainVm = viewModel()
                )
            }
        }
    }

    // -------------------- NFC Reader Mode ---------------------
    private fun enableNfcReader() {
        nfcAdapter?.enableReaderMode(
            this,
            this,
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NFC_BARCODE or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onResume() {
        super.onResume()
        enableNfcReader()
    }

    // ---------------------- Tag Scan ----------------------
    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return

        val idBytes = tag.id
        val tagId = idBytes.joinToString("") { "%02X".format(it) } // 转 HEX 字符串

        // Handle tagId in MainViewModel
        mainVm.onTagScanned(tagId) { scannedId ->
            appRepository.getLocationForTag(scannedId)?.locationId
        }
    }
}

// ------------------ ViewModel factory for WardrobeViewModel ------------------

class WardrobeViewModelFactory(private val repo: WardrobeRepository, private val memberId: Long)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WardrobeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WardrobeViewModel(repo, memberId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun WardrobeApp(memberId: Long, weather: WeatherInfo?, onExit: () -> Unit) {
    val context = LocalContext.current
    val db = AppDatabase.get(context)
    val repo = WardrobeRepository(db.clothesDao(), db.settingsRepository)

    val vmFactory = WardrobeViewModelFactory(repo, memberId)
    val vm: WardrobeViewModel =
        viewModel(
            key = memberId.toString(),
            factory = vmFactory
        )

    var route by remember { mutableStateOf("home") }
    var currentClothingId by remember { mutableStateOf<Long?>(null) }

    // Custom back-button navigation logic depending on the current route
    BackHandler(enabled = true) {
        when (route) {
            "edit" -> route = if (currentClothingId != null) "detail" else "home"
            "detail" -> route = "home"
            "home" -> onExit()
        }
    }

    // Simple navigation state machine
    when (route) {
        "home" -> com.example.wardrobe.ui.HomeScreen(
            vm = vm,
            weather = weather,
            onAddClick = { currentClothingId = null; route = "edit" },
            onItemClick = { id -> currentClothingId = id; route = "detail" }
        )

        "detail" -> com.example.wardrobe.ui.ItemDetailScreen(
            vm = vm,
            itemId = currentClothingId ?: 0L,
            onBack = { route = "home" },
            onEdit = { route = "edit" }
        )

        "edit" -> com.example.wardrobe.ui.EditItemScreen(
            vm = vm,
            itemId = currentClothingId,
            onDone = { route = if (currentClothingId != null) "detail" else "home" }
        )
    }
}