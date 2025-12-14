package com.example.wardrobe.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.wardrobe.R

data class TagUiModel(
    val id: Long,
    val name: String,
    val count: Int? = null,
    val isDeletable: Boolean = true
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagChips(
    tags: List<TagUiModel>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
    showCount: Boolean = true,
    onDelete: ((Long) -> Unit)? = null
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        tags.forEach { tag ->
            val selected = tag.id in selectedIds
            FilterChip(
                selected = selected,
                onClick = { onToggle(tag.id) },
                label = {
                    val text = if (showCount && tag.count != null && tag.count > 0) {
                        "${tag.name} (${tag.count})"
                    } else {
                        tag.name
                    }
                    Text(text)
                },
                trailingIcon = {
                    if (onDelete != null && tag.isDeletable) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onDelete(tag.id) }
                        )
                    }
                }
            )
        }
    }
}