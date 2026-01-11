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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.R
import com.example.myapplication.data.model.Product
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.CheckoutViewModel
import com.example.myapplication.ui.viewmodel.PaymentViewModel
import com.example.myapplication.ui.viewmodel.ProductViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutPage1Screen(
    productId: String,
    quantity: Int = 1,
    onBack: () -> Unit,
    onPayClick: (String) -> Unit, // Changed to pass paymentId
    onAddressClick: () -> Unit = {},
    onShippingMethodClick: () -> Unit = {},
    onNoteClick: () -> Unit = {},
    onBonusClick: () -> Unit = {},
    onSeeAllPaymentClick: () -> Unit = {},
    checkoutViewModel: CheckoutViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    productViewModel: ProductViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    paymentViewModel: PaymentViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val checkoutUiState by checkoutViewModel.uiState.collectAsState()
    val productUiState by productViewModel.uiState.collectAsState()
    val paymentUiState by paymentViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(productId) {
        productViewModel.loadProductById(productId)
    }
    
    LaunchedEffect(productUiState.productDetail) {
        productUiState.productDetail?.let { product ->
            checkoutViewModel.initializeCheckout(product, quantity)
        }
    }
    
    // Navigate to PaymentStatusScreen when payment is created successfully (only once)
    var hasNavigated by remember { mutableStateOf(false) }
    
    LaunchedEffect(paymentUiState.payment, paymentUiState.isLoading) {
        paymentUiState.payment?.let { payment ->
            if (!paymentUiState.isLoading && payment.id.isNotEmpty() && !hasNavigated) {
                hasNavigated = true
                onPayClick(payment.id)
            }
        }
    }
    
    // Reset navigation flag when loading starts
    LaunchedEffect(paymentUiState.isLoading) {
        if (paymentUiState.isLoading) {
            hasNavigated = false
        }
    }
    
    val product = productUiState.productDetail
    
    if (productUiState.isLoadingDetail && product == null) {
        // Loading State
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Black)
        }
    } else if (product == null) {
        // Error or No Product State
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Product not found",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFDC2626)
                )
                Button(onClick = onBack) {
                    Text("Go Back")
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Checkout",
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
            bottomBar = {
                val order = checkoutUiState.orders.firstOrNull()
                val selectedPaymentMethod = checkoutUiState.selectedPaymentMethod
                val selectedAddress = checkoutUiState.selectedShippingAddress
                val canProceed = order != null && selectedPaymentMethod != null && selectedAddress != null
                
                CheckoutBottomBar(
                    total = checkoutUiState.summary?.total ?: 0,
                    isLoading = paymentUiState.isLoading,
                    enabled = canProceed && !paymentUiState.isLoading,
                    onPayClick = {
                        // Create order and payment
                        // Validasi sudah dilakukan di enabled state, tapi double check untuk safety
                        val validOrder = order ?: return@CheckoutBottomBar
                        val validPaymentMethod = selectedPaymentMethod ?: return@CheckoutBottomBar
                        val validAddress = selectedAddress ?: return@CheckoutBottomBar
                        
                        // Map payment method type to backend format
                        val paymentMethodType = when (validPaymentMethod.type) {
                            "virtual_account" -> {
                                // Extract bank from payment method name (e.g., "BCA Virtual Account" -> "bca")
                                when {
                                    validPaymentMethod.name.contains("BCA", ignoreCase = true) -> "bca"
                                    validPaymentMethod.name.contains("BNI", ignoreCase = true) -> "bni"
                                    validPaymentMethod.name.contains("Mandiri", ignoreCase = true) -> "mandiri"
                                    validPaymentMethod.name.contains("BRI", ignoreCase = true) -> "bri"
                                    else -> "bca"
                                }
                            }
                            else -> null
                        }
                        
                        // Create order and payment
                        paymentViewModel.createOrderAndPayment(
                            checkoutOrder = validOrder,
                            shippingAddressId = validAddress.id,
                            paymentMethod = validPaymentMethod.type,
                            bank = paymentMethodType
                        )
                    }
                )
            },
            containerColor = White
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Shipping Address Section
                if (checkoutUiState.selectedShippingAddress != null) {
                    ShippingAddressSection(
                        address = checkoutUiState.selectedShippingAddress!!,
                        onClick = onAddressClick
                    )
                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                }
                
                // Order Details Section
                checkoutUiState.orders.forEachIndexed { index, order ->
                    OrderDetailsSection(
                        orderNumber = index + 1,
                        order = order,
                        useWarrantyProtection = checkoutUiState.useWarrantyProtection,
                        onWarrantyToggle = { enabled ->
                            checkoutViewModel.toggleWarrantyProtection(enabled)
                        },
                        onQuantityChange = { itemIndex, newQuantity ->
                            checkoutViewModel.updateQuantity(order.id, itemIndex, newQuantity)
                        }
                    )
                }
                
                // Shipping Method Section
                if (checkoutUiState.selectedShippingMethod != null) {
                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                    ShippingMethodSection(
                        shippingMethod = checkoutUiState.selectedShippingMethod!!,
                        useInsurance = checkoutUiState.useInsurance,
                        onShippingMethodClick = onShippingMethodClick,
                        onInsuranceToggle = { enabled ->
                            checkoutViewModel.toggleInsurance(enabled)
                        }
                    )
                }
                
                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                
                // Note Section
                NoteSection(
                    note = checkoutUiState.note ?: "",
                    onClick = onNoteClick
                )
                
                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                
                // Bonus Section
                if (checkoutUiState.bonus > 0) {
                    BonusSection(
                        bonus = checkoutUiState.bonus,
                        onClick = onBonusClick
                    )
                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                }
                
                // Payment Methods Section
                PaymentMethodsSection(
                    paymentMethods = checkoutUiState.paymentMethods,
                    selectedPaymentMethod = checkoutUiState.selectedPaymentMethod,
                    onPaymentMethodSelect = { method ->
                        checkoutViewModel.selectPaymentMethod(method)
                    },
                    onSeeAllClick = onSeeAllPaymentClick
                )
                
                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                
                // Detailed Payment Summary Section
                if (checkoutUiState.summary != null) {
                    DetailedPaymentSummarySection(summary = checkoutUiState.summary!!)
                }
                
                // Bottom spacing for bottom bar
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun ShippingAddressSection(
    address: com.example.myapplication.data.model.ShippingAddress,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Alamat pengiriman kamu",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${address.label} • ${address.recipientName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    Text(
                        text = "${address.getFullAddress()} (${address.phone})",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 2
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Edit",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun OrderDetailsSection(
    orderNumber: Int,
    order: com.example.myapplication.data.model.CheckoutOrder,
    useWarrantyProtection: Boolean,
    onWarrantyToggle: (Boolean) -> Unit,
    onQuantityChange: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Order Header
        Text(
            text = "PESANAN $orderNumber",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )
        
        // Seller Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = "Verified",
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = order.sellerName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Black
            )
        }
        
        // Product Items
        order.items.forEachIndexed { itemIndex, item ->
            OrderItemCard(
                item = item,
                onQuantityChange = { newQuantity ->
                    onQuantityChange(itemIndex, newQuantity)
                }
            )
        }
        
        // Warranty Protection Option
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = "Warranty",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Ganti rugi jika nggak cocok/alergi",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatPrice(order.warrantyCostPerItem),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280)
                )
                
                Checkbox(
                    checked = useWarrantyProtection,
                    onCheckedChange = onWarrantyToggle,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF10B981),
                        uncheckedColor = Color(0xFFD1D5DB)
                    )
                )
            }
        }
    }
}

