package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository {
    private val apiService = ApiClient.orderApiService
    
    suspend fun createOrder(
        shippingAddressId: String,
        orderItems: List<CreateOrderItemRequest>,
        shippingCost: Int,
        insuranceCost: Int,
        warrantyCost: Int,
        serviceFee: Int,
        applicationFee: Int,
        totalDiscount: Int,
        bonus: Int,
        notes: String? = null,
        token: String
    ): Result<Order> = withContext(Dispatchers.IO) {
        try {
            // Calculate subtotal from order items
            val calculatedSubtotal = orderItems.sumOf { it.price * it.quantity }
            
            val request = CreateOrderRequest(
                shippingAddressId = shippingAddressId,
                orderItems = orderItems,
                subtotal = calculatedSubtotal,
                shippingCost = shippingCost,
                insuranceCost = insuranceCost,
                warrantyCost = warrantyCost,
                serviceFee = serviceFee,
                applicationFee = applicationFee,
                totalDiscount = totalDiscount,
                bonus = bonus,
                notes = notes
            )
            
            val response = apiService.createOrder(request, "Bearer $token")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("No order data received"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to create order"
                if (response.code() == 401) {
                    Result.failure(TokenExpiredException("Token expired. Please login again."))
                } else {
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getOrders(
        page: Int = 1,
        limit: Int = 10,
        status: String? = null,
        paymentStatus: String? = null,
        token: String
    ): Result<OrdersListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOrders(page, limit, status, paymentStatus, "Bearer $token")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("No orders found"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to fetch orders"
                if (response.code() == 401) {
                    Result.failure(TokenExpiredException("Token expired. Please login again."))
                } else {
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getOrder(orderId: String, token: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOrder(orderId, "Bearer $token")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Order not found"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to fetch order"
                if (response.code() == 401) {
                    Result.failure(TokenExpiredException("Token expired. Please login again."))
                } else {
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
