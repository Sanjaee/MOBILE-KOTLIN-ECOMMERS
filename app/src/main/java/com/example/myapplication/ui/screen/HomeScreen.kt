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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
    
    // Track refresh state separately to avoid conflicts with initial loading
    var isRefreshing by remember { mutableStateOf(false) }
    
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    
    // Load products on first render
    LaunchedEffect(Unit) {
        productViewModel.loadProducts(page = 1, limit = 10, activeOnly = true)
    }
    
    // Handle refresh - end when loading completes
    LaunchedEffect(productUiState.isLoading) {
        if (isRefreshing && !productUiState.isLoading) {
            isRefreshing = false
        }
    }
    
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            isRefreshing = true
            productViewModel.loadProducts(page = 1, limit = 10, activeOnly = true)
        },
        modifier = Modifier.fillMaxSize()
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
                // Search Bar & Cart Header (Navbar style)
                item {
                    HomeNavBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
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
private fun HomeNavBar(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = { /* Perform search */ },
    onCartClick: () -> Unit = { /* Navigate to cart */ },
    cartItemCount: Int = 0
) {
    Surface(
        color = White, // White background
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Bar (tanpa tombol back)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .drawBehind {
                        drawRect(
                            color = Color(0xFFD1D5DB),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                    .clickable(onClick = onSearchClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Cari di Tokopedia",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    TextButton(
                        onClick = onSearchClick,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Black
                        )
                    ) {
                        Text(
                            text = "Cari",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Shopping Cart with Badge
            Box(modifier = Modifier.size(40.dp)) {
                IconButton(
                    onClick = onCartClick,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = "Cart",
                        tint = Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                if (cartItemCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-4).dp)
                            .size(18.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFEC4899)), // Pink-red badge color
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cartItemCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                }
            }
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
