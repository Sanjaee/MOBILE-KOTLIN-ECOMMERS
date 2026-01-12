package com.example.myapplication.data.api

import com.example.myapplication.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface CartApiService {
    @GET("api/v1/carts")
    suspend fun getCart(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Cart>>
    
    @GET("api/v1/carts/items")
    suspend fun getCartItems(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<CartItem>>>
    
    @POST("api/v1/carts/items")
    suspend fun addItemToCart(
        @Header("Authorization") token: String,
        @Body request: AddCartItemRequest
    ): Response<ApiResponse<CartItem>>
    
    @PUT("api/v1/carts/items/{id}")
    suspend fun updateCartItem(
        @Header("Authorization") token: String,
        @Path("id") cartItemId: String,
        @Body request: UpdateCartItemRequest
    ): Response<ApiResponse<CartItem>>
    
    @DELETE("api/v1/carts/items/{id}")
    suspend fun removeCartItem(
        @Header("Authorization") token: String,
        @Path("id") cartItemId: String
    ): Response<ApiResponse<Unit>>
    
    @DELETE("api/v1/carts")
    suspend fun clearCart(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Unit>>
}
