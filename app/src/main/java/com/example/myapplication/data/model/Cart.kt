package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Cart(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("user_id")
    val userId: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    @SerializedName("cart_items")
    val cartItems: List<CartItem>? = null
)

data class CartItem(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("cart_id")
    val cartId: String,
    
    @SerializedName("product_id")
    val productId: String,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("price")
    val price: Int,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    @SerializedName("product")
    val product: Product? = null
)

data class AddCartItemRequest(
    @SerializedName("product_id")
    val productId: String,
    
    @SerializedName("quantity")
    val quantity: Int
)

data class UpdateCartItemRequest(
    @SerializedName("quantity")
    val quantity: Int
)
