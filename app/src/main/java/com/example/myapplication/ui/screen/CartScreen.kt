package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.data.model.CartItem
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.CartViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import android.app.Application
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: (List<CartItem>) -> Unit = {},
    cartViewModel: CartViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val uiState by cartViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Load cart on first render
    LaunchedEffect(Unit) {
        cartViewModel.loadCart()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Keranjang",
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
                    IconButton(onClick = { /* Wishlist */ }) {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = Black
                        )
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
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Bar
            if (uiState.selectedItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
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
                        Text(
                            text = "${uiState.selectedItems.size} produk terpilih",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                        TextButton(
                            onClick = {
                                uiState.selectedItems.forEach { itemId ->
                                    cartViewModel.removeItem(itemId)
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF10B981)
                            )
                        ) {
                            Text(
                                text = "Hapus",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Cart Items List
            if (uiState.isLoading && uiState.cartItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF10B981))
                }
            } else if (uiState.cartItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "Empty Cart",
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF9CA3AF)
                        )
                        Text(
                            text = "Keranjang kosong",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = "Yuk, mulai belanja!",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.cartItems,
                        key = { it.id }
                    ) { cartItem ->
                        CartItemCard(
                            cartItem = cartItem,
                            isSelected = uiState.selectedItems.contains(cartItem.id),
                            onSelectChange = { cartViewModel.toggleItemSelection(cartItem.id) },
                            onIncrement = { cartViewModel.incrementQuantity(cartItem.id) },
                            onDecrement = { cartViewModel.decrementQuantity(cartItem.id) },
                            onRemove = { cartViewModel.removeItem(cartItem.id) },
                            isUpdating = uiState.isUpdating
                        )
                    }
                }
            }
            
            // Footer - Checkout Section
            if (uiState.cartItems.isNotEmpty()) {
                CartFooter(
                    selectedCount = uiState.selectedItems.size,
                    totalPrice = cartViewModel.getTotalPrice(),
                    totalQuantity = cartViewModel.getTotalQuantity(),
                    isAllSelected = uiState.selectedItems.size == uiState.cartItems.size,
                    onSelectAll = {
                        if (uiState.selectedItems.size == uiState.cartItems.size) {
                            cartViewModel.deselectAll()
                        } else {
                            cartViewModel.selectAll()
                        }
                    },
                    onCheckout = {
                        val selectedItems = cartViewModel.getSelectedItems()
                        if (selectedItems.isNotEmpty()) {
                            onCheckout(selectedItems)
                        }
                    },
                    enabled = uiState.selectedItems.isNotEmpty() && !uiState.isLoading
                )
            }
        }
    }
}

@Composable
private fun CartItemCard(
    cartItem: CartItem,
    isSelected: Boolean,
    onSelectChange: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    isUpdating: Boolean
) {
    val product = cartItem.product
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Seller Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectChange() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF10B981),
                            uncheckedColor = Color(0xFFD1D5DB)
                        )
                    )
                    Text(
                        text = "Seller",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            
            // Product Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product Image
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(ApiClient.getImageUrl(product?.thumbnail ?: product?.images?.firstOrNull()?.imageUrl))
                        .crossfade(true)
                        .build(),
                    contentDescription = product?.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Product Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = product?.name ?: "Product",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Price
                    Text(
                        text = formatPrice(cartItem.price),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                    
                    // Quantity Selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDecrement,
                            enabled = !isUpdating,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Remove,
                                contentDescription = "Decrease",
                                modifier = Modifier.size(18.dp),
                                tint = if (cartItem.quantity > 1) Color(0xFF10B981) else Color(0xFF9CA3AF)
                            )
                        }
                        Text(
                            text = cartItem.quantity.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Black,
                            modifier = Modifier.width(40.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        IconButton(
                            onClick = onIncrement,
                            enabled = !isUpdating && (product?.stock ?: 0) > cartItem.quantity,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Increase",
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartFooter(
    selectedCount: Int,
    totalPrice: Int,
    totalQuantity: Int,
    isAllSelected: Boolean,
    onSelectAll: () -> Unit,
    onCheckout: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Select All Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAllSelected,
                        onCheckedChange = { onSelectAll() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF10B981),
                            uncheckedColor = Color(0xFFD1D5DB)
                        )
                    )
                    Text(
                        text = "Pilih Semua",
                        fontSize = 14.sp,
                        color = Black
                    )
                }
            }
            
            // Bonus Info (Optional)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CardGiftcard,
                    contentDescription = "Bonus",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF10B981)
                )
                Text(
                    text = "Bonus 7,7rb",
                    fontSize = 14.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Total Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatPrice(totalPrice),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    // Total Discount (Optional, expandable)
                    Text(
                        text = "Total Diskon Rp10.5rb",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                
                // Checkout Button
                Button(
                    onClick = onCheckout,
                    enabled = enabled,
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = White,
                        disabledContainerColor = Color(0xFFD1D5DB),
                        disabledContentColor = Color(0xFF9CA3AF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Beli ($totalQuantity)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
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
