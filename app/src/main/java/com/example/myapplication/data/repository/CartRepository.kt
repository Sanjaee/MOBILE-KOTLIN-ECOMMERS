package com.example.myapplication.data.repository

import android.content.Context
import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.data.model.*
import com.example.myapplication.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first

class CartRepository(private val context: Context? = null) {
    private val apiService = ApiClient.cartApiService
    private val preferencesManager = context?.let { PreferencesManager(it) }
    
    suspend fun getCart(): Result<Cart> {
        return try {
            val token = preferencesManager?.accessToken?.first()
                ?: return Result.failure(Exception("Anda belum login. Silakan login terlebih dahulu."))
            
            val response = apiService.getCart("Bearer $token")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("No data received"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to fetch cart"
                
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
    
    suspend fun getCartItems(): Result<List<CartItem>> {
        return try {
            val token = preferencesManager?.accessToken?.first()
                ?: return Result.failure(Exception("Anda belum login. Silakan login terlebih dahulu."))
            
            val response = apiService.getCartItems("Bearer $token")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.success(emptyList())
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to fetch cart items"
                
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
    
    suspend fun addItemToCart(request: AddCartItemRequest): Result<CartItem> {
        return try {
            val token = preferencesManager?.accessToken?.first()
                ?: return Result.failure(Exception("Anda belum login. Silakan login terlebih dahulu."))
            
            val response = apiService.addItemToCart("Bearer $token", request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Failed to add item to cart"))
                }
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                
                when (response.code()) {
                    401 -> Result.failure(TokenExpiredException("Session Anda telah kadaluarsa. Silakan login ulang."))
                    400 -> Result.failure(Exception(errorMessage ?: "Permintaan tidak valid."))
                    404 -> Result.failure(Exception("Produk tidak ditemukan."))
                    else -> Result.failure(Exception(errorMessage ?: "Gagal menambahkan item ke keranjang."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateCartItem(cartItemId: String, request: UpdateCartItemRequest): Result<CartItem> {
        return try {
            val token = preferencesManager?.accessToken?.first()
                ?: return Result.failure(Exception("Anda belum login. Silakan login terlebih dahulu."))
            
            val response = apiService.updateCartItem("Bearer $token", cartItemId, request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Failed to update cart item"))
                }
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                
                when (response.code()) {
                    401 -> Result.failure(TokenExpiredException("Session Anda telah kadaluarsa. Silakan login ulang."))
                    400 -> Result.failure(Exception(errorMessage ?: "Permintaan tidak valid."))
                    404 -> Result.failure(Exception("Item tidak ditemukan."))
                    else -> Result.failure(Exception(errorMessage ?: "Gagal mengupdate item."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removeCartItem(cartItemId: String): Result<Unit> {
        return try {
            val token = preferencesManager?.accessToken?.first()
                ?: return Result.failure(Exception("Anda belum login. Silakan login terlebih dahulu."))
            
            val response = apiService.removeCartItem("Bearer $token", cartItemId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                
                when (response.code()) {
                    401 -> Result.failure(TokenExpiredException("Session Anda telah kadaluarsa. Silakan login ulang."))
                    404 -> Result.failure(Exception("Item tidak ditemukan."))
                    else -> Result.failure(Exception(errorMessage ?: "Gagal menghapus item."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun clearCart(): Result<Unit> {
        return try {
            val token = preferencesManager?.accessToken?.first()
                ?: return Result.failure(Exception("Anda belum login. Silakan login terlebih dahulu."))
            
            val response = apiService.clearCart("Bearer $token")
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMessage ?: "Gagal menghapus keranjang."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) {
            return null
        }
        
        return try {
            val jsonObject = org.json.JSONObject(errorBody)
            when {
                jsonObject.has("error") -> {
                    val errorObj = jsonObject.getJSONObject("error")
                    errorObj.optString("message", null)
                }
                jsonObject.has("message") -> {
                    jsonObject.optString("message", null)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
