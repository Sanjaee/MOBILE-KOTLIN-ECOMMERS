package com.example.myapplication.data.api

import com.example.myapplication.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApiService {
    @POST("api/v1/orders")
    suspend fun createOrder(
        @Body request: CreateOrderRequest,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Order>>
    
    @GET("api/v1/orders")
    suspend fun getOrders(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<Order>>>
    
    @GET("api/v1/orders/{id}")
    suspend fun getOrder(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Order>>
}
