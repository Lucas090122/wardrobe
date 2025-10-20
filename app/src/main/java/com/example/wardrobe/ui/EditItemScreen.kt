package com.example.wardrobe.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.wardrobe.ui.components.TagChips
import com.example.wardrobe.viewmodel.WardrobeViewModel
import com.example.wardrobe.util.persistImageToAppStorage
import java.io.File
import java.util.UUID
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.unit.min
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    vm: WardrobeViewModel,
    itemId: Long? = null,
    onDone: () -> Unit
) {
    val ui by vm.uiState.collectAsState()
    val allTags = ui.tags

    val editing = if (itemId != null) vm.itemFlow(itemId).collectAsState(initial = null).value else null

    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val selectedIds = remember { mutableStateListOf<Long>() }

    // 预填
    LaunchedEffect(editing?.item?.itemId, allTags) {
        editing?.let { d ->
            description = d.item.description
            imageUri = d.item.imageUri?.toUri()
            selectedIds.clear(); selectedIds.addAll(d.tags.map { it.tagId })
        }
    }

    val context = LocalContext.current

    // 相册选择
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) imageUri = uri }
    )

    // 预先为相机创建输出文件（写在 app 私有目录 /files/images）
    fun newImageFile(): File {
        val dir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
        return File(dir, "${UUID.randomUUID()}.jpg")
    }

    // 拍照：用 FileProvider 提供写入的 content://，成功后我们直接保存成 file://（稳定）
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { ok ->
            val file = pendingPhotoFile
            if (ok && file != null) {
                imageUri = file.toUri() // 保存 file://，无需再拷贝，重启稳定
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId == null) "添加衣服" else "编辑衣服") },
                navigationIcon = { TextButton(onClick = onDone) { Text("返回") } },
                actions = {
                    TextButton(onClick = {
                        val finalImageUri = imageUri?.let { uri ->
                            if (uri.scheme == "file") uri
                            else persistImageToAppStorage(context, uri)   // 先复制到 filesDir/images
                        }

                        vm.saveItem(
                            itemId = itemId,
                            description = description.trim(),
                            imageUri = finalImageUri?.toString(),
                            tagIds = selectedIds.toList()
                        )
                        onDone()
                    }) { Text("保存") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {

            // —— 图片区域：点击可从相册选 —— //
            val screenH = LocalConfiguration.current.screenHeightDp.dp
            val maxImageH = 360.dp // 或者 screenH * 0.5f

            var aspect by remember { mutableStateOf(1.6f) } // 宽 / 高，先给个默认

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // 当前可用宽度
                val w = this.maxWidth
                // 理论高度 = w / aspect；实际高度 = min(理论高度, 你的最大值)
                val desired = w / aspect
                val targetH = min(desired, maxImageH)

                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .width(w)           // 明确宽度
                            .height(targetH),   // 👈 直接指定最终高度，保证上限生效
                        contentScale = ContentScale.Fit, // 完整显示
                        onSuccess = { s ->
                            val d = s.result.drawable
                            val iw = max(1, d.intrinsicWidth)
                            val ih = max(1, d.intrinsicHeight)
                            aspect = iw.toFloat() / ih  // 更新真实比例后会自动重算 targetH
                        }
                    )
                } else {
                    // 没图时占位
                    Box(
                        Modifier
                            .width(w)
                            .height(180.dp)
                            .background(Color(0x11FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) { Text("点此选择图片") }
                }
            }

            Spacer(Modifier.height(8.dp))

            // —— 新增一行“拍照”与“从相册”按钮 —— //
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    // 准备输出文件 & content Uri
                    val file = newImageFile().also { pendingPhotoFile = it }
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    takePicture.launch(contentUri)
                }) { Text("拍照") }

                OutlinedButton(onClick = {
                    galleryPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) { Text("从相册选择") }
            }

            Spacer(Modifier.height(12.dp))

            // 描述
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("描述（如：蓝色羽绒服 110cm）") },
                modifier = Modifier.fillMaxWidth()
            )

            // 标签多选
            if (allTags.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("标签")
                Spacer(Modifier.height(8.dp))
                TagChips(
                    tags = allTags,
                    selectedIds = selectedIds.toSet(),
                    onToggle = { id ->
                        if (selectedIds.contains(id)) selectedIds.remove(id)
                        else selectedIds.add(id)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}