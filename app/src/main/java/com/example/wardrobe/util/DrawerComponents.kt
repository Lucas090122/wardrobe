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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wardrobe.R
import com.example.wardrobe.Screen
import com.example.wardrobe.data.Member
import com.example.wardrobe.ui.theme.Theme
import com.example.wardrobe.ui.AddMemberDialog
import com.example.wardrobe.viewmodel.MemberViewModel

private val DrawerTextItemModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp, vertical = 12.dp)

private val DrawerSwitchItemModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp, vertical = 6.dp)
@Composable
fun SimpleDrawerItem(
    item : Screen.DrawerScreen,
    selected: Boolean,
    onItemClicked: () -> Unit
){
    Row(
        modifier = DrawerTextItemModifier.clickable { onItemClicked() },
    ) {
        /*Icon(
            painter = painterResource(id = item.icon),
            contentDescription = item.dTitle,
            Modifier.padding(end = 8.dp, top = 4.dp)
        )*/
        Text(
            text = stringResource(id = item.titleId),
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ToggleDrawerItem(
    currentTheme: Theme,
    onThemeChange: (theme:Theme) -> Unit
){
    Row(
        modifier = DrawerSwitchItemModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.drawer_dark_mode),
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
        modifier = DrawerSwitchItemModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.drawer_admin_mode),
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
fun AiModeDrawerItem(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = DrawerSwitchItemModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.drawer_ai_mode),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = { checked ->
                onToggle(checked)
            }
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
            modifier = DrawerTextItemModifier.clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = item.titleId),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(if (expanded) R.string.drawer_collapse else R.string.drawer_expand)
            )
        }

        // Subitems
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                subItems.forEach { member ->
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vm.setCurrentMember(member.memberId)

                                controller.navigate(Screen.DrawerScreen.Member.createRoute(member.memberId)) {
                                    popUpTo(controller.graph.startDestinationId) {
                                        saveState = false
                                    }
                                }

                                onItemClicked()
                            }
                            .padding(vertical = 8.dp)
                    )
                }
                Row (
                    modifier = Modifier.fillMaxWidth(.8f)
                        .clip(RoundedCornerShape(30))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable{showAddMemberDialog = true},
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,

                    ){
                    Text(stringResource(R.string.drawer_add_member))
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
            onSave = { name, gender, age, birthDate ->
                vm.createMember(name, gender, age, birthDate)
                showAddMemberDialog = false
            }
        )
    }
}