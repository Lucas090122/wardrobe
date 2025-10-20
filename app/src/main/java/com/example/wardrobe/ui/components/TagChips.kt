package com.example.wardrobe.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wardrobe.data.Tag

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagChips(
    tags: List<Tag>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            val selected = tag.tagId in selectedIds
            FilterChip(
                selected = selected,
                onClick = { onToggle(tag.tagId) },
                label = { Text(tag.name) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TagChipsPreview() {
    val demoTags = listOf(
        Tag(tagId = 1, name = "冬装"),
        Tag(tagId = 2, name = "夏装"),
        Tag(tagId = 3, name = "上衣"),
        Tag(tagId = 4, name = "裤子")
    )
    TagChips(
        tags = demoTags,
        selectedIds = setOf(1L, 4L),
        onToggle = {}
    )
}