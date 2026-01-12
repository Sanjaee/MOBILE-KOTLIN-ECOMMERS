package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
                        onProductNameChange = { productName = it },
                        description = description,
                        onDescriptionChange = { description = it },
                        sku = sku,
                        onSkuChange = { sku = it },
                        price = price,
                        onPriceChange = { price = it },
                        stock = stock,
                        onStockChange = { stock = it },
                        weight = weight,
                        onWeightChange = { weight = it },
                        selectedCategoryId = selectedCategoryId,
                        selectedCategoryName = selectedCategoryName,
                        onCategorySelected = { id, name ->
                            selectedCategoryId = id
                            selectedCategoryName = name
                        },
                        categories = uiState.categories,
                        isActive = isActive,
                        onIsActiveChange = { isActive = it },
                        isFeatured = isFeatured,
                        onIsFeaturedChange = { isFeatured = it },
                        selectedImages = selectedImages,
                        onImagesChange = { selectedImages = it },
                        existingImageUrls = existingImageUrls,
                        onRemoveExistingImage = { url ->
                            existingImageUrls = existingImageUrls.filter { it != url }
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
    }
}
