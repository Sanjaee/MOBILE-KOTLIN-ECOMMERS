package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.CreateSellerRequest
import com.example.myapplication.data.preferences.PreferencesManager
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.viewmodel.SellerViewModel
import com.example.myapplication.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoreScreen(
    onBack: () -> Unit,
    onStoreCreated: (String) -> Unit, // Pass seller ID
    viewModel: SellerViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    var shopName by remember { mutableStateOf("") }
    var shopDescription by remember { mutableStateOf("") }
    var shopPhone by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Check if user already has a store
    LaunchedEffect(Unit) {
        viewModel.checkHasStore()
        userEmail = preferencesManager.userEmail.first()
    }
    
    // Navigate to store detail if user already has a store
    LaunchedEffect(uiState.hasStore, uiState.seller) {
        if (uiState.hasStore && uiState.seller != null && !uiState.isCheckingStore) {
            onStoreCreated(uiState.seller!!.id) // Navigate to store detail
        }
    }
    
    // Generate domain preview from shop name
    val domainPreview = remember(shopName) {
        if (shopName.isNotBlank()) {
            shopName.lowercase()
                .replace(" ", "-")
                .filter { it.isLetterOrDigit() || it == '-' }
                .take(50)
        } else {
            ""
        }
    }
    
    // Navigate when store is created successfully
    LaunchedEffect(uiState.isCreateSuccess, uiState.createdSellerId) {
        if (uiState.isCreateSuccess && uiState.createdSellerId != null) {
            onStoreCreated(uiState.createdSellerId!!)
            viewModel.resetCreateSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Masukkan info toko",
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
        // Show loading while checking if user has store
        if (uiState.isCheckingStore) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF10B981))
            }
        } else {
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
                    .verticalScroll(rememberScrollState()),
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
                
                // Store Name Field
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Nama tokomu?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it.filter { char -> char != '\n' } },
                        placeholder = {
                            Text(
                                "Masukkan nama toko",
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Store,
                                contentDescription = "Store Name",
                                tint = Color(0xFF6B7280)
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
                    
                    Text(
                        text = "Nama yang menarik lebih mudah diingat pembeli.",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                    
                    Text(
                        text = "Nama yang sudah dipilih tidak dapat diubah",
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Domain Preview
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Domain",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF3F4F6)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "pedia.com/",
                                fontSize = 16.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = domainPreview.ifEmpty { "nama-toko" },
                                fontSize = 16.sp,
                                color = if (domainPreview.isNotEmpty()) Black else Color(0xFF9CA3AF),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Text(
                        text = "Domain akan otomatis dibuat berdasarkan nama toko",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Store Description (Optional)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Deskripsi Toko (Opsional)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    
                    OutlinedTextField(
                        value = shopDescription,
                        onValueChange = { shopDescription = it },
                        placeholder = {
                            Text(
                                "Masukkan deskripsi toko",
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
                
                // Store Phone (Optional)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Nomor Telepon (Opsional)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    
                    OutlinedTextField(
                        value = shopPhone,
                        onValueChange = { shopPhone = it.filter { char -> char.isDigit() || char == '+' } },
                        placeholder = {
                            Text(
                                "Masukkan nomor telepon",
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Continue Button
                Button(
                    onClick = {
                        if (shopName.isNotBlank()) {
                            val request = CreateSellerRequest(
                                shopName = shopName.trim(),
                                shopDescription = shopDescription.takeIf { it.isNotBlank() },
                                shopPhone = shopPhone.takeIf { it.isNotBlank() },
                                shopEmail = userEmail?.takeIf { it.isNotBlank() }
                            )
                            viewModel.createSeller(request)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = shopName.isNotBlank() && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = White,
                        disabledContainerColor = Color(0xFFD1D5DB),
                        disabledContentColor = Color(0xFF9CA3AF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Lanjut",
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
}