package com.example.wardrobe.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wardrobe.R
import com.example.wardrobe.data.WardrobeRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferHistoryScreen(
    repo: WardrobeRepository,
    navController: NavController
) {
    val transferHistory by repo.getAllTransferHistoryDetails().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.transfer_history_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (transferHistory.isEmpty()) {
                Text(text = stringResource(R.string.transfer_history_empty))
            } else {
                LazyColumn {
                    items(transferHistory) { record ->
                        val formattedDate = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
                            .format(Date(record.transferTime))

                        Text(
                            text = stringResource(
                                R.string.transfer_history_record,
                                record.sourceMemberName,
                                record.itemName,
                                record.targetMemberName,
                                formattedDate
                            ),
                            modifier = Modifier.padding(vertical = 4.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
