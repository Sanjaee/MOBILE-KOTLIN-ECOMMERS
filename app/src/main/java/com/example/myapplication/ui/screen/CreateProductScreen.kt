package com.example.myapplication.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.data.model.CreateProductRequest
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.ProductViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductScreen(
    onBack: () -> Unit,
    onProductCreated: (String) -> Unit, // Pass product ID
    productViewModel: ProductViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
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
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    // Error states for form validation
    var categoryError by remember { mutableStateOf(false) }
    var productNameError by remember { mutableStateOf(false) }
    var skuError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var stockError by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    val uiState by productViewModel.uiState.collectAsState()
    
    // Load categories on first render
    LaunchedEffect(Unit) {
        productViewModel.loadCategories(activeOnly = true)
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.size + selectedImages.size <= 20) {
            selectedImages = selectedImages + uris
        }
    }
    
    // Navigate when product is created successfully
    LaunchedEffect(uiState.isCreateSuccess, uiState.createdProductId) {
        if (uiState.isCreateSuccess && uiState.createdProductId != null) {
            val productId = uiState.createdProductId!!
            
            // Upload images if any selected
            if (selectedImages.isNotEmpty()) {
                productViewModel.uploadProductImages(productId, selectedImages)
            } else {
                onProductCreated(productId)
                productViewModel.resetCreateSuccess()
            }
        }
    }
    
    // Handle image upload success - navigate to home
    LaunchedEffect(uiState.uploadImagesSuccess, uiState.createdProductId) {
        if (uiState.uploadImagesSuccess && uiState.createdProductId != null) {
            productViewModel.resetUploadImagesSuccess()
            productViewModel.resetCreateSuccess()
            onProductCreated(uiState.createdProductId!!)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tambah Produk",
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
                
                // Category Selection (Required)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Kategori *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = showCategoryDropdown,
                        onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedCategoryName ?: "",
                            onValueChange = { },
                            readOnly = true,
                            placeholder = {
                                Text(
                                    "Pilih kategori",
                                    color = Color(0xFF9CA3AF)
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                cursorColor = Color(0xFF10B981)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false }
                        ) {
                            uiState.categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = category.id
                                        selectedCategoryName = category.name
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Product Name (Required)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Nama Produk *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { 
                            productName = it
                            productNameError = false
                        },
                        placeholder = {
                            Text(
                                "Masukkan nama produk",
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // SKU (Required)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SKU *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { 
                            sku = it.uppercase()
                            skuError = false
                        },
                        placeholder = {
                            Text(
                                "Masukkan SKU (kode unik produk)",
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Description (Optional)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Deskripsi (Opsional)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = {
                            Text(
                                "Masukkan deskripsi produk",
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Price (Required)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Harga (Rp) *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    
                    OutlinedTextField(
                        value = price,
                        onValueChange = { 
                            price = it.filter { char -> char.isDigit() }
                        },
                        placeholder = {
                            Text(
                                "Masukkan harga",
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Text(
                                text = "Rp",
                                modifier = Modifier.padding(start = 16.dp),
                                color = Color(0xFF6B7280)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Stock (Required)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Stok *",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { 
                            stock = it.filter { char -> char.isDigit() }
                        },
                        placeholder = {
                            Text(
                                "Masukkan jumlah stok",
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Weight (Optional)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Berat (gram) (Opsional)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { 
                            weight = it.filter { char -> char.isDigit() }
                        },
                        placeholder = {
                            Text(
                                "Masukkan berat dalam gram",
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Text(
                                text = "g",
                                modifier = Modifier.padding(end = 16.dp),
                                color = Color(0xFF6B7280)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Product Images (Optional, Max 20)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gambar Produk (Opsional, Max 20)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Black
                        )
                        Text(
                            text = "${selectedImages.size}/20",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                    
                    // Image Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(if (selectedImages.isEmpty()) 100.dp else (100.dp * ((selectedImages.size + 2) / 3 + 1)).coerceAtMost(300.dp))
                    ) {
                        items(selectedImages) { uri ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Product Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        selectedImages = selectedImages.filter { it != uri }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                        
                        // Add Image Button
                        if (selectedImages.size < 20) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .border(2.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                        .clickable {
                                            imagePickerLauncher.launch("image/*")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Add Image",
                                            tint = Color(0xFF6B7280),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            text = "Tambah",
                                            fontSize = 12.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Is Active Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Aktifkan Produk",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
                
                // Is Featured Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tampilkan sebagai Featured",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Submit Button
                Button(
                    onClick = {
                        if (selectedCategoryId != null && productName.isNotBlank() && 
                            sku.isNotBlank() && price.isNotBlank() && stock.isNotBlank()) {
                            val request = CreateProductRequest(
                                categoryId = selectedCategoryId!!,
                                name = productName.trim(),
                                description = description.takeIf { it.isNotBlank() },
                                sku = sku.trim(),
                                price = price.toIntOrNull() ?: 0,
                                stock = stock.toIntOrNull() ?: 0,
                                weight = weight.takeIf { it.isNotBlank() }?.toIntOrNull(),
                                thumbnail = null, // Images will be uploaded separately
                                isActive = isActive,
                                isFeatured = isFeatured
                            )
                            productViewModel.createProduct(request)
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
                            text = "Simpan Produk",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
