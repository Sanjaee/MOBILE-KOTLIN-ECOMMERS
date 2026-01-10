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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Product Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFFF9FAFB))
            ) {
                // Product Image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.thumbnail ?: product.images?.firstOrNull()?.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.logo),
                    error = painterResource(id = R.drawable.logo)
                )
                
                // Free Shipping Badge - Bottom Left Overlay (like Tokopedia)
                if (product.stock > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 6.dp, bottom = 6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF10B981))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "GRATIS ONGKIR",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }
            
            // Product Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Product Name
                Text(
                    text = product.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF1F2937),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    modifier = Modifier.heightIn(min = 32.dp, max = 32.dp)
                )
                
                // Price
                Text(
                    text = formatPrice(product.price),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEE2121)
                )
                
                // Rating & Sold Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Star Rating Icon & Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "★",
                            fontSize = 11.sp,
                            color = Color(0xFFF59E0B)
                        )
                        Text(
                            text = "4.9",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                    }
                    
                    Text(
                        text = "|",
                        fontSize = 10.sp,
                        color = Color(0xFFD1D5DB)
                    )
                    
                    // Sold Count
                    Text(
                        text = formatSoldCount(product.stock),
                        fontSize = 10.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}

private fun formatPrice(price: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(price)
}

private fun formatSoldCount(stock: Int): String {
    // Use stock as sold count for demo (in real app, use actual sold count field)
    val soldCount = stock * 100 // Demo multiplier to show variety
    return when {
        soldCount >= 1000000 -> {
            val juta = soldCount / 1000000
            if (juta >= 10) "${juta}jt+ terjual" else "${juta},${(soldCount % 1000000) / 100000}jt+ terjual"
        }
        soldCount >= 100000 -> {
            val ratusRb = soldCount / 100000
            "${ratusRb}00rb+ terjual"
        }
        soldCount >= 10000 -> "${soldCount / 1000}rb+ terjual"
        soldCount >= 1000 -> "${soldCount / 1000}rb+ terjual"
        else -> "$soldCount terjual"
    }
}
