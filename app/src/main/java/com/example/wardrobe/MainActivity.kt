//package com.example.wardrobe
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.runtime.*
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.wardrobe.data.AppDatabase
//import com.example.wardrobe.data.WardrobeRepository
//import com.example.wardrobe.ui.EditItemScreen
//import com.example.wardrobe.ui.HomeScreen
//import com.example.wardrobe.ui.ItemDetailScreen
//import com.example.wardrobe.viewmodel.MemberViewModel
//import com.example.wardrobe.viewmodel.WardrobeViewModel
//import com.example.wardrobe.ui.theme.WardrobeTheme
//import androidx.activity.compose.BackHandler
//import androidx.compose.ui.platform.LocalContext
//import com.example.wardrobe.ui.components.MainView
//import com.example.wardrobe.ui.theme.Theme
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val db = AppDatabase.get(this)
//        val repo = WardrobeRepository(db.clothesDao(), db.settingsRepository)
//
//
//        val memberVmFactory = object : ViewModelProvider.Factory {
//            override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                @Suppress("UNCHECKED_CAST")
//                return MemberViewModel(repo) as T
//            }
//        }
//
//
//        setContent {
//            var theme by remember { mutableStateOf(Theme.LIGHT) }
//
//            WardrobeTheme(darkTheme = theme == Theme.DARK) {
//                val memberViewModel: MemberViewModel = viewModel(factory = memberVmFactory)
//                MainView(
//                    repo = repo,
//                    vm = memberViewModel,
//                    theme = theme,
//                    onThemeChange = { theme = it }
//                )
//
//                /*if (selectedMemberId == null) {
//                    // This is the root screen, handle app exit here
//                    var lastBackTime by remember { mutableLongStateOf(0L) }
//                    val context = LocalContext.current
//                    BackHandler(enabled = true) {
//                        val now = System.currentTimeMillis()
//                        if (now - lastBackTime < 2000) {
//                            finish() // Exit the app
//                        } else {
//                            lastBackTime = now
//                            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                    val memberViewModel: MemberViewModel = viewModel(factory = memberVmFactory)
//                    MemberSelectionScreen(
//                        vm = memberViewModel,
//                        onMemberSelected = { memberId ->
//                            selectedMemberId = memberId
//                        }
//                    )
//                } else {
//                    WardrobeApp(memberId = selectedMemberId!!) {
//                        selectedMemberId = null // On back pressed from app, go back to member selection
//                    }
//                }*/
//            }
//        }
//    }
//}
//
//// A factory for creating WardrobeViewModel instances for a specific member
//class WardrobeViewModelFactory(private val repo: WardrobeRepository, private val memberId: Long) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(WardrobeViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return WardrobeViewModel(repo, memberId) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
//
//@Composable
//fun WardrobeApp(memberId: Long, onExit: () -> Unit) {
//    val context = LocalContext.current
//    val db = AppDatabase.get(context)
//    val repo = WardrobeRepository(db.clothesDao(), db.settingsRepository)
//    val vmFactory = WardrobeViewModelFactory(repo, memberId)
//    val vm: WardrobeViewModel = viewModel(key = memberId.toString(), factory = vmFactory)
//
//    var route by remember { mutableStateOf("home") } // home / detail / edit
//    var currentClothingId by remember { mutableStateOf<Long?>(null) }
//
//    BackHandler(enabled = true) {
//        when (route) {
//            "edit" -> {
//                route = if (currentClothingId != null) "detail" else "home"
//            }
//            "detail" -> {
//                route = "home"
//            }
//            "home" -> {
//                // Single press back to go to member selection
//                onExit()
//            }
//        }
//    }
//
//    when (route) {
//        "home" -> {
//            HomeScreen(
//                vm = vm,
//                onAddClick = { currentClothingId = null; route = "edit" },
//                onItemClick = { id -> currentClothingId = id; route = "detail" }
//            )
//        }
//        "detail" -> ItemDetailScreen(
//            vm = vm,
//            itemId = currentClothingId ?: 0L,
//            onBack = { route = "home" },
//            onEdit = { route = "edit" }
//        )
//        "edit" -> EditItemScreen(
//            vm = vm,
//            itemId = currentClothingId,
//            onDone = {
//                route = if (currentClothingId != null) "detail" else "home"
//            }
//        )
//    }
//}
//1------------
//package com.example.wardrobe
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.runtime.*
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.wardrobe.data.AppDatabase
//import com.example.wardrobe.data.WardrobeRepository
//import com.example.wardrobe.ui.theme.WardrobeTheme
//import androidx.activity.compose.BackHandler
//import com.example.wardrobe.ui.components.MainView
//import com.example.wardrobe.ui.theme.Theme
//import com.example.wardrobe.viewmodel.MemberViewModel
//import com.example.wardrobe.viewmodel.WardrobeViewModel
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.platform.LocalContext
//import com.example.wardrobe.data.WeatherRepository
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val db = AppDatabase.get(this)
//        val repo = WardrobeRepository(db.clothesDao(), db.settingsRepository)
//        val weatherRepo = WeatherRepository(this) // 新增
//
//        val memberVmFactory = object : ViewModelProvider.Factory {
//            override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                @Suppress("UNCHECKED_CAST")
//                return MemberViewModel(repo) as T
//            }
//        }
//
//        setContent {
//            var theme by remember { mutableStateOf(Theme.LIGHT) }
//
//            WardrobeTheme(darkTheme = theme == Theme.DARK) {
//                val memberViewModel: MemberViewModel = viewModel(factory = memberVmFactory)
//                MainView(
//                    repo = repo,
//                    vm = memberViewModel,
//                    theme = theme,
//                    onThemeChange = { theme = it },
//                    weatherRepo = weatherRepo // 新增
//                )
//            }
//        }
//    }
//}
//
//// A factory for creating WardrobeViewModel instances for a specific member
//class WardrobeViewModelFactory(private val repo: WardrobeRepository, private val memberId: Long) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(WardrobeViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return WardrobeViewModel(repo, memberId) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
//
//@Composable
//fun WardrobeApp(memberId: Long, onExit: () -> Unit) {
//    val context = LocalContext.current
//    val db = AppDatabase.get(context)
//    val repo = WardrobeRepository(db.clothesDao(), db.settingsRepository)
//    val vmFactory = WardrobeViewModelFactory(repo, memberId)
//    val vm: WardrobeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(key = memberId.toString(), factory = vmFactory)
//
//    var route by remember { mutableStateOf("home") } // home / detail / edit
//    var currentClothingId by remember { mutableStateOf<Long?>(null) }
//
//    BackHandler(enabled = true) {
//        when (route) {
//            "edit" -> {
//                route = if (currentClothingId != null) "detail" else "home"
//            }
//            "detail" -> {
//                route = "home"
//            }
//            "home" -> {
//                onExit()
//            }
//        }
//    }
//
//    when (route) {
//        "home" -> {
//            com.example.wardrobe.ui.HomeScreen(
//                vm = vm,
//                onAddClick = { currentClothingId = null; route = "edit" },
//                onItemClick = { id -> currentClothingId = id; route = "detail" }
//            )
//        }
//        "detail" -> com.example.wardrobe.ui.ItemDetailScreen(
//            vm = vm,
//            itemId = currentClothingId ?: 0L,
//            onBack = { route = "home" },
//            onEdit = { route = "edit" }
//        )
//        "edit" -> com.example.wardrobe.ui.EditItemScreen(
//            vm = vm,
//            itemId = currentClothingId,
//            onDone = {
//                route = if (currentClothingId != null) "detail" else "home"
//            }
//        )
//    }
//}
//2-------------
//package com.example.wardrobe
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.runtime.*
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.wardrobe.data.AppDatabase
//import com.example.wardrobe.ui.theme.Theme
//import com.example.wardrobe.data.WardrobeRepository
//import com.example.wardrobe.data.WeatherRepository
//import com.example.wardrobe.ui.components.MainView
//import com.example.wardrobe.ui.theme.WardrobeTheme
//import com.example.wardrobe.viewmodel.MemberViewModel
//import com.example.wardrobe.viewmodel.WardrobeViewModel
//import androidx.activity.compose.BackHandler
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.compose.ui.platform.LocalContext
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val db = AppDatabase.get(this)
//        val repo = WardrobeRepository(db.clothesDao(), db.settingsRepository)
//        val weatherRepo = WeatherRepository(this)
//
//        val memberVmFactory = object : ViewModelProvider.Factory {
//            override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                @Suppress("UNCHECKED_CAST")
//                return MemberViewModel(repo) as T
//            }
//        }
//
//        setContent {
//            var theme by remember { mutableStateOf(Theme.LIGHT) }
//
//            // ---- 位置权限状态（只弹一次） ----
//            var hasLocationPermission by remember { mutableStateOf(false) }
//            var askedOnce by remember { mutableStateOf(false) }
//
//            LaunchedEffect(Unit) {
//                hasLocationPermission = ContextCompat.checkSelfPermission(
//                    this@MainActivity,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//            }
//            val permissionLauncher = rememberLauncherForActivityResult(
//                contract = ActivityResultContracts.RequestPermission()
//            ) { granted ->
//                hasLocationPermission = granted
//            }
//            LaunchedEffect(hasLocationPermission, askedOnce) {
//                if (!hasLocationPermission && !askedOnce) {
//                    askedOnce = true
//                    permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
//                }
//            }
//            // --------------------------------
//
//            WardrobeTheme(darkTheme = theme == Theme.DARK) {
//                val memberViewModel: MemberViewModel = viewModel(factory = memberVmFactory)
//                MainView(
//                    repo = repo,
//                    vm = memberViewModel,
//                    theme = theme,
//                    onThemeChange = { theme = it },
//                    weatherRepo = weatherRepo,
//                    hasLocationPermission = hasLocationPermission
//                )
//            }
//        }
//    }
//}
//
//// A factory for creating WardrobeViewModel instances for a specific member
//class WardrobeViewModelFactory(private val repo: WardrobeRepository, private val memberId: Long) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(WardrobeViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return WardrobeViewModel(repo, memberId) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
//
//@Composable
//fun WardrobeApp(memberId: Long, onExit: () -> Unit) {
//    val context = LocalContext.current
//    val db = AppDatabase.get(context)
//    val repo = WardrobeRepository(db.clothesDao(), db.settingsRepository)
//    val vmFactory = WardrobeViewModelFactory(repo, memberId)
//    val vm: WardrobeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(key = memberId.toString(), factory = vmFactory)
//
//    var route by remember { mutableStateOf("home") } // home / detail / edit
//    var currentClothingId by remember { mutableStateOf<Long?>(null) }
//
//    BackHandler(enabled = true) {
//        when (route) {
//            "edit" -> route = if (currentClothingId != null) "detail" else "home"
//            "detail" -> route = "home"
//            "home" -> onExit()
//        }
//    }
//
//    when (route) {
//        "home" -> {
//            com.example.wardrobe.ui.HomeScreen(
//                vm = vm,
//                onAddClick = { currentClothingId = null; route = "edit" },
//                onItemClick = { id -> currentClothingId = id; route = "detail" }
//            )
//        }
//        "detail" -> com.example.wardrobe.ui.ItemDetailScreen(
//            vm = vm,
//            itemId = currentClothingId ?: 0L,
//            onBack = { route = "home" },
//            onEdit = { route = "edit" }
//        )
//        "edit" -> com.example.wardrobe.ui.EditItemScreen(
//            vm = vm,
//            itemId = currentClothingId,
//            onDone = { route = if (currentClothingId != null) "detail" else "home" }
//        )
//    }
//}
//3------------------
//package com.example.wardrobe
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.runtime.*
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.wardrobe.data.AppDatabase
//import com.example.wardrobe.ui.theme.Theme
//import com.example.wardrobe.data.WardrobeRepository
//import com.example.wardrobe.data.WeatherRepository
//import com.example.wardrobe.ui.components.MainView
//import com.example.wardrobe.ui.theme.WardrobeTheme
//import com.example.wardrobe.viewmodel.MemberViewModel
//import com.example.wardrobe.viewmodel.WardrobeViewModel
//import androidx.activity.compose.BackHandler
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.compose.ui.platform.LocalContext
//import kotlinx.coroutines.delay
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val db = AppDatabase.get(this)
//        val repo = WardrobeRepository(db.clothesDao(), db.settingsRepository)
//        val weatherRepo = WeatherRepository(this)
//
//        val memberVmFactory = object : ViewModelProvider.Factory {
//            override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                @Suppress("UNCHECKED_CAST")
//                return MemberViewModel(repo) as T
//            }
//        }
//
//        setContent {
//            var theme by remember { mutableStateOf(Theme.LIGHT) }
//
//            // ---- 定位权限状态 ----
//            var hasLocationPermission by remember { mutableStateOf(false) }
//            var askedOnce by remember { mutableStateOf(false) }
//
//            // 启动时立即检查权限
//            LaunchedEffect(Unit) {
//                hasLocationPermission = ContextCompat.checkSelfPermission(
//                    this@MainActivity,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED
//            }
//
//            // 权限请求 Launcher
//            val permissionLauncher = rememberLauncherForActivityResult(
//                contract = ActivityResultContracts.RequestPermission()
//            ) { granted ->
//                hasLocationPermission = granted
//            }
//
//            // 仅首次启动时弹窗
//            LaunchedEffect(hasLocationPermission, askedOnce) {
//                if (!hasLocationPermission && !askedOnce) {
//                    askedOnce = true
//                    permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
//                }
//            }
//
//            // ✅ 持续监听用户手动更改权限（每 5 秒检查一次）
//            LaunchedEffect(Unit) {
//                while (true) {
//                    val granted = ContextCompat.checkSelfPermission(
//                        this@MainActivity,
//                        Manifest.permission.ACCESS_COARSE_LOCATION
//                    ) == PackageManager.PERMISSION_GRANTED
//                    if (granted != hasLocationPermission) {
//                        hasLocationPermission = granted
//                    }
//                    delay(5000)
//                }
//            }
//            // ----------------------------------
//
//            WardrobeTheme(darkTheme = theme == Theme.DARK) {
//                val memberViewModel: MemberViewModel = viewModel(factory = memberVmFactory)
//                MainView(
//                    repo = repo,
//                    vm = memberViewModel,
//                    theme = theme,
//                    onThemeChange = { theme = it },
//                    weatherRepo = weatherRepo,
//                    hasLocationPermission = hasLocationPermission
//                )
//            }
//        }
//    }
//}
//
//// ------------------ 其他部分保持不变 ------------------
//
//class WardrobeViewModelFactory(private val repo: WardrobeRepository, private val memberId: Long) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(WardrobeViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return WardrobeViewModel(repo, memberId) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
//
//@Composable
//fun WardrobeApp(memberId: Long, onExit: () -> Unit) {
//    val context = LocalContext.current
//    val db = AppDatabase.get(context)
//    val repo = WardrobeRepository(db.clothesDao(), db.settingsRepository)
//    val vmFactory = WardrobeViewModelFactory(repo, memberId)
//    val vm: WardrobeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(key = memberId.toString(), factory = vmFactory)
//
//    var route by remember { mutableStateOf("home") }
//    var currentClothingId by remember { mutableStateOf<Long?>(null) }
//
//    BackHandler(enabled = true) {
//        when (route) {
//            "edit" -> route = if (currentClothingId != null) "detail" else "home"
//            "detail" -> route = "home"
//            "home" -> onExit()
//        }
//    }
//
//    when (route) {
//        "home" -> com.example.wardrobe.ui.HomeScreen(
//            vm = vm,
//            onAddClick = { currentClothingId = null; route = "edit" },
//            onItemClick = { id -> currentClothingId = id; route = "detail" }
//        )
//        "detail" -> com.example.wardrobe.ui.ItemDetailScreen(
//            vm = vm,
//            itemId = currentClothingId ?: 0L,
//            onBack = { route = "home" },
//            onEdit = { route = "edit" }
//        )
//        "edit" -> com.example.wardrobe.ui.EditItemScreen(
//            vm = vm,
//            itemId = currentClothingId,
//            onDone = { route = if (currentClothingId != null) "detail" else "home" }
//        )
//    }
//}
//4--------------
package com.example.wardrobe

