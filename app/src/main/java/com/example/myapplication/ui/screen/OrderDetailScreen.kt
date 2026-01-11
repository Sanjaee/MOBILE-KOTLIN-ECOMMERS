package com.example.myapplication.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.R
import com.example.myapplication.data.model.Order
import com.example.myapplication.data.model.OrderItem
import com.example.myapplication.data.model.OrderShippingAddress
import com.example.myapplication.data.model.PaymentStatus
import com.example.myapplication.data.preferences.PreferencesManager
import com.example.myapplication.data.repository.OrderRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onChatSeller: () -> Unit = {},
    onHelpCenter: () -> Unit = {},
    onViewInvoice: () -> Unit = {},
    onViewAllItems: () -> Unit = {},
    onViewShippingDetail: () -> Unit = {},
    onViewReturnPolicy: () -> Unit = {},
    onBuyAgain: () -> Unit = {}
) {
    val context = LocalContext.current
    val orderRepository = remember { com.example.myapplication.data.repository.OrderRepository() }
    val preferencesManager = remember { com.example.myapplication.data.preferences.PreferencesManager(context) }
    
    var order by remember { mutableStateOf<Order?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(orderId) {
        isLoading = true
        errorMessage = null
        
        val token = preferencesManager.accessToken.first()
        if (token == null) {
            errorMessage = "Session expired. Please login again."
            isLoading = false
            return@LaunchedEffect
        }
        
        orderRepository.getOrder(orderId, token).fold(
            onSuccess = { orderData ->
                order = orderData
                isLoading = false
            },
            onFailure = { exception ->
                errorMessage = exception.message ?: "Failed to load order details"
                isLoading = false
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Pesanan",
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
                    IconButton(onClick = onChatSeller) {
                        Icon(
                            imageVector = Icons.Outlined.Message,
                            contentDescription = "Chat",
                            tint = Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
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
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF10B981))
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Error loading order",
                            fontSize = 14.sp,
                            color = Color(0xFFDC2626),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            Text("Go Back")
                        }
                    }
                }
                order != null -> {
                    OrderDetailContent(
                        order = order!!,
                        onCopyOrderNumber = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Order Number", order!!.orderNumber)
                            clipboard.setPrimaryClip(clip)
                            // Show snackbar
                        },
                        onCopyTrackingNumber = { trackingNumber ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Tracking Number", trackingNumber)
                            clipboard.setPrimaryClip(clip)
                        },
                        onCopyAddress = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val address = order!!.shippingAddress?.getFullAddress() ?: ""
                            val clip = ClipData.newPlainText("Address", address)
                            clipboard.setPrimaryClip(clip)
                        },
                        onViewInvoice = onViewInvoice,
                        onChatSeller = onChatSeller,
                        onHelpCenter = onHelpCenter,
                        onViewAllItems = onViewAllItems,
                        onViewShippingDetail = onViewShippingDetail,
                        onViewReturnPolicy = onViewReturnPolicy,
                        onBuyAgain = onBuyAgain
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Order not found",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
}

// Full implementation with all sections
@Composable
fun OrderDetailContent(
    order: Order,
    onCopyOrderNumber: () -> Unit,
    onCopyTrackingNumber: (String) -> Unit,
    onCopyAddress: () -> Unit,
    onViewInvoice: () -> Unit,
    onChatSeller: () -> Unit,
    onHelpCenter: () -> Unit,
    onViewAllItems: () -> Unit,
    onViewShippingDetail: () -> Unit,
    onViewReturnPolicy: () -> Unit,
    onBuyAgain: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFFF5F5F5))
    ) {
        // Section 1: Pesanan Selesai (Order Completed)
        OrderCompletedSection(
            orderNumber = order.orderNumber,
            purchaseDate = order.createdAt,
            onCopyOrderNumber = onCopyOrderNumber,
            onViewInvoice = onViewInvoice
        )
        
        // Section 2: Detail Produk (Product Details)
        if (order.orderItems.isNotEmpty()) {
            ProductDetailsSection(
                orderItems = order.orderItems,
                hasProtection = order.warrantyCost > 0 || order.insuranceCost > 0,
                totalDiscount = order.totalDiscount,
                onViewAllItems = onViewAllItems,
                onBuyAgain = onBuyAgain
            )
        }
        
        // Section 3: Transaction Protection
        if (order.warrantyCost > 0 || order.insuranceCost > 0) {
            TransactionProtectionSection()
        }
        
        // Section 4: Info Pengiriman (Shipping Info)
        ShippingInfoSection(
            order = order,
            onCopyTrackingNumber = onCopyTrackingNumber,
            onCopyAddress = onCopyAddress,
            onViewShippingDetail = onViewShippingDetail
        )
        
        // Section 5: Gratis Pengembalian (Free Returns)
        FreeReturnsSection(
            onViewReturnPolicy = onViewReturnPolicy
        )
        
        // Section 6: Bantuan (Help)
        HelpSection(
            onChatSeller = onChatSeller,
            onHelpCenter = onHelpCenter
        )
        
        // Section 7: Rincian Pembayaran (Payment Details)
        PaymentDetailsSection(
            order = order
        )
        
        // Section 8: Total Belanja (Total Shopping)
        TotalShoppingSection(
            totalAmount = order.totalAmount
        )
        
        // Bottom spacing for consistency with other screens
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun OrderCompletedSection(
    orderNumber: String,
    purchaseDate: String,
    onCopyOrderNumber: () -> Unit,
    onViewInvoice: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Pesanan Selesai",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No. Pesanan:",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = orderNumber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Black,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = onCopyOrderNumber,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Tanggal Pembelian",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = formatPurchaseDateOrderDetail(purchaseDate),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                }
                
                TextButton(
                    onClick = onViewInvoice,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF10B981)
                    )
                ) {
                    Text(
                        text = "Lihat Invoice",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ProductDetailsSection(
    orderItems: List<OrderItem>,
    hasProtection: Boolean,
    totalDiscount: Int,
    onViewAllItems: () -> Unit,
    onBuyAgain: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detail Produk",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { /* Navigate to seller */ }
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF10B981), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Power Shop",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                    Text(
                        text = "Planet Com...",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Product Item
            orderItems.firstOrNull()?.let { firstItem ->
                ProductDetailItem(
                    orderItem = firstItem,
                    hasProtection = hasProtection,
                    discountAmount = if (orderItems.size == 1) totalDiscount else 0
                )
            }
            
            if (orderItems.size > 1) {
                TextButton(
                    onClick = onViewAllItems,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF10B981)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Lihat Semua Barang",
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductDetailItem(
    orderItem: OrderItem,
    hasProtection: Boolean,
    discountAmount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Product Image
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF3F4F6))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(ApiClient.getImageUrl(orderItem.product?.thumbnail ?: orderItem.product?.images?.firstOrNull()?.imageUrl))
                    .crossfade(true)
                    .build(),
                contentDescription = orderItem.productName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = androidx.compose.ui.res.painterResource(id = R.drawable.logo),
                error = androidx.compose.ui.res.painterResource(id = R.drawable.logo)
            )
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = orderItem.productName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Black,
                maxLines = 2
            )
            
            if (hasProtection) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE0F2FE), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Dilindungi Proteksi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0369A1)
                    )
                }
            }
            
            Text(
                text = "${orderItem.quantity} x ${formatPriceOrderDetail(orderItem.price)}",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
            
            if (discountAmount > 0) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFCE7F3), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Diskon ${formatPriceShortOrderDetail(discountAmount)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFBE185D)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionProtectionSection() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { /* Navigate to policy */ },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Kamu pakai proteksi di transaksi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    Text(
                        text = "Setelah pesanan selesai, kamu bisa cek polis",
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
    }
}

