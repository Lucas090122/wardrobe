package com.example.wardrobe.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wardrobe.data.Member
import com.example.wardrobe.viewmodel.MemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberSelectionScreen(
    vm: MemberViewModel,
    onMemberSelected: (Long) -> Unit
) {
    val members by vm.members.collectAsState()
    var newMemberName by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Select or Create a Member") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // New Member Input
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newMemberName,
                    onValueChange = { newMemberName = it },
                    label = { Text("New member name") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newMemberName.isNotBlank()) {
                            vm.createMember(newMemberName)
                            newMemberName = ""
                        }
                    },
                    enabled = newMemberName.isNotBlank()
                ) {
                    Text("Create")
                }
            }

            Divider()

            // Member List
            if (members.isEmpty()) {
                Text("No members yet. Add one above!")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(members) { member ->
                        MemberCard(member = member, onClick = { onMemberSelected(member.memberId) })
                    }
                }
            }
        }
    }
}

@Composable
fun MemberCard(member: Member, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text(
            text = member.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}