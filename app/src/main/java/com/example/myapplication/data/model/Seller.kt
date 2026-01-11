package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Seller(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("user_id")
    val userId: String,
    
    @SerializedName("shop_name")
    val shopName: String,
    
    @SerializedName("shop_slug")
    val shopSlug: String,
    
    @SerializedName("shop_description")
    val shopDescription: String? = null,
    
    @SerializedName("shop_logo")
    val shopLogo: String? = null,
    
    @SerializedName("shop_banner")
    val shopBanner: String? = null,
    
    @SerializedName("shop_address")
    val shopAddress: String? = null,
    
    @SerializedName("shop_city")
    val shopCity: String? = null,
    
    @SerializedName("shop_province")
    val shopProvince: String? = null,
    
    @SerializedName("shop_phone")
    val shopPhone: String? = null,
    
    @SerializedName("shop_email")
    val shopEmail: String? = null,
    
    @SerializedName("is_verified")
    val isVerified: Boolean,
    
    @SerializedName("is_active")
    val isActive: Boolean,
    
    @SerializedName("total_products")
    val totalProducts: Int,
    
    @SerializedName("total_sales")
    val totalSales: Int,
    
    @SerializedName("rating_average")
    val ratingAverage: Double,
    
    @SerializedName("total_reviews")
    val totalReviews: Int,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)

data class CreateSellerRequest(
    @SerializedName("shop_name")
    val shopName: String,
    
    @SerializedName("shop_description")
    val shopDescription: String? = null,
    
    @SerializedName("shop_logo")
    val shopLogo: String? = null,
    
    @SerializedName("shop_banner")
    val shopBanner: String? = null,
    
    @SerializedName("shop_address")
    val shopAddress: String? = null,
    
    @SerializedName("shop_city")
    val shopCity: String? = null,
    
    @SerializedName("shop_province")
    val shopProvince: String? = null,
    
    @SerializedName("shop_phone")
    val shopPhone: String? = null,
    
    @SerializedName("shop_email")
    val shopEmail: String? = null
)