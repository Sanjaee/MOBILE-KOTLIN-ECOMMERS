package com.example.myapplication.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.model.Category
import com.example.myapplication.ui.theme.Black

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductForm(
    productName: String,
    onProductNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    sku: String,
    onSkuChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    stock: String,
    onStockChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    selectedCategoryId: String?,
    selectedCategoryName: String?,
    onCategorySelected: (String, String) -> Unit,
    categories: List<Category>,
    isActive: Boolean,
    onIsActiveChange: (Boolean) -> Unit,
    isFeatured: Boolean,
    onIsFeaturedChange: (Boolean) -> Unit,
    selectedImages: List<Uri>,
    onImagesChange: (List<Uri>) -> Unit,
    existingImageUrls: List<String> = emptyList(),
    onRemoveExistingImage: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showCategoryDropdown by remember { mutableStateOf(false) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.size + selectedImages.size <= 20) {
            onImagesChange(selectedImages + uris)
        }
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                onCategorySelected(category.id, category.name)
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
                onValueChange = onProductNameChange,
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
                onValueChange = { onSkuChange(it.uppercase()) },
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
                onValueChange = onDescriptionChange,
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
                onValueChange = { onPriceChange(it.filter { char -> char.isDigit() }) },
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
                onValueChange = { onStockChange(it.filter { char -> char.isDigit() }) },
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
                onValueChange = { onWeightChange(it.filter { char -> char.isDigit() }) },
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
                    text = "${selectedImages.size + existingImageUrls.size}/20",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
            
            // Image Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(
                    if (selectedImages.isEmpty() && existingImageUrls.isEmpty()) 100.dp 
                    else (100.dp * ((selectedImages.size + existingImageUrls.size + 2) / 3 + 1)).coerceAtMost(300.dp)
                )
            ) {
                // Existing images from server
                items(existingImageUrls) { imageUrl ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Product Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (onRemoveExistingImage != null) {
                            IconButton(
                                onClick = { onRemoveExistingImage(imageUrl) },
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
                }
                
                // New selected images
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
                                onImagesChange(selectedImages.filter { it != uri })
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
                if (selectedImages.size + existingImageUrls.size < 20) {
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
                onCheckedChange = onIsActiveChange
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
                onCheckedChange = onIsFeaturedChange
            )
        }
    }
}
