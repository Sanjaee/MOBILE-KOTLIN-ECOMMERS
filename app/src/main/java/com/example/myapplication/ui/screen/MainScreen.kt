package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null
) {
    object Home : BottomNavItem("main/home", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    object Food : BottomNavItem("main/food", "Food", Icons.Outlined.PlayArrow)
    object Promo : BottomNavItem("main/promo", "Promo", Icons.Outlined.LocalOffer)
    object Transaksi : BottomNavItem("main/transaksi", "Transaksi", Icons.Outlined.Receipt)
    object Akun : BottomNavItem("main/akun", "Akun", Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    selectedTab: String = BottomNavItem.Home.route,
    onTabSelected: (String) -> Unit,
    onLogout: () -> Unit,
    onProductClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedRoute = selectedTab,
                onItemSelected = onTabSelected,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content()
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        // Home
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (selectedRoute == BottomNavItem.Home.route && BottomNavItem.Home.selectedIcon != null) {
                        BottomNavItem.Home.selectedIcon!!
                    } else {
                        BottomNavItem.Home.icon
                    },
                    contentDescription = BottomNavItem.Home.title
                )
            },
            label = {
                Text(
                    text = BottomNavItem.Home.title,
                    fontSize = 11.sp
                )
            },
            selected = selectedRoute == BottomNavItem.Home.route,
            onClick = {
                if (selectedRoute != BottomNavItem.Home.route) {
                    onItemSelected(BottomNavItem.Home.route)
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF10B981),
                selectedTextColor = Color(0xFF10B981),
                indicatorColor = Color.White,
                unselectedIconColor = Color(0xFF6B7280),
                unselectedTextColor = Color(0xFF6B7280)
            )
        )
        
        // Food
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = BottomNavItem.Food.icon,
                    contentDescription = BottomNavItem.Food.title
                )
            },
            label = {
                Text(
                    text = BottomNavItem.Food.title,
                    fontSize = 11.sp
                )
            },
            selected = selectedRoute == BottomNavItem.Food.route,
            onClick = {
                if (selectedRoute != BottomNavItem.Food.route) {
                    onItemSelected(BottomNavItem.Food.route)
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF10B981),
                selectedTextColor = Color(0xFF10B981),
                indicatorColor = Color.White,
                unselectedIconColor = Color(0xFF6B7280),
                unselectedTextColor = Color(0xFF6B7280)
            )
        )
        
        // Promo
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = BottomNavItem.Promo.icon,
                    contentDescription = BottomNavItem.Promo.title
                )
            },
            label = {
                Text(
                    text = BottomNavItem.Promo.title,
                    fontSize = 11.sp
                )
            },
            selected = selectedRoute == BottomNavItem.Promo.route,
            onClick = {
                if (selectedRoute != BottomNavItem.Promo.route) {
                    onItemSelected(BottomNavItem.Promo.route)
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF10B981),
                selectedTextColor = Color(0xFF10B981),
                indicatorColor = Color.White,
                unselectedIconColor = Color(0xFF6B7280),
                unselectedTextColor = Color(0xFF6B7280)
            )
        )
        
        // Transaksi
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = BottomNavItem.Transaksi.icon,
                    contentDescription = BottomNavItem.Transaksi.title
                )
            },
            label = {
                Text(
                    text = BottomNavItem.Transaksi.title,
                    fontSize = 11.sp
                )
            },
            selected = selectedRoute == BottomNavItem.Transaksi.route,
            onClick = {
                if (selectedRoute != BottomNavItem.Transaksi.route) {
                    onItemSelected(BottomNavItem.Transaksi.route)
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF10B981),
                selectedTextColor = Color(0xFF10B981),
                indicatorColor = Color.White,
                unselectedIconColor = Color(0xFF6B7280),
                unselectedTextColor = Color(0xFF6B7280)
            )
        )
        
        // Akun
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = BottomNavItem.Akun.icon,
                    contentDescription = BottomNavItem.Akun.title
                )
            },
            label = {
                Text(
                    text = BottomNavItem.Akun.title,
                    fontSize = 11.sp
                )
            },
            selected = selectedRoute == BottomNavItem.Akun.route,
            onClick = {
                if (selectedRoute != BottomNavItem.Akun.route) {
                    onProfileClick()
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF10B981),
                selectedTextColor = Color(0xFF10B981),
                indicatorColor = Color.White,
                unselectedIconColor = Color(0xFF6B7280),
                unselectedTextColor = Color(0xFF6B7280)
            )
        )
    }
}
