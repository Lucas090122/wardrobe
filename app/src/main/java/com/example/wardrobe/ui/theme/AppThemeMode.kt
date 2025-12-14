package com.example.wardrobe.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.example.wardrobe.R

enum class Theme {
    DARK, LIGHT
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun appBarColor (
    theme : Theme
) : TopAppBarColors {
    //val isDark = isSystemInDarkTheme()
    return if (theme == Theme.LIGHT)
    {
        TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(id = R.color.app_bar_bg_light),
            titleContentColor = colorResource(id = R.color.app_bar_text_light)
        )
    } else
    {
        TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(id = R.color.app_bar_bg_dark),
            titleContentColor = colorResource(id = R.color.app_bar_text_dark)
        )
    }
}