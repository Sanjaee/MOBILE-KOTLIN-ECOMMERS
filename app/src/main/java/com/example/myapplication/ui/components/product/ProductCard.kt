package com.example.myapplication.ui.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.R
import com.example.myapplication.data.model.Product
import com.example.myapplication.ui.theme.Black
import java.text.NumberFormat
import java.util.*

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate discount (static for demo)
    val originalPrice = (product.price * 1.65).toInt() // Static: original price 65% higher
    val rating = 4.9f // Static
    val soldCount = 1000 // Static
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pure Product Image (No overlay, no badge, no text)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                    .background(Color(0xFFF9FAFB))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
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
            
            // Content Section (Title, Price, Rating)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Product Title
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                
                // Price Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Current Price (Red, Bold)
                    Text(
                        text = formatPrice(product.price),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                    
                    // Original Price (Strikethrough, Gray)
                    if (originalPrice > product.price) {
                        Text(
                            text = formatPrice(originalPrice),
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF),
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
                
                // Rating and Sold Count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "★",
                        fontSize = 14.sp,
                        color = Color(0xFFF59E0B)
                    )
                    Text(
                        text = "$rating",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = Color(0xFFD1D5DB)
                    )
                    Text(
                        text = formatSoldCount(soldCount),
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
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

private fun formatSoldCount(soldCount: Int): String {
    return when {
        soldCount >= 1000000 -> {
            val juta = soldCount / 1000000
            val sisaJuta = (soldCount % 1000000) / 100000
            if (sisaJuta > 0) "${juta},${sisaJuta}jt+ terjual" else "${juta}jt+ terjual"
        }
        soldCount >= 100000 -> {
            val ratusRb = soldCount / 100000
            "${ratusRb}00rb+ terjual"
        }
        soldCount >= 1000 -> {
            val ribu = soldCount / 1000
            "${ribu}rb+ terjual"
        }
        else -> {
            // Untuk angka < 1000, tetap tampilkan "1rb+ terjual" sebagai minimum
            "1rb+ terjual"
        }
    }
}
