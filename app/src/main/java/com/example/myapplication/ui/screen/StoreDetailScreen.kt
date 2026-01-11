package com.example.myapplication.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.R
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.SellerViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    sellerId: String? = null, // If null, load current user's store
    onBack: () -> Unit,
    onAddProductClick: () -> Unit = {},
    viewModel: SellerViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val seller = uiState.seller
    val context = LocalContext.current
    
    LaunchedEffect(sellerId) {
        if (sellerId != null) {
            viewModel.getSeller(sellerId)
        } else {
            viewModel.getMySeller()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Toko",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        if (uiState.isLoading && seller == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF10B981))
            }
        } else if (seller != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Store Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFFE5E7EB))
                ) {
                    if (seller.shopBanner != null && seller.shopBanner.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(ApiClient.getImageUrl(seller.shopBanner))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Store Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Store,
                                contentDescription = "Store Banner",
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
                
                // Store Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Store Logo and Name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Store Logo
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = CircleShape,
                                color = Color(0xFFE5E7EB)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (seller.shopLogo != null && seller.shopLogo.isNotEmpty()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(ApiClient.getImageUrl(seller.shopLogo))
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Store Logo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.Store,
                                            contentDescription = "Store Logo",
                                            modifier = Modifier.size(40.dp),
                                            tint = Color(0xFF6B7280)
                                        )
                                    }
                                }
                            }
                            
                            // Store Name and Verification
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = seller.shopName,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Black
                                    )
                                    
                                    if (seller.isVerified) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Verified",
                                            modifier = Modifier.size(20.dp),
                                            tint = Color(0xFF8B5CF6)
                                        )
                                    }
                                }
                                
                                // Rating and Sales
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Star,
                                            contentDescription = "Rating",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFFFFB800)
                                        )
                                        Text(
                                            text = DecimalFormat("#.#").format(seller.ratingAverage),
                                            fontSize = 14.sp,
                                            color = Black,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "(${formatNumber(seller.totalReviews)})",
                                            fontSize = 14.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                    
                                    Text(
                                        text = "•",
                                        fontSize = 14.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                    
                                    Text(
                                        text = "${formatNumber(seller.totalSales)} terjual",
                                        fontSize = 14.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                        }
                        
                        Divider(color = Color(0xFFE5E7EB))
                        
                        // Store Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StoreStatItem(
                                label = "Produk",
                                value = seller.totalProducts.toString()
                            )
                            StoreStatItem(
                                label = "Penjualan",
                                value = formatNumber(seller.totalSales)
                            )
                            StoreStatItem(
                                label = "Rating",
                                value = DecimalFormat("#.#").format(seller.ratingAverage)
                            )
                        }
                        
                        // Store Description
                        if (!seller.shopDescription.isNullOrBlank()) {
                            Divider(color = Color(0xFFE5E7EB))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Tentang Toko",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Black
                                )
                                Text(
                                    text = seller.shopDescription,
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                        
                        // Store Contact Info
                        if (!seller.shopPhone.isNullOrBlank() || !seller.shopEmail.isNullOrBlank()) {
                            Divider(color = Color(0xFFE5E7EB))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Kontak",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Black
                                )
                                
                                if (!seller.shopPhone.isNullOrBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Phone,
                                            contentDescription = "Phone",
                                            modifier = Modifier.size(20.dp),
                                            tint = Color(0xFF6B7280)
                                        )
                                        Text(
                                            text = seller.shopPhone,
                                            fontSize = 14.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }
                                
                                if (!seller.shopEmail.isNullOrBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Email,
                                            contentDescription = "Email",
                                            modifier = Modifier.size(20.dp),
                                            tint = Color(0xFF6B7280)
                                        )
                                        Text(
                                            text = seller.shopEmail,
                                            fontSize = 14.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Store Address
                        if (!seller.shopAddress.isNullOrBlank() || !seller.shopCity.isNullOrBlank() || !seller.shopProvince.isNullOrBlank()) {
                            Divider(color = Color(0xFFE5E7EB))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Alamat",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Black
                                )
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = "Address",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFF6B7280)
                                    )
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (!seller.shopAddress.isNullOrBlank()) {
                                            Text(
                                                text = seller.shopAddress,
                                                fontSize = 14.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                        val locationParts = listOfNotNull(
                                            seller.shopCity,
                                            seller.shopProvince
                                        )
                                        if (locationParts.isNotEmpty()) {
                                            Text(
                                                text = locationParts.joinToString(", "),
                                                fontSize = 14.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Store Status
                        Divider(color = Color(0xFFE5E7EB))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Black
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (seller.isActive) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = if (seller.isActive) "Aktif" else "Tidak Aktif",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (seller.isActive) Color(0xFF10B981) else Color(0xFFEF4444),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                
                                if (seller.isVerified) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF8B5CF6).copy(alpha = 0.1f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Verified",
                                                modifier = Modifier.size(16.dp),
                                                tint = Color(0xFF8B5CF6)
                                            )
                                            Text(
                                                text = "Terverifikasi",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF8B5CF6)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Add Product Button (only show for store owner)
                if (sellerId == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable(onClick = onAddProductClick),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Add Product",
                                modifier = Modifier.size(24.dp),
                                tint = White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Tambah Produk",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            val errorMessage = uiState.errorMessage
            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            fontSize = 16.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreStatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
    }
}

private fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> {
            val millions = number / 1_000_000.0
            String.format("%.1f jt", millions).replace(".0", "")
        }
        number >= 1_000 -> {
            val thousands = number / 1_000.0
            String.format("%.1f rb", thousands).replace(".0", "")
        }
        else -> number.toString()
    }
}