package com.example.myapplication.ui.screen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
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
import com.example.myapplication.ui.components.product.ProductCard
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.ProductViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    keyword: String,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: ProductViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    var searchQuery by remember { mutableStateOf(keyword) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Load search results on first render
    LaunchedEffect(keyword) {
        if (keyword.isNotBlank()) {
            viewModel.searchProducts(keyword, page = 1, limit = 20)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
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
        containerColor = White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(White)
        ) {
            // Search Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Cari di Tokopedia",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF9CA3AF)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { searchQuery = "" }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        cursorColor = Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                // Search Button
                TextButton(
                    onClick = {
                        if (searchQuery.isNotBlank()) {
                            viewModel.searchProducts(searchQuery.trim(), page = 1, limit = 20)
                        }
                    },
                    enabled = searchQuery.isNotBlank() && !uiState.isSearching,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF10B981)
                    )
                ) {
                    Text(
                        text = "Cari",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Results Section
            if (uiState.isSearching && uiState.searchResults.isEmpty()) {
                // Loading State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF10B981))
                        Text(
                            text = "Mencari produk...",
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (uiState.errorMessage != null && uiState.searchResults.isEmpty()) {
                // Error State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Gagal mencari produk",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFDC2626)
                        )
                        Text(
                            text = uiState.errorMessage ?: "Unknown error",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                        Button(
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    viewModel.searchProducts(searchQuery.trim(), page = 1, limit = 20)
                                }
                            }
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            } else if (uiState.searchResults.isEmpty() && !uiState.isSearching) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Produk tidak ditemukan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Black
                        )
                        Text(
                            text = "Coba gunakan kata kunci yang berbeda",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            } else {
                // Results Grid
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Results Count
                    if (uiState.searchTotal > 0) {
                        Text(
                            text = "Ditemukan ${uiState.searchTotal} produk",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    // Product Grid (2 columns)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.searchResults) { product ->
                            ProductCard(
                                product = product,
                                onClick = { onProductClick(product.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
