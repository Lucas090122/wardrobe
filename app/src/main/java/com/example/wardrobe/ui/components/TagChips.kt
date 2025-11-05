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

data class TagUiModel(
    val id: Long,
    val name: String,
    val count: Int? = null
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagChips(
    tags: List<TagUiModel>,
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
            val selected = tag.id in selectedIds
            FilterChip(
                selected = selected,
                onClick = { onToggle(tag.id) },
                label = {
                    val text = if (tag.count != null && tag.count > 0) {
                        "${tag.name} (${tag.count})"
                    } else {
                        tag.name
                    }
                    Text(text)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TagChipsPreview() {
    val demoTags = listOf(
        TagUiModel(id = 1, name = "Winter", count = 5),
        TagUiModel(id = 2, name = "Summer", count = 0),
        TagUiModel(id = 3, name = "Top", count = 12),
        TagUiModel(id = 4, name = "Pants")
    )
    TagChips(
        tags = demoTags,
        selectedIds = setOf(1L, 4L),
        onToggle = {}
    )
}