package com.example.myapplication.data.api

import com.example.myapplication.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ProductApiService {
    @GET("api/v1/products")
    suspend fun getProducts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("category_id") categoryId: String? = null,
        @Query("featured") featured: String? = null,
        @Query("active_only") activeOnly: String? = null
    ): Response<ApiResponse<ProductListResponse>>
    
    @GET("api/v1/products/search")
    suspend fun searchProducts(
        @Query("q") keyword: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("active_only") activeOnly: String? = "true"
    ): Response<ApiResponse<ProductListResponse>>
    
    @GET("api/v1/products/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<ApiResponse<Product>>
    
    @GET("api/v1/categories")
    suspend fun getCategories(
        @Query("active_only") activeOnly: String? = null
    ): Response<ApiResponse<List<Category>>>
    
    @GET("api/v1/categories/{id}")
    suspend fun getCategoryById(@Path("id") id: String): Response<ApiResponse<Category>>
    
    @POST("api/v1/products")
    suspend fun createProduct(
        @Header("Authorization") token: String,
        @Body request: CreateProductRequest
    ): Response<ApiResponse<Product>>
    
    @PUT("api/v1/products/{id}")
    suspend fun updateProduct(
        @Header("Authorization") token: String,
        @Path("id") productId: String,
        @Body request: UpdateProductRequest
    ): Response<ApiResponse<Product>>
    
    @DELETE("api/v1/products/{id}")
    suspend fun deleteProduct(
        @Header("Authorization") token: String,
        @Path("id") productId: String
    ): Response<ApiResponse<Unit>>
    
    @Multipart
    @POST("api/v1/products/{id}/images/upload")
    suspend fun uploadProductImages(
        @Header("Authorization") token: String,
        @Path("id") productId: String,
        @Part images: List<MultipartBody.Part>
    ): Response<ApiResponse<UploadImagesResponse>>
}
