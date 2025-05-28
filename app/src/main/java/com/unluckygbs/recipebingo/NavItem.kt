package com.unluckygbs.recipebingo

import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavIcon {
    data class Vector(val image: ImageVector): NavIcon()
    data class Drawable(val resId: Int): NavIcon()
}

data class NavItem(
    val label: String,
    val icon: NavIcon
)
