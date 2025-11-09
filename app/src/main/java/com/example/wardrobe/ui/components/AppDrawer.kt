package com.example.wardrobe.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wardrobe.Screen
import com.example.wardrobe.ScreensInDrawer
import com.example.wardrobe.data.Theme
import com.example.wardrobe.data.appBarColor
import com.example.wardrobe.data.bgColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.wardrobe.data.textColor
import com.example.wardrobe.viewmodel.WardrobeViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    title: String = "Drawer",
    //content: @Composable (PaddingValues) -> Unit
){
    /*val viewModel : WardrobeViewModel = viewModel()
    val currentScreen = remember {
        viewModel.currentScreen.value
    }
    val title = remember{
        mutableStateOf(currentScreen.title)
    }*/

    /*val controller: NavController = rememberNavController()
    val navBackStackEntry by controller.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route*/

    val scope : CoroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var theme by remember { mutableStateOf(Theme.LIGHT) }

    val drawerContent = @Composable {
        ModalDrawerSheet (
            modifier = Modifier.padding(top = 40.dp)
        ){
            /*Text(
                text = "Navigation Menu",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            Divider()*/
            ExpandableDrawerItem(
                item = Screen.DrawerScreen.Member,
                subItems = listOf(
                    "Mark",
                    "Henry",
                    "John"
                ),
                onItemClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                },
                onButtonClicked = {
                        scope.launch {
                            drawerState.close()
                        }
                }
            )
            LazyColumn (
                //modifier = Modifier.padding(top = 40.dp)
            ) {
                items(ScreensInDrawer){
                    item -> SimpleDrawerItem(
                    //selected = currentRoute == item.dRoute,
                    selected = false,
                    item = item,
                    ) {
                    scope.launch {
                        drawerState.close()
                    }
                    //controller.navigate(item.dRoute)
                    //title.value = item.dTitle
                    }
                }
            }

            ToggleDrawerItem(theme) { newTheme ->
                theme = newTheme
            }
        }
    }

    ModalNavigationDrawer(
        drawerContent = drawerContent,
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomEnd = 12.dp,
                            bottomStart = 12.dp
                        )
                    ),
                    colors = appBarColor(theme),
                    title = { Text("Wardrobe") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Open Drawer",
                                modifier = Modifier.padding(5.dp)
                            )
                        }
                    },
                    actions = {
                        Row (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            IconButton(
                                onClick = {
                                    //TODO("Updates the weather using weather api")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "weather",
                                    modifier = Modifier.padding(5.dp)
                                )
                            }
                            Text("Temp")
                        }
                    }
                )
            },
            content = { innerPadding ->
                Scaffold (
                    containerColor = bgColor(theme)
                ){
                    Text(
                    "Main Screen Content",
                    modifier = Modifier.padding(innerPadding)
                )
                } // <- You control the main screen content

            }
        )
    }
}

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
            style = MaterialTheme.typography.bodyLarge
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
fun ExpandableDrawerItem(
    item: Screen.DrawerScreen,
    subItems: List<String>,
    onItemClicked: (String) -> Unit,
    onButtonClicked: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

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
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClicked(item) }
                            .padding(vertical = 8.dp)
                    )
                }
                Row (
                    modifier = Modifier.fillMaxWidth(.8f)
                        .clip(RoundedCornerShape(30))
                        .background(Color.Green)
                        .clickable{onButtonClicked()},
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
}