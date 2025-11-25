package com.example.wardrobe.ui.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.wardrobe.data.ClothingItem

@Composable
fun ClothingCard(
    item: ClothingItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = android.text.format.DateFormat.getDateFormat(LocalContext.current)
        .format(item.createdAt)
    ListItem(
        headlineContent = {
            Text(
                text = item.description,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(formattedDate)
        },
        leadingContent = {
            val uri = item.imageUri?.let { Uri.parse(it) }
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
            }
        },
        modifier = modifier.clickable(onClick = onClick)
    )
}