package com.example.myapplication.ui.screen

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.R
import com.example.myapplication.data.model.Product
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
    onBuyClick: ((Product) -> Unit)? = null,
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
            TopAppBar(
                title = {
                    Text(
                        text = "",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Navigate to search */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = Black
                        )
                    }
                    IconButton(onClick = { /* Share */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Send,
                            contentDescription = "Share",
                            tint = Black
                        )
                    }
                    Box {
                        IconButton(onClick = { /* Navigate to cart */ }) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = "Cart",
                                tint = Black
                            )
                        }
                        // Cart badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-4).dp)
                                .size(18.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFDC2626)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                    }
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Menu",
                            tint = Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            ProductDetailBottomBar(
                onChatClick = { /* Open chat */ },
                onBuyDirectClick = {
                    product?.let { onBuyClick?.invoke(it) }
                },
                onAddToCartClick = { /* Add to cart */ }
            )
        },
        containerColor = White
    ) { paddingValues ->
        if (uiState.isLoadingDetail && product == null) {
            // Loading State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
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
            }
        } else if (uiState.errorMessage != null && product == null) {
            // Error State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
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
            }
        } else if (product != null) {
            // Calculate discount
            val discountPercent = 5 // 5% discount as per image
            val originalPrice = (product.price * 1.056).toInt() // ~5.6% higher to get 5% discount
            val rating = 4.9f
            val reviewCount = 628
            val photoReviewCount = 74
            val soldCount = 5000
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .background(Color.White)
            ) {
                // Pure Product Image (No overlay, no badge)
                Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .background(Color.White)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(ApiClient.getImageUrl(product.thumbnail ?: product.images?.firstOrNull()?.imageUrl))
                                .crossfade(true)
                                .build(),
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            placeholder = painterResource(id = R.drawable.logo),
                            error = painterResource(id = R.drawable.logo)
                        )
                    }
                    
                // Price and Discount Section
                Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Price Row: Current Price (Large, Black, Bold) and Original Price with Discount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Current Price (Large, Bold, Black)
                            Text(
                                text = formatPriceDetail(product.price),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Black
                            )
                            
                            // Original Price with Discount Percentage
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = formatPriceDetail(originalPrice),
                                    fontSize = 14.sp,
                                    color = Color(0xFF9CA3AF),
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Text(
                                    text = "$discountPercent%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                        
                        // Discount Badge (Pink) - "Diskon Terpakai 100"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFCE7F3))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Diskon Terpakai 100",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFBE185D)
                            )
                        }
                        
                        // Bonus Cashback Row with Icon and Chevron
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Navigate to bonus info */ },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocalOffer,
                                    contentDescription = "Bonus",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Lebih hemat s.d. 10% pakai bonus di checkout",
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Bonus Cashback Badge (Yellow)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFFF8E1))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Bonus Cashback",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                        
                        // Product Title with Heart Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Star Rating
                            Text(
                                text = "★",
                                fontSize = 16.sp,
                                color = Color(0xFFF59E0B)
                            )
                            Text(
                                text = "$rating",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )
                            
                            // Review Count (Blue, Underlined, Clickable)
                            Text(
                                text = "($reviewCount)",
                                fontSize = 14.sp,
                                color = Color(0xFF3B82F6),
                                modifier = Modifier.clickable { /* Navigate to reviews */ },
                                style = TextStyle(
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                            
                            Text(
                                text = "•",
                                fontSize = 14.sp,
                                color = Color(0xFFD1D5DB)
                            )
                            
                            // Photo Reviews (Blue, Underlined, Clickable)
                            Text(
                                text = "$photoReviewCount Foto ulasan",
                                fontSize = 14.sp,
                                color = Color(0xFF3B82F6),
                                modifier = Modifier.clickable { /* Navigate to photo reviews */ },
                                style = TextStyle(
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                            
                            Text(
                                text = "•",
                                fontSize = 14.sp,
                                color = Color(0xFFD1D5DB)
                            )
                            
                            // Sold Count
                            Text(
                                text = formatSoldCountDetail(soldCount),
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        
                        // Shipping Information Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Navigate to shipping details */ },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocalShipping,
                                    contentDescription = "Shipping",
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Ongkir mulai Rp8.000",
                                        fontSize = 14.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                    Text(
                                        text = "Est. tiba besok - 15 Jan",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Return Policy Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Navigate to return policy */ },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Return Policy",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Mudah dikembalikan • Pasti Ori • Pengembalian 6 hari",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
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

@Composable
fun ProductDetailNavBar(
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    cartItemCount: Int = 0
) {
    Surface(
        color = Color(0xFFFFF8E1), // Light yellow/beige background like in image
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Search Bar
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

@Composable
fun ProductDetailBottomBar(
    onChatClick: () -> Unit,
    onBuyDirectClick: () -> Unit,
    onAddToCartClick: () -> Unit
) {
    Surface(
        color = White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chat Icon Button (Outline dengan border)
            OutlinedButton(
                onClick = onChatClick,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFD1D5DB)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Chat,
                    contentDescription = "Chat",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // "Beli Langsung" Button (Outline Green)
            OutlinedButton(
                onClick = onBuyDirectClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981)), // Green border
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF10B981) // Green text
                )
            ) {
                Text(
                    text = "Beli Langsung",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // "+ Keranjang" Button (Full Green Background)
            Button(
                onClick = onAddToCartClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981), // Green background
                    contentColor = White // White text
                )
            ) {
                Text(
                    text = "+ Keranjang",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatPriceDetail(price: Int): String {
    @Suppress("DEPRECATION")
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    val formattedNumber = formatter.format(price)
    return "Rp$formattedNumber"
}

private fun formatSoldCountDetail(soldCount: Int): String {
    return when {
        soldCount >= 1000000 -> {
            val juta = soldCount / 1000000
            val sisaJuta = (soldCount % 1000000) / 100000
            if (sisaJuta > 0) "${juta},${sisaJuta}jt+ terjual" else "${juta}jt+ terjual"
        }
        soldCount >= 100000 -> {
            val ratusRb = soldCount / 100000
            "${ratusRb}00rb+ terjual"
        }
        soldCount >= 10000 -> {
            val puluhRb = soldCount / 10000
            "${puluhRb}0rb+ terjual"
        }
        soldCount >= 1000 -> {
            val ribu = soldCount / 1000
            "${ribu} rb+ terjual" // Format: "5 rb+ terjual"
        }
        else -> {
            "1 rb+ terjual"
        }
    }
}