@Composable
private fun OrderItemCard(
    item: com.example.myapplication.data.model.CheckoutItem,
    onQuantityChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val product = item.product
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                model = ImageRequest.Builder(context)
                    .data(ApiClient.getImageUrl(product.thumbnail ?: product.images?.firstOrNull()?.imageUrl))
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
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
                text = product.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Black,
                maxLines = 2
            )
            
            if (product.weight != null) {
                Text(
                    text = "${product.weight}g",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
            
            // Price Section: Original price (crossed out) on top, discount percentage and discounted price below
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Original Price (Crossed out) - shown on top if exists
                item.originalPrice?.let { originalPrice ->
                    if (originalPrice > item.price) {
                        Text(
                            text = formatPrice(originalPrice),
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
                
                // Discounted Price and Discount Percentage - shown below
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatPrice(item.price),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    
                    item.originalPrice?.let { originalPrice ->
                        if (originalPrice > item.price) {
                            val discountPercent = ((originalPrice - item.price) * 100 / originalPrice)
                            Text(
                                text = "$discountPercent%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }
        
        // Quantity Selector
        QuantitySelector(
            quantity = item.quantity,
            onQuantityChange = onQuantityChange
        )
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF3F4F6)),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(18.dp)
            )
        }
        
        Text(
            text = quantity.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Black,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        
        IconButton(
            onClick = { onQuantityChange(quantity + 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ShippingMethodSection(
    shippingMethod: com.example.myapplication.data.model.ShippingMethod,
    useInsurance: Boolean,
    onShippingMethodClick: () -> Unit,
    onInsuranceToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onShippingMethodClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = shippingMethod.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Change",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Shipping Details
        Text(
            text = "${shippingMethod.carrier} ${formatPrice(shippingMethod.cost)}",
            fontSize = 13.sp,
            color = Color(0xFF6B7280)
        )
        
        Text(
            text = "Estimasi tiba ${shippingMethod.estimatedDays}",
            fontSize = 13.sp,
            color = Color(0xFF6B7280)
        )
        
        // Insurance Option
        if (shippingMethod.hasInsurance) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = "Insurance",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Pakai Asuransi Pengiriman ${formatPrice(shippingMethod.insuranceCost)}",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                
                Checkbox(
                    checked = useInsurance,
                    onCheckedChange = onInsuranceToggle,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF10B981),
                        uncheckedColor = Color(0xFFD1D5DB)
                    )
                )
            }
        }
    }
}

@Composable
private fun NoteSection(
    note: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Note",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = "Kasih Catatan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black
                )
                if (note.isNotEmpty()) {
                    Text(
                        text = note,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 1
                    )
                }
            }
        }
        
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "Edit Note",
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun BonusSection(
    bonus: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = "Bonus",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(20.dp)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Bonus",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFF4E6))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = formatPrice(bonus),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }
        
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "View Bonus",
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun PaymentMethodsSection(
    paymentMethods: List<com.example.myapplication.data.model.PaymentMethod>,
    selectedPaymentMethod: com.example.myapplication.data.model.PaymentMethod?,
    onPaymentMethodSelect: (com.example.myapplication.data.model.PaymentMethod) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Metode Pembayaran",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            TextButton(
                onClick = onSeeAllClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Lihat Semua",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF10B981)
                )
            }
        }
        
        // Payment Methods List
        paymentMethods.forEach { method ->
            PaymentMethodItem(
                method = method,
                isSelected = method.id == selectedPaymentMethod?.id,
                onClick = { onPaymentMethodSelect(method) }
            )
        }
    }
}