import android.Manifest
import android.content.pm.PackageManager
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
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.get(this)
        val repo = WardrobeRepository(db.clothesDao(), db.settingsRepository)
        val weatherRepo = WeatherRepository(this)

        val memberVmFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MemberViewModel(repo) as T
            }
        }

        // 共享偏好，用于永久保存“是否已请求过权限”
        val prefs = getSharedPreferences("wardrobe_prefs", MODE_PRIVATE)

        setContent {
            var theme by remember { mutableStateOf(Theme.LIGHT) }

            // ---- 定位权限状态 ----
            var hasLocationPermission by remember { mutableStateOf(false) }
            var askedOnce by remember { mutableStateOf(prefs.getBoolean("askedOnce", false)) }

            // 启动时立即检查权限
            LaunchedEffect(Unit) {
                hasLocationPermission = ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            }

            // 权限请求 Launcher
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                hasLocationPermission = granted
            }

            // 仅首次启动时弹窗（状态持久化）
            LaunchedEffect(hasLocationPermission, askedOnce) {
                if (!hasLocationPermission && !askedOnce) {
                    askedOnce = true
                    prefs.edit().putBoolean("askedOnce", true).apply()
                    permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            }

            // ✅ 持续监听用户手动更改权限（每 5 秒检查一次）
            LaunchedEffect(Unit) {
                while (true) {
                    val granted = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted != hasLocationPermission) {
                        hasLocationPermission = granted
                    }
                    delay(5000)
                }
            }
            // ----------------------------------

            WardrobeTheme(darkTheme = theme == Theme.DARK) {
                val memberViewModel: MemberViewModel = viewModel(factory = memberVmFactory)
                MainView(
                    repo = repo,
                    vm = memberViewModel,
                    theme = theme,
                    onThemeChange = { theme = it },
                    weatherRepo = weatherRepo,
                    hasLocationPermission = hasLocationPermission
                )
            }
        }
    }
}

// ------------------ 其他部分保持不变 ------------------

class WardrobeViewModelFactory(private val repo: WardrobeRepository, private val memberId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WardrobeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WardrobeViewModel(repo, memberId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun WardrobeApp(memberId: Long,weather: WeatherInfo?, onExit: () -> Unit) {
    val context = LocalContext.current
    val db = AppDatabase.get(context)
    val repo = WardrobeRepository(db.clothesDao(), db.settingsRepository)
    val vmFactory = WardrobeViewModelFactory(repo, memberId)
    val vm: WardrobeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(key = memberId.toString(), factory = vmFactory)

    var route by remember { mutableStateOf("home") }
    var currentClothingId by remember { mutableStateOf<Long?>(null) }

    BackHandler(enabled = true) {
        when (route) {
            "edit" -> route = if (currentClothingId != null) "detail" else "home"
            "detail" -> route = "home"
            "home" -> onExit()
        }
    }

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
