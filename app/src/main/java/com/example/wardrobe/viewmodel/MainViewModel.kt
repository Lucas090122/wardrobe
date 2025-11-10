package com.example.wardrobe.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.wardrobe.Screen

class MainViewModel: ViewModel() {
    private val _currentScreen : MutableState<Screen> = mutableStateOf(Screen.DrawerScreen.Home)

    val currentScreen: MutableState<Screen>
        get() = _currentScreen

    fun setCurrentScreen(screen: Screen){
        _currentScreen.value = screen
    }
}