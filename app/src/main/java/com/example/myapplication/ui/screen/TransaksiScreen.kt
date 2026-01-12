package com.example.myapplication.ui.screen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.R
import com.example.myapplication.data.model.Order
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.OrderViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaksiScreen(
    onOrderClick: (String) -> Unit = {},
    onPaymentClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    orderViewModel: OrderViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by orderViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        orderViewModel.loadOrders()
    }
    
    Scaffold(
        topBar = {
            TransaksiTopBar(
                onSearchClick = onSearchClick,
                onCartClick = onCartClick,
                onMenuClick = onMenuClick
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.orders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF10B981))
                    }
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Error loading orders",
                            fontSize = 14.sp,
                            color = Color(0xFFDC2626),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                uiState.orders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Belum ada transaksi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.orders) { order ->
                            OrderTransactionCard(
                                order = order,
                                onClick = { onOrderClick(order.id) },
                                onPaymentClick = { paymentId -> onPaymentClick(paymentId) },
                                onBuyAgainClick = { /* Handle buy again */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransaksiTopBar(
    onSearchClick: () -> Unit,
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF3F4F6))
                    .clickable(onClick = onSearchClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Cari transaksi",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF)
                    )
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
                // Badge (optional - can be added if needed)
            }
            
            // Menu
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "Menu",
                    tint = Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun OrderTransactionCard(
    order: Order,
    onClick: () -> Unit,
    onPaymentClick: (String) -> Unit,
    onBuyAgainClick: () -> Unit
) {
    val context = LocalContext.current
    val firstItem = order.orderItems.firstOrNull()
    val isPending = order.status.lowercase() == "pending"
    val paymentId = order.payment?.id
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Belanja, Date, Status, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = "Belanja",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Belanja",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    Text(
                        text = formatTransactionDate(order.createdAt),
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    StatusBadge(status = order.status)
                    IconButton(
                        onClick = { /* Show menu */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Menu",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Product Info
            if (firstItem != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Product Image
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(ApiClient.getImageUrl(firstItem.product?.thumbnail ?: firstItem.product?.images?.firstOrNull()?.imageUrl))
                                .crossfade(true)
                                .build(),
                            contentDescription = firstItem.productName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.logo),
                            error = painterResource(id = R.drawable.logo)
                        )
                    }
                    
                    // Product Details
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = firstItem.productName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${firstItem.quantity} barang",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
            
            HorizontalDivider(color = Color(0xFFE5E7EB))
            
            // Price and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Total Belanja",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatPrice(order.totalAmount),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black
                        )
                        if (order.totalDiscount > 0) {
                            DiscountBadge(discount = order.totalDiscount)
                        }
                    }
                }
                
                Button(
                    onClick = {
                        if (isPending && paymentId != null) {
                            onPaymentClick(paymentId)
                        } else {
                            onBuyAgainClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isPending) "Bayar" else "Beli Lagi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = White
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (text, color) = when (status.lowercase()) {
        "delivered", "selesai" -> "Selesai" to Color(0xFF10B981)
        "pending" -> "Pending" to Color(0xFFF59E0B)
        "processing" -> "Processing" to Color(0xFF3B82F6)
        "shipped" -> "Dikirim" to Color(0xFF8B5CF6)
        "cancelled" -> "Dibatalkan" to Color(0xFFDC2626)
        else -> status to Color(0xFF6B7280)
    }
    
    Surface(
        color = color,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun DiscountBadge(discount: Int) {
    Surface(
        color = Color(0xFFFCE7F3),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "Diskon ${formatDiscountPrice(discount)}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFBE185D),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun formatPrice(price: Int): String {
    @Suppress("DEPRECATION")
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    val formattedNumber = formatter.format(price)
    return "Rp$formattedNumber"
}

private fun formatDiscountPrice(price: Int): String {
    return when {
        price >= 1000000 -> {
            val juta = price / 1000000
            val sisa = (price % 1000000) / 100000
            if (sisa > 0) "Rp$juta.${sisa}jt" else "Rp${juta}jt"
        }
        price >= 1000 -> {
            val ribu = price / 1000
            val ratus = (price % 1000) / 100
            if (ratus >= 50) "Rp$ribu.${ratus}rb" else "Rp${ribu}rb"
        }
        else -> "Rp${price}"
    }
}

private fun formatTransactionDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val date = inputFormat.parse(dateString.substringBefore("."))
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}
