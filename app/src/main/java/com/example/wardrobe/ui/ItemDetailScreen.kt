package com.example.wardrobe.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.wardrobe.viewmodel.WardrobeViewModel
import com.example.wardrobe.ui.components.TagChips
import kotlin.math.max
import androidx.compose.ui.platform.LocalConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    vm: WardrobeViewModel,
    itemId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val data by vm.itemFlow(itemId).collectAsState(initial = null)
    var showConfirm by remember { mutableStateOf(false) } // 👈 提到屏幕作用域

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("衣物详情") },
                navigationIcon = { TextButton(onClick = onBack) { Text("返回") } },
                actions = {
                    TextButton(onClick = onEdit) { Text("编辑") }
                    TextButton(onClick = { showConfirm = true }) { Text("删除") } // 👈 按钮在此
                }
            )
        }
    ) { padding ->
        if (data == null) {
            Box(Modifier.padding(padding).fillMaxSize()) { Text("加载中…") }
        } else {
            val item = data!!.item
            val tags = data!!.tags

            // 屏幕高度，用来限制图片的最大高度（比如不超过 60%）
            val screenH = LocalConfiguration.current.screenHeightDp.dp
            val maxImageH = screenH * 0.6f

            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    val uri = item.imageUri?.let { android.net.Uri.parse(it) }
                    if (uri != null) {
                        var aspect by remember { mutableFloatStateOf(1.6f) }

                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                // 按比例自适应，但不超过屏幕高度的 60%
                                .heightIn(max = maxImageH),
                            contentScale = ContentScale.Fit,
                            onSuccess = { success ->
                                val d = success.result.drawable
                                val w = max(1, d.intrinsicWidth)
                                val h = max(1, d.intrinsicHeight)
                                aspect = w.toFloat() / h
                            }
                        )
                    }
                }

                item {
                    Text(text = item.description, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = android.text.format.DateFormat
                            .format("yyyy-MM-dd", item.createdAt).toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (tags.isNotEmpty()) {
                    item {
                        Text("标签", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        TagChips(
                            tags = tags,
                            selectedIds = tags.map { it.tagId }.toSet(),
                            onToggle = {},
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // 👇 弹窗放在 Scaffold 外层（同一 Composable 内）
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这件衣服吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteItem(itemId)
                    showConfirm = false
                    onBack() // 返回首页
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("取消") }
            }
        )
    }
}