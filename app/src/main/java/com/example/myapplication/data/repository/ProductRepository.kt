package com.example.myapplication.data.repository

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.data.model.*
import com.example.myapplication.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProductRepository(private val context: Context? = null) {
    private val apiService = ApiClient.productApiService
    private val preferencesManager = context?.let { PreferencesManager(it) }
    
    suspend fun getProducts(
        page: Int = 1,
        limit: Int = 10,
        categoryId: String? = null,
        featured: Boolean? = null,
        activeOnly: Boolean? = null
    ): Result<ProductListResponse> {
        return try {
            val featuredStr = featured?.let { if (it) "true" else "false" }
            val activeOnlyStr = activeOnly?.let { if (it) "true" else "false" }
            
            val response = apiService.getProducts(
                page = page,
                limit = limit,
                categoryId = categoryId,
                featured = featuredStr,
                activeOnly = activeOnlyStr
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("No data received"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = response.body()?.message ?: "Failed to fetch products"
                
                // Check if token expired
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
    
    suspend fun getProductById(id: String): Result<Product> {
        return try {
            val response = apiService.getProductById(id)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Product not found"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to fetch product"
                
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
    
    suspend fun getCategories(activeOnly: Boolean? = null): Result<List<Category>> {
        return try {
            val activeOnlyStr = activeOnly?.let { if (it) "true" else "false" }
            val response = apiService.getCategories(activeOnly = activeOnlyStr)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("No categories found"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to fetch categories"
                
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
    
    suspend fun createProduct(request: CreateProductRequest): Result<Product> {
        return try {
            val token = preferencesManager?.accessToken?.first()
                ?: return Result.failure(Exception("Anda belum login. Silakan login terlebih dahulu."))
            
            val response = apiService.createProduct("Bearer $token", request)
            if (response.isSuccessful) {
                val responseBody = response.body()
                val isSuccess = responseBody?.success != false && responseBody?.data != null
                if (isSuccess) {
                    responseBody?.data?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Gagal membuat produk: Data tidak valid"))
                } else {
                    val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    Result.failure(Exception(errorMessage ?: "Gagal membuat produk"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Session Anda telah kadaluarsa. Silakan login ulang."
                    403 -> "Anda tidak memiliki izin untuk membuat produk. Pastikan Anda sudah memiliki toko."
                    404 -> "Kategori tidak ditemukan."
                    409 -> parseErrorMessage(response.errorBody()?.string()) ?: "SKU sudah digunakan. Gunakan SKU yang berbeda."
                    422 -> parseErrorMessage(response.errorBody()?.string()) ?: "Data yang dimasukkan tidak valid. Periksa kembali form."
                    else -> parseErrorMessage(response.errorBody()?.string()) ?: "Gagal membuat produk. Kode error: ${response.code()}"
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
    
    suspend fun uploadProductImages(productId: String, imageUris: List<Uri>): Result<UploadImagesResponse> {
        return try {
            val token = preferencesManager?.accessToken?.first()
                ?: return Result.failure(Exception("Anda belum login. Silakan login terlebih dahulu."))

            val imageParts = imageUris.mapNotNull { uri ->
                context?.contentResolver?.openInputStream(uri)?.use { inputStream ->
                    // Get MIME type from ContentResolver
                    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                    
                    // Determine extension from MIME type
                    val extension = when {
                        mimeType.contains("jpeg", ignoreCase = true) || mimeType.contains("jpg", ignoreCase = true) -> ".jpg"
                        mimeType.contains("png", ignoreCase = true) -> ".png"
                        mimeType.contains("webp", ignoreCase = true) -> ".webp"
                        mimeType.contains("gif", ignoreCase = true) -> ".gif"
                        else -> ".jpg" // Default to jpg if cannot determine
                    }
                    
                    val tempFile = File.createTempFile("upload_", extension, context.cacheDir)
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("images", tempFile.name, requestFile)
                }
            }

            if (imageParts.isEmpty()) {
                return Result.failure(Exception("Tidak ada gambar yang valid untuk diunggah."))
            }

            val response = apiService.uploadProductImages("Bearer $token", productId, imageParts)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Gagal mengunggah gambar: Data tidak valid"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Session Anda telah kadaluarsa. Silakan login ulang."
                    403 -> "Anda tidak memiliki izin untuk mengunggah gambar."
                    404 -> "Produk tidak ditemukan."
                    400 -> parseErrorMessage(response.errorBody()?.string()) ?: "Permintaan tidak valid."
                    else -> parseErrorMessage(response.errorBody()?.string()) ?: "Gagal mengunggah gambar. Kode error: ${response.code()}"
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
            return "Terjadi kesalahan"
        }
        
        return try {
            val jsonObject = JSONObject(errorBody)
            when {
                jsonObject.has("error") -> {
                    val errorObj = jsonObject.getJSONObject("error")
                    errorObj.optString("message", "Terjadi kesalahan")
                }
                jsonObject.has("message") -> {
                    jsonObject.optString("message", "Terjadi kesalahan")
                }
                jsonObject.has("error") && jsonObject.get("error") is String -> {
                    jsonObject.getString("error")
                }
                else -> errorBody
            }
        } catch (e: Exception) {
            errorBody.takeIf { it.length < 200 } ?: "Terjadi kesalahan"
        }
    }
}
