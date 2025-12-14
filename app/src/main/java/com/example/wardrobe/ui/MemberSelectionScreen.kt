package com.example.wardrobe.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.wardrobe.R
import com.example.wardrobe.data.Member
import com.example.wardrobe.viewmodel.MemberViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberSelectionScreen(
    vm: MemberViewModel,
    onMemberSelected: (Long) -> Unit
) {
    val members by vm.members.collectAsState()
    val outdatedMap by vm.outdatedCounts.collectAsState()
    var showAddMemberDialog by remember { mutableStateOf(false) }
    val isAdminMode by vm.isAdminMode.collectAsState()
    var deleteTarget by remember { mutableStateOf<Member?>(null) }
    var showDeleteDenied by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Member?>(null) }


    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { showAddMemberDialog = true }) {
                Text(stringResource(R.string.add_new_member))
            }

            Spacer(Modifier.height(16.dp))
            @Suppress("DEPRECATION")
            Divider()
            Spacer(Modifier.height(16.dp))

            if (members.isEmpty()) {
                Text(stringResource(R.string.no_members_yet))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(members, key = { it.memberId }) { member ->
                        val outdated = outdatedMap[member.memberId] ?: 0

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->

                                when (value) {
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        if (isAdminMode) {
                                            deleteTarget = member
                                        } else {
                                            showDeleteDenied = true
                                        }
                                    }

                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        editTarget = member
                                    }

                                    else -> Unit
                                }

                                false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val bgColor = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceContainerLowest
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(bgColor),
                                    contentAlignment = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                        else -> Alignment.Center
                                    }
                                ) {
                                    val icon = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                                        else -> null
                                    }
                                    icon?.let {
                                        Icon(
                                            it,
                                            contentDescription = null,
                                            modifier = Modifier.padding(24.dp)
                                        )
                                    }
                                }
                            },
                            content = {
                                MemberCard(
                                    member = member,
                                    outdatedCount = outdated,
                                    onClick = {
                                        vm.setCurrentMember(member.memberId)
                                        onMemberSelected(member.memberId)
                                    }
                                )
                            }
                        )
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

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.delete_member)) },
            text = {
                Text(stringResource(R.string.delete_member_confirmation, deleteTarget!!.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteMember(deleteTarget!!.memberId)
                        deleteTarget = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDeleteDenied) {
        AlertDialog(
            onDismissRequest = { showDeleteDenied = false },
            title = { Text(stringResource(R.string.action_not_allowed)) },
            text = { Text(stringResource(R.string.admin_only_delete_warning)) },
            confirmButton = {
                TextButton(onClick = { showDeleteDenied = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    if (editTarget != null) {
        EditMemberDialog(
            member = editTarget!!,
            onDismiss = { editTarget = null },
            onSave = { name, gender, age, birthDate ->
                vm.updateMember(editTarget!!.memberId, name, gender, age, birthDate)
                editTarget = null
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
                Text(member.name, style = MaterialTheme.typography.titleMedium)
                Text("${member.gender}, ${member.age}", style = MaterialTheme.typography.bodyMedium)
            }

            if (member.age < 18 && outdatedCount > 0) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(stringResource(R.string.outdated_items_chip, outdatedCount))
                    },
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

    val genderOptions = listOf(
        stringResource(R.string.gender_male),
        stringResource(R.string.gender_female)
    )

    val ageInt = ageText.toIntOrNull()
    val isMinor = ageInt != null && ageInt in 0 until 18

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
                Text(stringResource(R.string.add_new_member), style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.gender), style = MaterialTheme.typography.bodyLarge)
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
                    label = { Text(stringResource(R.string.age)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                if (isMinor) {
                    Column {
                        Text(stringResource(R.string.birthday), style = MaterialTheme.typography.bodyLarge)
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(
                                if (birthDateMillis != null)
                                    stringResource(
                                        R.string.selected_birthday,
                                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            .format(Date(birthDateMillis!!))
                                    )
                                else stringResource(R.string.select_birthday)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(
                            name,
                            gender,
                            ageInt!!,
                            if (isMinor) birthDateMillis else null
                        )
                    }, enabled = isSaveEnabled) {
                        Text(stringResource(R.string.save))
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
                        if (selected != null) birthDateMillis = selected
                        showDatePicker = false
                    }
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMemberDialog(
    member: Member,
    onDismiss: () -> Unit,
    onSave: (updatedName: String, updatedGender: String, updatedAge: Int, updatedBirthDate: Long?) -> Unit
) {
    var name by remember { mutableStateOf(member.name) }
    var gender by remember { mutableStateOf(member.gender) }
    var ageText by remember { mutableStateOf(member.age.toString()) }

    val genderOptions = listOf(
        stringResource(R.string.gender_male),
        stringResource(R.string.gender_female)
    )

    val ageInt = ageText.toIntOrNull()
    val isMinor = ageInt != null && ageInt in 0 until 18

    var showDatePicker by remember { mutableStateOf(false) }
    var birthDateMillis by remember { mutableStateOf(member.birthDate) }

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
                Text(
                    text = stringResource(R.string.edit_member),
                    style = MaterialTheme.typography.titleLarge
                )

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Gender options
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.gender),
                        style = MaterialTheme.typography.bodyLarge
                    )
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

                // Age
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { ch -> ch.isDigit() } },
                    label = { Text(stringResource(R.string.age)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Birthday if minor
                if (isMinor) {
                    Column {
                        Text(
                            text = stringResource(R.string.birthday),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(
                                if (birthDateMillis != null)
                                    stringResource(
                                        R.string.selected_birthday,
                                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            .format(Date(birthDateMillis!!))
                                    )
                                else stringResource(R.string.select_birthday)
                            )
                        }
                    }
                }

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                name,
                                gender,
                                ageInt!!,
                                if (isMinor) birthDateMillis else null
                            )
                        },
                        enabled = isSaveEnabled
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = birthDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) birthDateMillis = selected
                        showDatePicker = false
                    }
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
