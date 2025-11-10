package com.example.wardrobe.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wardrobe.Screen
import com.example.wardrobe.data.Member
import com.example.wardrobe.data.Theme
import com.example.wardrobe.ui.AddMemberDialog
import com.example.wardrobe.viewmodel.MemberViewModel

@Composable
fun SimpleDrawerItem(
    item : Screen.DrawerScreen,
    selected: Boolean,
    onItemClicked: () -> Unit
){
    Row(
        Modifier.fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .clickable {
                onItemClicked()
            }
    ) {
        /*Icon(
            painter = painterResource(id = item.icon),
            contentDescription = item.dTitle,
            Modifier.padding(end = 8.dp, top = 4.dp)
        )*/
        Text(
            text = item.dTitle,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ToggleDrawerItem(
    currentTheme: Theme,
    onThemeChange: (theme:Theme) -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Dark Mode",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = currentTheme == Theme.DARK,
            onCheckedChange = { isChecked ->
                onThemeChange(if (isChecked) Theme.DARK else Theme.LIGHT)
            },
        )
    }
}

@Composable
fun AdminModeDrawerItem(
    isAdmin: Boolean,
    onAdminChange: (Boolean) -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Admin Mode",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isAdmin,
            onCheckedChange = { checked ->
                onAdminChange(checked)
            },
        )
    }
}

@Composable
fun ExpandableDrawerItem(
    item: Screen.DrawerScreen,
    subItems: List<Member>,
    onItemClicked: () -> Unit,
    vm: MemberViewModel,
    controller: NavController
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.dTitle,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }

        // Subitems
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                subItems.forEach { item ->
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                controller.navigate(Screen.DrawerScreen.Member.createRoute(item.memberId))
                                onItemClicked()
                            }
                            .padding(vertical = 8.dp)
                    )
                }
                Row (
                    modifier = Modifier.fillMaxWidth(.8f)
                        .clip(RoundedCornerShape(30))
                        .background(Color.Green)
                        .clickable{showAddMemberDialog = true},
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,

                    ){
                    Text("Add Member")
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add member"
                    )

                }

            }
        }
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onSave = {
                vm.createMember(it.name, it.gender, it.age)
                showAddMemberDialog = false
            }
        )
    }
}