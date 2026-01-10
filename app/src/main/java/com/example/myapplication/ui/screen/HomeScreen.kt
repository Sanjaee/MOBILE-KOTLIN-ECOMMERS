package com.example.myapplication.ui.screen

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.ui.components.product.ProductListSection
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.HomeViewModel
import com.example.myapplication.ui.viewmodel.ProductViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory

@Composable
fun HomeScreenContent(
    onProductClick: (String) -> Unit,
    homeViewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    productViewModel: ProductViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val homeUiState by homeViewModel.uiState.collectAsState()
    val productUiState by productViewModel.uiState.collectAsState()
    
    // Load products on first render
    LaunchedEffect(Unit) {
        productViewModel.loadProducts(page = 1, limit = 10, activeOnly = true)
        productViewModel.loadFeaturedProducts()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        if (productUiState.isLoading && productUiState.products.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = Black)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Loading products...",
                    color = Color(0xFF6B7280),
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Search Bar & Cart Header
                item {
                    SearchAndCartHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                
                // Filter & Location Bar
                item {
                    FilterLocationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                item {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFE5E7EB),
                        thickness = 1.dp
                    )
                }
                
                // Featured Products Section (New Arrival)
                if (productUiState.featuredProducts.isNotEmpty()) {
                    item {
                        ProductListSection(
                            title = "New arrival di ${getCurrentMonthYear()}",
                            products = productUiState.featuredProducts,
                            onProductClick = { product ->
                                onProductClick(product.id)
                            },
                            onSeeAllClick = {
                                // Navigate to all featured products
                            },
                            isHorizontal = true
                        )
                    }
                    
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                            color = Color(0xFFDC2626),
                            thickness = 2.dp
                        )
                    }
                }
                
                // All Products Section
                if (productUiState.products.isNotEmpty()) {
                    item {
                        ProductListSection(
                            title = "Semua Produk",
                            products = productUiState.products,
                            onProductClick = { product ->
                                onProductClick(product.id)
                            },
                            isHorizontal = false
                        )
                    }
                } else if (!productUiState.isLoading) {
                    // Empty State
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "No products found",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )
                            
                            productUiState.errorMessage?.let { error ->
                                Text(
                                    text = error,
                                    fontSize = 14.sp,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchAndCartHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Bar - Width auto dengan style seperti Tokopedia
        OutlinedTextField(
            value = "",
            onValueChange = { },
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minWidth = 200.dp),
            placeholder = { 
                Text(
                    text = "Cari di Tokopedia", 
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                Text(
                    text = "Cari",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = { /* Perform search */ })
                        .padding(end = 8.dp, start = 8.dp, top = 8.dp, bottom = 8.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFFE5E7EB),
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedTextColor = Color(0xFF1F2937),
                unfocusedTextColor = Color(0xFF1F2937)
            )
        )
        
        // Right Icons: Envelope, Bell, Shopping Cart (outline style seperti di gambar)
        IconButton(
            onClick = { /* Navigate to messages */ },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = "Messages",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(24.dp)
            )
        }
        
        IconButton(
            onClick = { /* Navigate to notifications */ },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(24.dp)
            )
        }
        
        IconButton(
            onClick = { /* Navigate to cart */ },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = "Cart",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun FilterLocationBar(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Filter Button
        OutlinedButton(
            onClick = { /* Open filter */ },
            modifier = Modifier.height(36.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = White,
                contentColor = Black
            )
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size),
                contentDescription = "Filter",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Filter", fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        
        // Location Chips
        listOf("Jabodetabek", "Jakarta Barat", "Jakarta").forEach { location ->
            FilterChip(
                selected = location == "Jabodetabek",
                onClick = { },
                label = { Text(location, fontSize = 12.sp) },
                modifier = Modifier.height(36.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFEFF6FF),
                    selectedLabelColor = Color(0xFF3B82F6),
                    containerColor = Color(0xFFF9FAFB),
                    labelColor = Color(0xFF6B7280)
                )
            )
        }
    }
}

private fun getCurrentMonthYear(): String {
    val months = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    val calendar = java.util.Calendar.getInstance()
    val month = months[calendar.get(java.util.Calendar.MONTH)]
    val year = calendar.get(java.util.Calendar.YEAR)
    return "$month $year"
}

@Composable
private fun ProfileImage(
    profilePhotoUrl: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Box dengan size square dan clip CircleShape untuk memastikan bentuk bulat sempurna
    Box(
        modifier = modifier
            .size(100.dp)  // Pastikan square size (width == height)
            .clip(CircleShape)  // Clip dengan CircleShape
    ) {
        if (!profilePhotoUrl.isNullOrBlank()) {
            // Tampilkan foto profil Google jika ada
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(profilePhotoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile photo",
                modifier = Modifier.fillMaxSize(),  // Isi penuh Box
                contentScale = ContentScale.Crop,  // Crop untuk memastikan gambar mengisi lingkaran sempurna
                placeholder = painterResource(id = com.example.myapplication.R.drawable.logo),
                error = painterResource(id = com.example.myapplication.R.drawable.logo)
            )
        } else {
            // Tampilkan logo jika tidak ada foto profil
            Image(
                painter = painterResource(id = com.example.myapplication.R.drawable.logo),
                contentDescription = "zacode logo",
                modifier = Modifier.fillMaxSize(),  // Isi penuh Box
                contentScale = ContentScale.Crop  // Crop juga untuk logo agar bulat sempurna
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Black,
            fontWeight = FontWeight.Normal
        )
    }
}