@Composable
fun ShippingInfoSection(
    order: Order,
    onCopyTrackingNumber: (String) -> Unit,
    onCopyAddress: () -> Unit,
    onViewShippingDetail: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Info Pengiriman",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
                TextButton(
                    onClick = onViewShippingDetail,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF10B981)
                    )
                ) {
                    Text(
                        text = "Lihat Detail",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Courier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Kurir",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "Kargo GRATIS ONGKIR",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF10B981)
                )
            }
            
            // Tracking Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No Resi",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "570219370158", // Mock tracking number - should come from order
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    IconButton(
                        onClick = { onCopyTrackingNumber("570219370158") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Address
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alamat",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    IconButton(
                        onClick = onCopyAddress,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                order.shippingAddress?.let { address ->
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            text = "${address.recipientName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = "(${address.phone})",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = address.addressLine1,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = "${address.city} ${address.province} ${address.postalCode}",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FreeReturnsSection(
    onViewReturnPolicy: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Autorenew,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Gratis pengembalian",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black
                )
            }
            Column(
                modifier = Modifier.padding(start = 36.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Kamu bisa kembalikan barang yang rusak atau tidak sesuai deskripsi, tanpa bayar ongkir.",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                TextButton(
                    onClick = onViewReturnPolicy,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF10B981)
                    ),
                    contentPadding = PaddingValues(0.dp) // Remove default padding to reduce spacing
                ) {
                    Text(
                        text = "Lihat Selengkapnya",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HelpSection(
    onChatSeller: () -> Unit,
    onHelpCenter: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Bantuan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onChatSeller),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Message,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Chat Penjual",
                        fontSize = 14.sp,
                        color = Black
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onHelpCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Help,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Pusat Bantuan",
                        fontSize = 14.sp,
                        color = Black
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
    }
}

