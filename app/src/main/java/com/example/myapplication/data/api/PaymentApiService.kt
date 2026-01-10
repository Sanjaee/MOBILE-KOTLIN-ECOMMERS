package com.example.myapplication.data.api

import com.example.myapplication.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface PaymentApiService {
    @POST("api/v1/payments")
    suspend fun createPayment(
        @Body request: CreatePaymentRequest,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Payment>>
    
    @GET("api/v1/payments/{id}")
    suspend fun getPayment(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Payment>>
    
    @GET("api/v1/payments/order/{order_id}")
    suspend fun getPaymentByOrder(
        @Path("order_id") orderId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Payment>>
    
    @GET("api/v1/payments/{id}/status")
    suspend fun checkPaymentStatus(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Payment>>
}