@Composable
private fun PaymentMethodItem(
    method: com.example.myapplication.data.model.PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Payment Logo berdasarkan id (statik - seperti di gambar)
            PaymentLogoBox(methodId = method.id)
            
            Text(
                text = method.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Black,
                modifier = Modifier.weight(1f)
            )
        }
        
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF10B981),
                unselectedColor = Color(0xFFD1D5DB)
            )
        )
    }
}

@Composable
private fun PaymentLogoBox(methodId: String) {
    val methodName = when {
        methodId == "BCA_VA" -> "BCA"
        methodId == "MANDIRI_VA" -> "MDR"
        methodId == "BRI_VA" -> "BRI"
        methodId == "ALFAMART" -> "ALF"
        methodId == "QRIS" -> "QR"
        else -> ""
    }
    
    val bgColor = when {
        methodId == "BCA_VA" -> Color(0xFF0D47A1) // BCA Blue
        methodId == "MANDIRI_VA" -> Color(0xFFFFC107) // Mandiri Yellow
        methodId == "BRI_VA" -> Color(0xFF1976D2) // BRI Blue
        methodId == "ALFAMART" -> Color(0xFFDC2626) // Alfamart Red
        methodId == "QRIS" -> Color(0xFF10B981) // QRIS Green
        else -> Color(0xFF6B7280)
    }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = methodName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun DetailedPaymentSummarySection(
    summary: com.example.myapplication.data.model.CheckoutSummary
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text(
            text = "Cek ringkasan belanjaanmu, yuk",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )
        
        // Detailed Summary List (seperti di gambar)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF9FAFB))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Total Harga (1 Barang)
            DetailedSummaryRow(
                label = "Total Harga (${summary.totalItems} Barang)",
                value = formatPrice(summary.subtotal)
            )
            
            // Total Ongkos Kirim
            DetailedSummaryRow(
                label = "Total Ongkos Kirim",
                value = formatPrice(summary.shippingCost)
            )
            
            // Total Asuransi Pengiriman
            if (summary.insuranceCost > 0) {
                DetailedSummaryRow(
                    label = "Total Asuransi Pengiriman",
                    value = formatPrice(summary.insuranceCost)
                )
            }
            
            // Biaya Layanan (dengan info icon)
            DetailedSummaryRow(
                label = "Biaya Layanan",
                value = formatPrice(summary.serviceFee),
                showInfoIcon = true
            )
            
            // Biaya Jasa Aplikasi (Diskon) (dengan info icon)
            DetailedSummaryRow(
                label = "Biaya Jasa Aplikasi (Diskon)",
                value = formatPrice(summary.applicationFee),
                showInfoIcon = true
            )
            
            // Diskon (dengan icon percentage dan arrow)
            if (summary.totalDiscount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Diskon",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                        Text(
                            text = "↓",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                        Text(
                            text = "-${formatPrice(summary.totalDiscount)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
            
            // Bonus Cashback (orange text, format: "1.700" tanpa Rp)
            if (summary.bonus > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bonus Cashback",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = formatBonus(summary.bonus),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF59E0B) // Orange color
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color(0xFFE5E7EB),
                thickness = 1.dp
            )
            
            // Total Tagihan (bold, dengan percentage icon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Tagihan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatPrice(summary.total),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Savings text di bawah total (format: "Kamu Hemat Rp10.5rb!")
            if (summary.savings > 0) {
                val savingsText = formatSavings(summary.savings)
                Text(
                    text = "Kamu Hemat $savingsText!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailedSummaryRow(
    label: String,
    value: String,
    showInfoIcon: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
            if (showInfoIcon) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Info",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
private fun DetailedSummaryRowWithIcon(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF6B7280),
    showPercentageIcon: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF6B7280)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showPercentageIcon) {
                Text(
                    text = "%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            }
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
    }
}

@Composable
private fun CheckoutBottomBar(
    total: Int,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    onPayClick: () -> Unit
) {
    Surface(
        color = White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Tagihan",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatPrice(total),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Button(
                onClick = onPayClick,
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 160.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    disabledContainerColor = Color(0xFF9CA3AF)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Pay",
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bayar Sekarang",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }
        }
    }
}

private fun formatPrice(price: Int): String {
    @Suppress("DEPRECATION")
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    val formattedNumber = formatter.format(price)
    return "Rp$formattedNumber"
}

private fun formatSavings(savings: Int): String {
    // Format seperti "Rp10.5rb" untuk 10500
    return when {
        savings >= 1000000 -> {
            val juta = savings / 1000000
            val sisa = (savings % 1000000) / 100000
            if (sisa > 0) "Rp$juta.${sisa}jt" else "Rp${juta}jt"
        }
        savings >= 10000 -> {
            val puluhK = savings / 10000
            val ribu = (savings % 10000) / 1000
            if (ribu >= 5) "Rp$puluhK.${ribu}rb" else "Rp${puluhK}rb"
        }
        savings >= 1000 -> {
            val k = savings / 1000
            val ratus = (savings % 1000) / 100
            if (ratus >= 5) "Rp$k.${ratus}rb" else "Rp${k}rb"
        }
        else -> "Rp${savings}"
    }
}

private fun formatBonus(bonus: Int): String {
    // Format bonus seperti "1.700" (tanpa Rp, dengan titik sebagai separator ribu)
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return formatter.format(bonus)
}