@Composable
fun PaymentDetailsSection(
    order: Order
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Rincian Pembayaran",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            
            // Payment Method - determine from payment method and bank type
            val paymentMethodText = when (order.payment?.paymentMethod) {
                "bank_transfer" -> {
                    when (order.payment?.bankType?.lowercase()) {
                        "bca" -> "BCA Virtual Account"
                        "bni" -> "BNI Virtual Account"
                        "mandiri" -> "Mandiri Virtual Account"
                        "bri" -> "BRI Virtual Account"
                        else -> "Bank Virtual Account"
                    }
                }
                "gopay" -> "Gopay"
                "qris" -> "QRIS"
                "credit_card" -> "Credit Card"
                "alfamart" -> "Alfamart / Alfamidi / Lawson / Dan+Dan"
                else -> "Bank Transfer"
            }
            
            DetailRow(
                label = "Metode Pembayaran",
                value = paymentMethodText
            )
            
            // Subtotal Harga Barang
            DetailRow(
                label = "Subtotal Harga Barang",
                value = formatPriceOrderDetail(order.subtotal)
            )
            
            // Diskon Barang dari Penjual (if any) - show if discount > 0
            // Note: In production, you might want to separate seller discount vs platform discount
            if (order.totalDiscount > 0) {
                DetailRow(
                    label = "Diskon Barang dari Penjual",
                    value = "-${formatPriceOrderDetail(order.totalDiscount)}",
                    valueColor = Color(0xFF10B981)
                )
                // Platform discount would be shown separately if available
                // DetailRow(
                //     label = "Kupon Diskon Barang dari Platform",
                //     value = "-${formatPrice(platformDiscount)}",
                //     valueColor = Color(0xFF10B981)
                // )
            }
            
            // Total Ongkos Kirim
            DetailRow(
                label = "Total Ongkos Kirim",
                value = formatPriceOrderDetail(order.shippingCost)
            )
            
            // Kupon Diskon Ongkos Kirim (if shipping cost is 0 or discounted)
            // This would need additional field in Order model
            // if (order.shippingCost == 0 || hasShippingDiscount) {
            //     DetailRow(
            //         label = "Kupon Diskon Ongkos Kirim dari Platform",
            //         value = "-${formatPrice(shippingDiscount)}",
            //         valueColor = Color(0xFF10B981)
            //     )
            // }
            
            // Total Proteksi Produk
            if (order.warrantyCost > 0) {
                DetailRow(
                    label = "Total Proteksi Produk",
                    value = formatPriceOrderDetail(order.warrantyCost)
                )
            }
            
            // Asuransi Pengiriman
            if (order.insuranceCost > 0) {
                DetailRow(
                    label = "Asuransi Pengiriman",
                    value = formatPriceOrderDetail(order.insuranceCost)
                )
            }
            
            // Biaya Jasa Aplikasi
            if (order.serviceFee > 0) {
                DetailRow(
                    label = "Biaya Jasa Aplikasi",
                    value = formatPriceOrderDetail(order.serviceFee)
                )
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
fun TotalShoppingSection(
    totalAmount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Belanja",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Text(
                text = formatPriceOrderDetail(totalAmount),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
        }
    }
}

@Composable
fun BuyAgainButton(
    discountAmount: Int,
    onBuyAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box {
            Button(
                onClick = onBuyAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Beli Lagi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
            
            if (discountAmount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = (-8).dp), // offset allows negative values
                    color = Color(0xFFFCE7F3),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Diskon Rp${formatPriceShortOrderDetail(discountAmount)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBE185D),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// Helper functions for OrderDetailScreen
private fun formatPriceOrderDetail(price: Int): String {
    @Suppress("DEPRECATION")
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    val formattedNumber = formatter.format(price)
    return "Rp$formattedNumber"
}

private fun formatPriceShortOrderDetail(price: Int): String {
    // Format short price for discount badge: "Rp50rb" or "50rb"
    return when {
        price >= 1000000 -> {
            val juta = price / 1000000
            val sisa = (price % 1000000) / 100000
            if (sisa > 0) "$juta.${sisa}jt" else "${juta}jt"
        }
        price >= 1000 -> {
            val ribu = price / 1000
            "${ribu}rb"
        }
        else -> price.toString()
    }
}

private fun formatCashbackDisplay(cashback: Int): String {
    // Format cashback like "41.960" (with dot as thousand separator, no currency)
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return formatter.format(cashback)
}

private fun formatPurchaseDateOrderDetail(dateString: String): String {
    return try {
        // Parse ISO format: "2026-01-10T17:29:18.891079Z"
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm WIB", Locale("id", "ID"))
        val date = inputFormat.parse(dateString.substringBefore("."))
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}