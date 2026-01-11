package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("category_id")
    val categoryId: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("sku")
    val sku: String,
    
    @SerializedName("price")
    val price: Int,
    
    @SerializedName("stock")
    val stock: Int,
    
    @SerializedName("weight")
    val weight: Int? = null,
    
    @SerializedName("thumbnail")
    val thumbnail: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean,
    
    @SerializedName("is_featured")
    val isFeatured: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    @SerializedName("category")
    val category: Category? = null,
    
    @SerializedName("images")
    val images: List<ProductImage>? = null
)

data class Category(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("slug")
    val slug: String,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("parent_id")
    val parentId: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)

data class ProductImage(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("product_id")
    val productId: String,
    
    @SerializedName("image_url")
    val imageUrl: String,
    
    @SerializedName("sort_order")
    val sortOrder: Int,
    
    @SerializedName("created_at")
    val createdAt: String
)

data class ProductListResponse(
    @SerializedName("products")
    val products: List<Product>,
    
    @SerializedName("total")
    val total: Long,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("limit")
    val limit: Int
)

data class CreateProductRequest(
    @SerializedName("category_id")
    val categoryId: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("sku")
    val sku: String,
    
    @SerializedName("price")
    val price: Int,
    
    @SerializedName("stock")
    val stock: Int,
    
    @SerializedName("weight")
    val weight: Int? = null,
    
    @SerializedName("thumbnail")
    val thumbnail: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean? = null,
    
    @SerializedName("is_featured")
    val isFeatured: Boolean? = null
)