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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
    onOrderDetail: (String) -> Unit = {}, // Navigate to order detail when payment success
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
    
    // Load payment when screen opens
    LaunchedEffect(paymentId, orderId) {
        when {
            !paymentId.isNullOrEmpty() -> {
                paymentViewModel.loadPayment(paymentId!!)
            }
            !orderId.isNullOrEmpty() -> {
                // Load payment by order ID if needed
                // For now, paymentId should always be provided
            }
        }
    }
    
    // Navigate to OrderDetailScreen when payment is successful
    var hasNavigated by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.payment?.status) {
        val paymentStatus = uiState.payment?.status
        if (paymentStatus == com.example.myapplication.data.model.PaymentStatus.SUCCESS && !hasNavigated) {
            paymentViewModel.stopPolling()
            // Navigate to order detail immediately when payment is successful
            // Use order_uuid from payment (this is the order ID in UUID format)
            val orderId = uiState.payment?.orderUuid
            if (!orderId.isNullOrEmpty()) {
                hasNavigated = true
                onOrderDetail(orderId)
            } else {
                // Fallback: try to get from order object if available
                uiState.payment?.order?.id?.let { orderIdFromOrder ->
                    if (orderIdFromOrder.isNotEmpty()) {
                        hasNavigated = true
                        onOrderDetail(orderIdFromOrder)
                    }
                }
            }
        } else if (paymentStatus != com.example.myapplication.data.model.PaymentStatus.PENDING) {
            paymentViewModel.stopPolling()
        }
    }
    
    // Reset navigation flag when payment changes
    LaunchedEffect(uiState.payment?.id) {
        hasNavigated = false
    }
    
    // Stop polling when screen is disposed (user leaves the screen)
    DisposableEffect(Unit) {
        onDispose {
            paymentViewModel.stopPolling()
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
            TopAppBar(
                title = {
                    Text(
                        text = "Payment Status",
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
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
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
            
            // Shopping Cart
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Baris 1: Icon + "Bayar sebelum" | Timer
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
                        Text(
                            text = "Bayar sebelum",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
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
                
                // Baris 2: Tanggal (kiri saja)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(52.dp)) // Spacer untuk align dengan "Bayar sebelum"
                    Text(
                        text = formatExpiryDate(payment.expiryTime),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                }
            }
            
            HorizontalDivider(color = Color(0xFFE5E7EB))
            
            // Virtual Account Number (only for bank_transfer payment method)
            val isBankTransfer = payment.paymentMethod?.lowercase() == "bank_transfer" || 
                                 payment.paymentMethod?.lowercase() == "virtual_account"
            
            if (isBankTransfer && payment.vaNumber != null) {
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
                                fontSize = 12.sp,
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
                
                HorizontalDivider(color = Color(0xFFE5E7EB))
            }
            
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
            
            // QR Code Section (for QRIS) - show after Total Bill
            val isQRIS = payment.paymentMethod?.lowercase() == "qris"
            if (isQRIS && payment.qrCodeUrl != null && !payment.qrCodeUrl.isNullOrEmpty()) {
                HorizontalDivider(color = Color(0xFFE5E7EB))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Scan QR Code untuk Bayar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    
                    // Display QR Code from URL using Coil
                    val context = LocalContext.current
                    Surface(
                        modifier = Modifier.size(280.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(payment.qrCodeUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    
                    // Action Buttons below QR Code
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Download QRIS */ },
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
                                text = "Download QRIS",
                                fontSize = 11.sp,
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
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            HorizontalDivider(color = Color(0xFFE5E7EB))
            
            // Important Notes
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isBankTransfer && payment.bankType != null && payment.vaNumber != null) {
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
            
            // QRIS Instructions (only for QRIS)
            if (isQRIS) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFE5E7EB)
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Panduan Pembayaran QRIS:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QRISInstructionItem("Download atau screenshot QRIS.")
                        QRISInstructionItem("Buka aplikasi bank atau e-wallet yang mendukung pembayaran QRIS di HP-mu.")
                        QRISInstructionItem("Scan atau upload QR code di atas.")
                        QRISInstructionItem("Pastikan total tagihan sudah benar, lalu lanjutkan proses pembayaran.")
                        QRISInstructionItem("Setelah berhasil, kamu bisa cek status bayar untuk konfirmasi pembayaranmu.")
                    }
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
    @Suppress("DEPRECATION")
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    val formattedNumber = formatter.format(amount)
    return "Rp$formattedNumber"
}

@Composable
private fun QRISInstructionItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF6B7280),
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}
