package com.example.wardrobe.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.wardrobe.data.Member
import com.example.wardrobe.viewmodel.MemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberSelectionScreen(
    vm: MemberViewModel,

    onMemberSelected: (Long) -> Unit
) {
    val members by vm.members.collectAsState()
    val outdatedMap by vm.outdatedCounts.collectAsState()
    var showAddMemberDialog by remember { mutableStateOf(false) }

    Scaffold(
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { showAddMemberDialog = true }) {
                Text("Add New Member")
            }

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            if (members.isEmpty()) {
                Text("No members yet. Add one above!")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(members) { member ->
                        val outdated = outdatedMap[member.memberId] ?: 0
                        MemberCard(member = member, outdatedCount = outdated, onClick = {
                            vm.setCurrentMember(member.memberId)
                            onMemberSelected(member.memberId)
                        })
                    }
                }
            }
        }
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onSave = { name, gender, age, birthDate ->
                vm.createMember(name, gender, age, birthDate)
                showAddMemberDialog = false
            }
        )
    }
}

@Composable
fun MemberCard(
    member: Member,
    outdatedCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${member.gender}, ${member.age}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (member.age < 18 && outdatedCount > 0) {
                AssistChip(
                    onClick = { /* 这里只是显示，不必有动作，未来可以点进去看详情 */ },
                    label = { Text("$outdatedCount items may be too small") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }

    val genderOptions = listOf("Male", "Female")

    val ageInt = ageText.toIntOrNull() ?: 0
    val isMinor = ageInt in 0 until 18

    var showDatePicker by remember { mutableStateOf(false) }
    var birthDateMillis by remember { mutableStateOf<Long?>(null) }

    val isSaveEnabled =
        name.isNotBlank() &&
                gender.isNotBlank() &&
                ageText.isNotBlank() &&
                (!isMinor || birthDateMillis != null)

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Add New Member", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Gender", style = MaterialTheme.typography.bodyLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        genderOptions.forEach { option ->
                            FilterChip(
                                selected = gender == option,
                                onClick = { gender = option },
                                label = { Text(option) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Age") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                if (isMinor) {
                    Column {
                        Text("Birthday", style = MaterialTheme.typography.bodyLarge)
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(
                                if (birthDateMillis != null)
                                    "Selected: ${java.text.SimpleDateFormat("yyyy-MM-dd")
                                        .format(java.util.Date(birthDateMillis!!))}"
                                else
                                    "Select birthday"
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                name,
                                gender,
                                ageInt,
                                if (isMinor) birthDateMillis else null
                            )
                        },
                        enabled = isSaveEnabled
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            birthDateMillis = selected
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}