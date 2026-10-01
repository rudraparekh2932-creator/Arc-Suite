package com.example.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Returns true if the device screen width is at least 600dp (Medium/Expanded Window Size Class - Tablets & Foldables).
 */
@Composable
fun isTablet(): Boolean {
    val config = LocalConfiguration.current
    return config.screenWidthDp >= 600
}

/**
 * Returns true if the device screen width is at least 840dp (Expanded Window Size Class - Large Tablets & Desktops).
 */
@Composable
fun isExpandedTablet(): Boolean {
    val config = LocalConfiguration.current
    return config.screenWidthDp >= 840
}
