package com.example.wardrobe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wardrobe.data.AppDatabase
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.ui.EditItemScreen
import com.example.wardrobe.ui.HomeScreen
import com.example.wardrobe.ui.ItemDetailScreen
import com.example.wardrobe.ui.MemberSelectionScreen
import com.example.wardrobe.viewmodel.MemberViewModel
import com.example.wardrobe.viewmodel.WardrobeViewModel
import com.example.wardrobe.ui.theme.WardrobeTheme
import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.wardrobe.ui.components.MainView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.get(this)
        val repo = WardrobeRepository(db.clothesDao())


        val memberVmFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MemberViewModel(repo) as T
            }
        }


        setContent {
            WardrobeTheme {
                //var selectedMemberId by remember { mutableStateOf<Long?>(null) }
                val memberViewModel: MemberViewModel = viewModel(factory = memberVmFactory)
                MainView(
                    repo = repo,
                    vm = memberViewModel
                    )

                /*if (selectedMemberId == null) {
                    // This is the root screen, handle app exit here
                    var lastBackTime by remember { mutableLongStateOf(0L) }
                    val context = LocalContext.current
                    BackHandler(enabled = true) {
                        val now = System.currentTimeMillis()
                        if (now - lastBackTime < 2000) {
                            finish() // Exit the app
                        } else {
                            lastBackTime = now
                            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                        }
                    }

                    val memberViewModel: MemberViewModel = viewModel(factory = memberVmFactory)
                    MemberSelectionScreen(
                        vm = memberViewModel,
                        onMemberSelected = { memberId ->
                            selectedMemberId = memberId
                        }
                    )
                } else {
                    WardrobeApp(memberId = selectedMemberId!!) {
                        selectedMemberId = null // On back pressed from app, go back to member selection
                    }
                }*/
            }
        }
    }
}

// A factory for creating WardrobeViewModel instances for a specific member
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
fun WardrobeApp(memberId: Long, onExit: () -> Unit) {
    val context = LocalContext.current
    val repo = WardrobeRepository(AppDatabase.get(context).clothesDao())
    val vmFactory = WardrobeViewModelFactory(repo, memberId)
    val vm: WardrobeViewModel = viewModel(key = memberId.toString(), factory = vmFactory)

    var route by remember { mutableStateOf("home") } // home / detail / edit
    var currentClothingId by remember { mutableStateOf<Long?>(null) }

    BackHandler(enabled = true) {
        when (route) {
            "edit" -> {
                route = if (currentClothingId != null) "detail" else "home"
            }
            "detail" -> {
                route = "home"
            }
            "home" -> {
                // Single press back to go to member selection
                onExit()
            }
        }
    }

    when (route) {
        "home" -> {
            HomeScreen(
                vm = vm,
                onAddClick = { currentClothingId = null; route = "edit" },
                onItemClick = { id -> currentClothingId = id; route = "detail" }
            )
        }
        "detail" -> ItemDetailScreen(
            vm = vm,
            itemId = currentClothingId ?: 0L,
            onBack = { route = "home" },
            onEdit = { route = "edit" }
        )
        "edit" -> EditItemScreen(
            vm = vm,
            itemId = currentClothingId,
            onDone = {
                route = if (currentClothingId != null) "detail" else "home"
            }
        )
    }
}