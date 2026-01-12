package com.example.myapplication.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.data.model.UpdateProductRequest
import com.example.myapplication.ui.components.ProductForm
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.ProductViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    productId: String,
    onBack: () -> Unit,
    onProductUpdated: () -> Unit,
    productViewModel: ProductViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val uiState by productViewModel.uiState.collectAsState()
    val product = uiState.productDetail
    val context = LocalContext.current
    
    // Form states
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedCategoryName by remember { mutableStateOf<String?>(null) }
    var isActive by remember { mutableStateOf(true) }
    var isFeatured by remember { mutableStateOf(false) }
    var selectedImages by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }
    var existingImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    // Load product and categories on first render
    LaunchedEffect(productId) {
        productViewModel.loadProductById(productId)
        productViewModel.loadCategories(activeOnly = true)
    }
    
    // Populate form when product is loaded
    LaunchedEffect(product) {
        if (product != null) {
            productName = product.name
            description = product.description ?: ""
            sku = product.sku
            price = product.price.toString()
            stock = product.stock.toString()
            weight = product.weight?.toString() ?: ""
            selectedCategoryId = product.categoryId
            selectedCategoryName = product.category?.name
            isActive = product.isActive
            isFeatured = product.isFeatured
            existingImageUrls = product.images?.mapNotNull { 
                it.imageUrl?.let { url -> ApiClient.getImageUrl(url) }
            } ?: emptyList()
        }
    }
    
    // Navigate when product is updated successfully
    LaunchedEffect(uiState.isCreateSuccess, uiState.createdProductId) {
        if (uiState.isCreateSuccess && uiState.createdProductId != null) {
            val updatedProductId = uiState.createdProductId!!
            
            // Upload new images if any selected
            if (selectedImages.isNotEmpty()) {
                productViewModel.uploadProductImages(updatedProductId, selectedImages)
            } else {
                onProductUpdated()
                productViewModel.resetCreateSuccess()
            }
        }
    }
    
    // Handle image upload success - navigate back
    LaunchedEffect(uiState.uploadImagesSuccess, uiState.createdProductId) {
        if (uiState.uploadImagesSuccess && uiState.createdProductId != null) {
            productViewModel.resetUploadImagesSuccess()
            productViewModel.resetCreateSuccess()
            onProductUpdated()
        }
    }
    
    // Handle delete success - navigate back
    LaunchedEffect(uiState.isDeleteSuccess) {
        if (uiState.isDeleteSuccess) {
            productViewModel.resetDeleteSuccess()
            onProductUpdated()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Produk",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            if (uiState.isLoadingDetail && product == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF10B981))
                }
            } else if (product != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Error Message
                    uiState.errorMessage?.let { error ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEE2E2)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = error,
                                color = Color(0xFFDC2626),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    
                    ProductForm(
                        productName = productName,
                        onProductNameChange = { value -> productName = value },
                        description = description,
                        onDescriptionChange = { value -> description = value },
                        sku = sku,
                        onSkuChange = { value -> sku = value },
                        price = price,
                        onPriceChange = { value -> price = value },
                        stock = stock,
                        onStockChange = { value -> stock = value },
                        weight = weight,
                        onWeightChange = { value -> weight = value },
                        selectedCategoryId = selectedCategoryId,
                        selectedCategoryName = selectedCategoryName,
                        onCategorySelected = { id, name ->
                            selectedCategoryId = id
                            selectedCategoryName = name
                        },
                        categories = uiState.categories,
                        isActive = isActive,
                        onIsActiveChange = { value -> isActive = value },
                        isFeatured = isFeatured,
                        onIsFeaturedChange = { value -> isFeatured = value },
                        selectedImages = selectedImages,
                        onImagesChange = { images -> selectedImages = images },
                        existingImageUrls = existingImageUrls,
                        onRemoveExistingImage = { url ->
                            existingImageUrls = existingImageUrls.filter { imageUrl -> imageUrl != url }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Submit Button
                    Button(
                        onClick = {
                            if (selectedCategoryId != null && productName.isNotBlank() && 
                                sku.isNotBlank() && price.isNotBlank() && stock.isNotBlank()) {
                                val request = UpdateProductRequest(
                                    categoryId = selectedCategoryId,
                                    name = productName.trim(),
                                    description = description.takeIf { it.isNotBlank() },
                                    sku = sku.trim(),
                                    price = price.toIntOrNull(),
                                    stock = stock.toIntOrNull(),
                                    weight = weight.takeIf { it.isNotBlank() }?.toIntOrNull(),
                                    thumbnail = null,
                                    isActive = isActive,
                                    isFeatured = isFeatured
                                )
                                productViewModel.updateProduct(productId, request)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = selectedCategoryId != null && productName.isNotBlank() && 
                                 sku.isNotBlank() && price.isNotBlank() && stock.isNotBlank() && 
                                 !uiState.isLoading && !uiState.isUploadingImages,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = White,
                            disabledContainerColor = Color(0xFFD1D5DB),
                            disabledContentColor = Color(0xFF9CA3AF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isLoading || uiState.isUploadingImages) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Update Produk",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Delete Button
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isLoading && !uiState.isUploadingImages && !uiState.isDeleting,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444),
                            disabledContentColor = Color(0xFF9CA3AF)
                        ),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFFEF4444),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Hapus Produk",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Produk tidak ditemukan",
                            fontSize = 16.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
        
        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { 
                    if (!uiState.isDeleting) {
                        showDeleteDialog = false
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "Hapus Produk?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Apakah Anda yakin ingin menghapus produk ini?",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                        if (product != null) {
                            Text(
                                text = "\"${product.name}\"",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Black
                            )
                        }
                        Text(
                            text = "Tindakan ini tidak dapat dibatalkan.",
                            fontSize = 14.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            productViewModel.deleteProduct(productId)
                        },
                        enabled = !uiState.isDeleting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            contentColor = White,
                            disabledContainerColor = Color(0xFFD1D5DB),
                            disabledContentColor = Color(0xFF9CA3AF)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Hapus",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false },
                        enabled = !uiState.isDeleting,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Batal",
                            color = if (uiState.isDeleting) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                containerColor = White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
