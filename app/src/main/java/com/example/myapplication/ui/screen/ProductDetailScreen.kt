package com.example.myapplication.ui.screen

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.ProductViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: ProductViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val product = uiState.productDetail
    var isFavorite by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    LaunchedEffect(productId) {
        viewModel.loadProductById(productId)
    }
    
    // Handle token expired
    LaunchedEffect(uiState.isTokenExpired) {
        if (uiState.isTokenExpired) {
            onLogout()
        }
    }
    
    Scaffold(
        topBar = {
            ProductDetailTopBar(
                onBack = onBack,
                onSearchClick = { /* Navigate to search */ },
                onShareClick = { /* Share product */ },
                onCartClick = { /* Navigate to cart */ },
                onMenuClick = { /* Open menu */ }
            )
        },
        bottomBar = {
            ProductDetailBottomBar(
                onChatClick = { /* Open chat */ },
                onCallClick = { /* Make call */ },
                onAppBenefitClick = { /* Show app benefit */ }
            )
        },
        containerColor = White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoadingDetail && product == null) {
                // Loading State
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading product...",
                        color = Color(0xFF6B7280),
                        fontSize = 16.sp
                    )
                }
            } else if (uiState.errorMessage != null && product == null) {
                // Error State
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error loading product",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadProductById(productId) }
                    ) {
                        Text("Retry")
                    }
                }
            } else if (product != null) {
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Product Image Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .background(Color.White)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(product.thumbnail ?: product.images?.firstOrNull()?.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.logo),
                            error = painterResource(id = R.drawable.logo)
                        )
                    }
                    
                    // Warranty Text
                    Text(
                        text = "Garansi Resmi Indonesia",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                    
                    // Price Section
                    Text(
                        text = formatPrice(product.price),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    // Discount/Bonus Information
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalOffer,
                            contentDescription = "Discount",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Lebih hemat s.d. 1% pakai bonus di checkout",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color(0xFFE5E7EB),
                        thickness = 1.dp
                    )
                    
                    // Product Title with Heart Icon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = product.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Black,
                            modifier = Modifier.weight(1f),
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = { isFavorite = !isFavorite },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFFDC2626) else Color(0xFF6B7280),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    // Rating and Reviews Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "★",
                                fontSize = 16.sp,
                                color = Color(0xFFF59E0B)
                            )
                            Text(
                                text = "5.0 (107)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )
                        }
                        
                        Text(
                            text = "•",
                            fontSize = 13.sp,
                            color = Color(0xFFD1D5DB)
                        )
                        
                        Text(
                            text = "70 Foto ulasan",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                        
                        Text(
                            text = "•",
                            fontSize = 13.sp,
                            color = Color(0xFFD1D5DB)
                        )
                        
                        Text(
                            text = "250+ Terjual",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        color = Color(0xFFE5E7EB),
                        thickness = 1.dp
                    )
                    
                    // Product Description
                    if (!product.description.isNullOrBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Deskripsi Produk",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = product.description ?: "",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                lineHeight = 20.sp
                            )
                        }
                    }
                    
                    // Category Info
                    product.category?.let { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Kategori:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = category.name,
                                fontSize = 14.sp,
                                color = Black
                            )
                        }
                    }
                    
                    // Stock Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Stok:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = if (product.stock > 0) "${product.stock} tersedia" else "Stok habis",
                            fontSize = 14.sp,
                            color = if (product.stock > 0) Color(0xFF10B981) else Color(0xFFDC2626)
                        )
                    }
                    
                    // Bottom spacing for bottom bar
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductDetailTopBar(
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onShareClick: () -> Unit,
    onCartClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Surface(
        color = White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Logo "Universe Your Gadget Store"
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "Universe",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        letterSpacing = 0.3.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFDC2626))
                    )
                }
                Text(
                    text = "Your Gadget Store",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280),
                    letterSpacing = 0.2.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
            
            // Right Icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onSearchClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onShareClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onCartClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = "Cart",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onMenuClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "Menu",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductDetailBottomBar(
    onChatClick: () -> Unit,
    onCallClick: () -> Unit,
    onAppBenefitClick: () -> Unit
) {
    Surface(
        color = White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chat Icon
            IconButton(
                onClick = onChatClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Chat,
                    contentDescription = "Chat",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Bell Langsung Button
            Button(
                onClick = onCallClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                )
            ) {
                Text(
                    text = "Bell Langsung",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
            
            // Untung pakai App Button
            Button(
                onClick = onAppBenefitClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                )
            ) {
                Text(
                    text = "Untung pakai App",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
    }
}

private fun formatPrice(price: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(price)
}
