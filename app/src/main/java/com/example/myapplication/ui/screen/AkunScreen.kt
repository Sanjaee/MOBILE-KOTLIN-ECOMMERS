package com.example.myapplication.ui.screen

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.ProfileViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory

@Composable
fun AkunScreen(
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }
    
    // Handle logout when clicked
    LaunchedEffect(uiState.shouldLogout) {
        if (uiState.shouldLogout) {
            onLogout()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onProfileClick),
            shape = RoundedCornerShape(12.dp),
            color = White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Photo
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = Color(0xFFE5E7EB)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.user?.profilePhoto != null && uiState.user?.profilePhoto?.isNotEmpty() == true) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uiState.user?.profilePhoto)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(30.dp),
                                tint = Color(0xFF6B7280)
                            )
                        }
                    }
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = uiState.user?.fullName ?: "User",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    Text(
                        text = uiState.user?.email ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Profile",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Quick Menu Items
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // My Orders
            ProfileMenuItem(
                icon = Icons.Outlined.ShoppingBag,
                title = "Pesanan Saya",
                onClick = { /* Navigate to orders */ }
            )
            
            // Addresses
            ProfileMenuItem(
                icon = Icons.Outlined.LocationOn,
                title = "Alamat Saya",
                onClick = { /* Navigate to addresses */ }
            )
            
            // Payment Methods
            ProfileMenuItem(
                icon = Icons.Outlined.CreditCard,
                title = "Metode Pembayaran",
                onClick = { /* Navigate to payment methods */ }
            )
            
            // Settings
            ProfileMenuItem(
                icon = Icons.Outlined.Settings,
                title = "Pengaturan",
                onClick = { /* Navigate to settings */ }
            )
            
            // Help & Support
            ProfileMenuItem(
                icon = Icons.Outlined.Help,
                title = "Bantuan & Dukungan",
                onClick = { /* Navigate to help */ }
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFFE5E7EB)
            )
            
            // Logout Button
            ProfileMenuItem(
                icon = Icons.Outlined.Logout,
                title = "Keluar",
                titleColor = Color(0xFFEF4444),
                onClick = {
                    viewModel.logout()
                }
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    titleColor: Color = Black,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (titleColor == Color(0xFFEF4444)) Color(0xFFEF4444) else Color(0xFF6B7280),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = titleColor,
                    fontWeight = if (titleColor == Color(0xFFEF4444)) FontWeight.Medium else FontWeight.Normal
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Navigate",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
