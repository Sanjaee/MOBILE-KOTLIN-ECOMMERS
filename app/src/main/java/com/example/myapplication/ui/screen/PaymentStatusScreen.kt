package com.example.myapplication.ui.screen

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.product.ProductCard
import com.example.myapplication.ui.components.product.ProductListSection
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.PaymentViewModel
import com.example.myapplication.ui.viewmodel.ProductViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentStatusScreen(
    paymentId: String? = null,
    orderId: String? = null,
    onBack: () -> Unit,
    onHome: () -> Unit,
    paymentViewModel: PaymentViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    productViewModel: ProductViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by paymentViewModel.uiState.collectAsState()
    val productUiState by productViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var countdown by remember { mutableStateOf(uiState.countdownSeconds) }
    
    LaunchedEffect(paymentId, orderId) {
        when {
            paymentId != null -> paymentViewModel.loadPayment(paymentId)
            orderId != null -> {
                // Load payment by order ID if needed
            }
        }
    }
    
    LaunchedEffect(uiState.countdownSeconds) {
        countdown = uiState.countdownSeconds
    }
    
    // Countdown timer
    LaunchedEffect(countdown) {
        while (countdown > 0 && uiState.payment?.status == com.example.myapplication.data.model.PaymentStatus.PENDING) {
            delay(1000)
            countdown--
            if (countdown < 0) countdown = 0
        }
    }
    
    // Load products for recommendations
    LaunchedEffect(Unit) {
        productViewModel.loadProducts(page = 1, limit = 10, activeOnly = true)
    }
    
    val payment = uiState.payment
    
    Scaffold(
        topBar = {
            PaymentStatusTopBar(
                onBack = onBack,
                onSearchClick = { /* Navigate to search */ },
                onCartClick = { /* Navigate to cart */ },
                onMenuClick = { /* Open menu */ }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (uiState.isLoading && payment == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF10B981))
                    }
                }
            } else if (payment == null) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Payment not found",
                            fontSize = 16.sp,
                            color = Color(0xFF6B7280)
                        )
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
            } else {
                // Payment Card
                item {
                    PaymentDetailsCard(
                        payment = payment,
                        countdownSeconds = countdown,
                        onCopyVANumber = { vaNumber ->
                            copyToClipboard(context, vaNumber, "VA Number")
                        },
                        onCopyTotal = { total ->
                            copyToClipboard(context, formatPrice(total), "Total Bill")
                        },
                        onSeePaymentGuide = { /* Navigate to payment guide */ },
                        onCheckStatus = {
                            payment.id?.let { paymentViewModel.checkPaymentStatus(it) }
                        }
                    )
                }
                
                // Recommendations
                if (productUiState.products.isNotEmpty()) {
                    item {
                        Text(
                            text = "Rekomendasi untuk Anda",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                    }
                    
                    item {
                        ProductListSection(
                            title = "Rekomendasi untuk Anda",
                            products = productUiState.products.take(4),
                            isHorizontal = false,
                            onProductClick = { /* Handle product click */ }
                        )
                    }
                }
            }
        }
    }
    
    // Handle payment success
    LaunchedEffect(payment?.status) {
        if (payment?.status == com.example.myapplication.data.model.PaymentStatus.SUCCESS) {
            // Payment successful - could show success dialog or navigate
            paymentViewModel.stopPolling()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentStatusTopBar(
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = { Text("") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Black
                )
            }
        },
        actions = {
            // Search bar
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(onClick = onSearchClick)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Cari di Tokopedia",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
                TextButton(onClick = onSearchClick) {
                    Text(
                        text = "Cari",
                        fontSize = 12.sp,
                        color = Color(0xFF10B981)
                    )
                }
            }
            
            IconButton(onClick = { /* Navigate to messages */ }) {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = "Messages",
                    tint = Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            IconButton(onClick = { /* Navigate to notifications */ }) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Box {
                IconButton(onClick = onCartClick) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = "Cart",
                        tint = Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Cart badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp),
                    shape = CircleShape,
                    color = Color(0xFFEF4444)
                ) {
                    Text(
                        text = "1",
                        fontSize = 10.sp,
                        color = White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxSize(),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = "Menu",
                    tint = Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = White,
            titleContentColor = Black
        )
    )
}

@Composable
private fun PaymentDetailsCard(
    payment: com.example.myapplication.data.model.Payment,
    countdownSeconds: Long,
    onCopyVANumber: (String) -> Unit,
    onCopyTotal: (Int) -> Unit,
    onSeePaymentGuide: () -> Unit,
    onCheckStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Payment Deadline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color(0xFFFFF9C4)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "L",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Bayar sebelum",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = formatExpiryDate(payment.expiryTime),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Black
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = "Timer",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = formatCountdown(countdownSeconds),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            }
            
            Divider(color = Color(0xFFE5E7EB))
            
            // Savings Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFECFDF5)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalOffer,
                        contentDescription = "Savings",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Kamu hemat Rp1.700 dari transaksi ini",
                        fontSize = 13.sp,
                        color = Color(0xFF065F46),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Virtual Account Number
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Nomor Virtual Account",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = payment.vaNumber ?: "N/A",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black
                        )
                        IconButton(
                            onClick = { payment.vaNumber?.let { onCopyVANumber(it) } },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    // Bank Logo (BCA)
                    if (payment.bankType?.lowercase() == "bca") {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1E3A8A)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "BCA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                            }
                        }
                    }
                }
            }
            
            Divider(color = Color(0xFFE5E7EB))
            
            // Total Bill
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Total Tagihan",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = formatPrice(payment.totalAmount),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black
                        )
                        IconButton(
                            onClick = { onCopyTotal(payment.totalAmount) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    TextButton(onClick = { /* Show detail */ }) {
                        Text(
                            text = "Lihat Detail",
                            fontSize = 12.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
            
            // QR Code Section (for Gopay/QRIS) - show after VA number if available
            if (payment.qrCodeUrl != null && !payment.qrCodeUrl.isNullOrEmpty()) {
                Divider(color = Color(0xFFE5E7EB))
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Scan QR Code untuk Bayar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    // QR Code would be displayed here using Coil or QR code library
                    // For now, placeholder - in production, use QR code library to generate from URL
                    Surface(
                        modifier = Modifier.size(200.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "QR Code",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
            
            Divider(color = Color(0xFFE5E7EB))
            
            // Important Notes
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (payment.bankType != null && payment.vaNumber != null) {
                    Text(
                        text = "Penting: Transfer Virtual Account hanya bisa dilakukan dari bank yang kamu pilih",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 18.sp
                    )
                }
                Text(
                    text = "Transaksi kamu baru akan diteruskan ke penjual setelah pembayaran berhasil diverifikasi.",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 18.sp
                )
            }
            
            Divider(color = Color(0xFFE5E7EB))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSeePaymentGuide,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF10B981)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color(0xFF10B981)
                    )
                ) {
                    Text(
                        text = "Lihat Cara Bayar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                OutlinedButton(
                    onClick = onCheckStatus,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF10B981)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color(0xFF10B981)
                    )
                ) {
                    Text(
                        text = "Cek Status Bayar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun formatExpiryDate(expiryTime: String?): String {
    if (expiryTime == null) return "N/A"
    
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = formatter.parse(expiryTime)
        val displayFormat = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID"))
        displayFormat.format(date ?: Date())
    } catch (e: Exception) {
        expiryTime
    }
}

private fun formatCountdown(seconds: Long): String {
    val hours = TimeUnit.SECONDS.toHours(seconds)
    val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

private fun formatPrice(amount: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(amount).replace("Rp", "Rp").replace(",00", "")
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}
