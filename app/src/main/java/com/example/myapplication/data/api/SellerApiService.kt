package com.example.myapplication.data.api

import com.example.myapplication.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SellerApiService {
    @POST("api/v1/sellers")
    suspend fun createSeller(
        @Header("Authorization") token: String,
        @Body request: CreateSellerRequest
    ): Response<ApiResponse<Seller>>
    
    @GET("api/v1/sellers/me")
    suspend fun getMySeller(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Seller>>
    
    @GET("api/v1/sellers/{id}")
    suspend fun getSeller(
        @Path("id") sellerId: String
    ): Response<ApiResponse<Seller>>
    
    @PUT("api/v1/sellers")
    suspend fun updateSeller(
        @Header("Authorization") token: String,
        @Body request: CreateSellerRequest
    ): Response<ApiResponse<Seller>>
}