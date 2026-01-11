package com.example.myapplication.data.repository

import android.content.Context
import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.data.model.*
import com.example.myapplication.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first
import org.json.JSONObject

class SellerRepository(private val context: Context) {
    private val apiService = ApiClient.sellerApiService
    private val preferencesManager = PreferencesManager(context)
    
    suspend fun createSeller(request: CreateSellerRequest): Result<Seller> {
        return try {
            val token = preferencesManager.accessToken.first()
                ?: return Result.failure(Exception("Anda belum login. Silakan login terlebih dahulu."))
            
            val response = apiService.createSeller("Bearer $token", request)
            if (response.isSuccessful) {
                val responseBody = response.body()
                val isSuccess = responseBody?.success != false && responseBody?.data != null
                if (isSuccess) {
                    responseBody?.data?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Gagal membuat toko: Data tidak valid"))
                } else {
                    val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMessage ?: "Gagal membuat toko"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Session Anda telah kadaluarsa. Silakan login ulang."
                    403 -> "Anda tidak memiliki izin untuk membuat toko."
                    404 -> "Endpoint tidak ditemukan. Pastikan aplikasi sudah terupdate."
                    409 -> parseErrorMessage(response.errorBody()?.string()) ?: "Toko sudah ada. Satu user hanya bisa memiliki satu toko."
                    422 -> parseErrorMessage(response.errorBody()?.string()) ?: "Data yang dimasukkan tidak valid. Periksa kembali form."
                    else -> parseErrorMessage(response.errorBody()?.string()) ?: "Gagal membuat toko. Kode error: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."
                e.message?.contains("timeout") == true -> "Koneksi timeout. Silakan coba lagi."
                e.message?.contains("SocketTimeoutException") == true -> "Waktu koneksi habis. Silakan coba lagi."
                else -> e.message ?: "Terjadi kesalahan: ${e.javaClass.simpleName}"
            }
            Result.failure(Exception(errorMessage))
        }
    }
    
    suspend fun getMySeller(): Result<Seller> {
        return try {
            val token = preferencesManager.accessToken.first()
                ?: return Result.failure(Exception("Anda belum login. Silakan login terlebih dahulu."))
            
            val response = apiService.getMySeller("Bearer $token")
            if (response.isSuccessful) {
                val responseBody = response.body()
                val isSuccess = responseBody?.success != false && responseBody?.data != null
                if (isSuccess) {
                    responseBody?.data?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Gagal memuat data toko: Data tidak valid"))
                } else {
                    val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMessage ?: "Gagal memuat data toko"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Session Anda telah kadaluarsa. Silakan login ulang."
                    403 -> "Anda tidak memiliki izin untuk melihat data toko."
                    404 -> "Anda belum memiliki toko. Silakan buat toko terlebih dahulu."
                    else -> parseErrorMessage(response.errorBody()?.string()) ?: "Gagal memuat data toko. Kode error: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."
                e.message?.contains("timeout") == true -> "Koneksi timeout. Silakan coba lagi."
                e.message?.contains("SocketTimeoutException") == true -> "Waktu koneksi habis. Silakan coba lagi."
                else -> e.message ?: "Terjadi kesalahan: ${e.javaClass.simpleName}"
            }
            Result.failure(Exception(errorMessage))
        }
    }
    
    suspend fun getSeller(sellerId: String): Result<Seller> {
        return try {
            val response = apiService.getSeller(sellerId)
            if (response.isSuccessful) {
                val responseBody = response.body()
                val isSuccess = responseBody?.success != false && responseBody?.data != null
                if (isSuccess) {
                    responseBody?.data?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Gagal memuat data toko: Data tidak valid"))
                } else {
                    val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMessage ?: "Gagal memuat data toko"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Toko tidak ditemukan."
                    500 -> "Terjadi kesalahan di server. Silakan coba lagi nanti."
                    else -> parseErrorMessage(response.errorBody()?.string()) ?: "Gagal memuat data toko. Kode error: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."
                e.message?.contains("timeout") == true -> "Koneksi timeout. Silakan coba lagi."
                e.message?.contains("SocketTimeoutException") == true -> "Waktu koneksi habis. Silakan coba lagi."
                else -> e.message ?: "Terjadi kesalahan: ${e.javaClass.simpleName}"
            }
            Result.failure(Exception(errorMessage))
        }
    }
    
    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) {
            return "An error occurred"
        }
        
        return try {
            val jsonObject = JSONObject(errorBody)
            when {
                jsonObject.has("error") -> {
                    val errorObj = jsonObject.getJSONObject("error")
                    errorObj.optString("message", "An error occurred")
                }
                jsonObject.has("message") -> {
                    jsonObject.optString("message", "An error occurred")
                }
                jsonObject.has("error") && jsonObject.get("error") is String -> {
                    jsonObject.getString("error")
                }
                else -> errorBody
            }
        } catch (e: Exception) {
            errorBody.takeIf { it.length < 200 } ?: "An error occurred"
        }
    }
}