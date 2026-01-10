package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository {
    private val apiService = ApiClient.paymentApiService
    
    suspend fun createPayment(
        orderId: String,
        paymentMethod: String,
        bank: String? = null,
        token: String
    ): Result<Payment> = withContext(Dispatchers.IO) {
        try {
            val request = CreatePaymentRequest(
                orderId = orderId,
                paymentMethod = paymentMethod,
                bank = bank
            )
            
            val response = apiService.createPayment(request, "Bearer $token")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("No payment data received"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to create payment"
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
    
    suspend fun getPayment(paymentId: String, token: String): Result<Payment> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPayment(paymentId, "Bearer $token")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Payment not found"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to fetch payment"
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
    
    suspend fun getPaymentByOrder(orderId: String, token: String): Result<Payment> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPaymentByOrder(orderId, "Bearer $token")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Payment not found for order"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to fetch payment"
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
    
    suspend fun checkPaymentStatus(paymentId: String, token: String): Result<Payment> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkPaymentStatus(paymentId, "Bearer $token")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Failed to check payment status"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to check payment status"
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
