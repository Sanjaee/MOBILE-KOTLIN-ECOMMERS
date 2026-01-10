package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiClient
import com.example.myapplication.data.model.*

class ProductRepository {
    private val apiService = ApiClient.productApiService
    
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
}
