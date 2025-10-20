package com.example.wardrobe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wardrobe.data.AppDatabase
import com.example.wardrobe.data.WardrobeRepository
import com.example.wardrobe.ui.EditItemScreen
import com.example.wardrobe.ui.HomeScreen
import com.example.wardrobe.ui.ItemDetailScreen
import com.example.wardrobe.viewmodel.WardrobeViewModel
import com.example.wardrobe.ui.theme.WardrobeTheme
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableLongStateOf
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.get(this)
        val repo = WardrobeRepository(db.clothesDao())
        val vmFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return WardrobeViewModel(repo) as T
            }
        }
        val vm = ViewModelProvider(this, vmFactory)[WardrobeViewModel::class.java]

        setContent {
            WardrobeTheme {
                var route by remember { mutableStateOf("home") } // home / detail / edit
                var currentId by remember { mutableStateOf<Long?>(null) }
                val context = LocalContext.current
                var lastBackTime by remember { mutableLongStateOf(0L) }

                androidx.activity.compose.BackHandler(enabled = true) {
                    when (route) {
                        "edit" -> {
                            // 如果是编辑已有 → 回详情，否则回首页
                            route = if (currentId != null) "detail" else "home"
                        }
                        "detail" -> {
                            route = "home"
                        }
                        else -> {
                            // 首页：双击返回退出
                            val now = System.currentTimeMillis()
                            if (now - lastBackTime < 2000) {
                                // 两秒内第二次按 → 退出
                                finish()
                            } else {
                                lastBackTime = now
                                Toast.makeText(context, "再按一次返回退出应用", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                when (route) {
                    "home" -> {
                        val ui by vm.uiState.collectAsState()
                        HomeScreen(
                            tags = ui.tags,
                            selectedTagIds = ui.selectedTagIds,
                            query = ui.query,
                            items = ui.items,
                            onToggleTag = vm::toggleTag,
                            onQueryChange = vm::setQuery,
                            onAddClick = { currentId = null; route = "edit" },
                            onItemClick = { id -> currentId = id; route = "detail" } // 现在先到详情
                        )
                    }
                    "detail" -> ItemDetailScreen(
                        vm = vm,
                        itemId = currentId ?: 0L,
                        onBack = { route = "home" },
                        onEdit = { route = "edit" }
                    )
                    "edit" -> EditItemScreen(
                        vm = vm,
                        itemId = currentId,
                        onDone = {
                            route = if (currentId != null) "detail" else "home"
                        }
                    )
                }
            }
        }
    }
}