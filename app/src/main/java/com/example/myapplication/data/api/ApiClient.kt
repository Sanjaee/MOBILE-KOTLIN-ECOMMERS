package com.example.myapplication.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://api.zascript.com/"
    
    /**
     * Helper function to get full image URL
     * If imageUrl is null or empty, returns null
     * If imageUrl is already absolute (starts with http:// or https://), returns as is
     * Fixes common typos like "hhttps://" -> "https://"
     * Otherwise, combines with BASE_URL
     */
    fun getImageUrl(imageUrl: String?): String? {
        if (imageUrl.isNullOrBlank()) return null
        
        // Fix common typo: "hhttps://" -> "https://"
        var fixedUrl = imageUrl.trim()
        if (fixedUrl.startsWith("hhttps://")) {
            fixedUrl = "https://" + fixedUrl.substring(9) // Remove "hhttps://" and add "https://"
        }
        
        return if (fixedUrl.startsWith("http://") || fixedUrl.startsWith("https://")) {
            fixedUrl
        } else {
            // Remove leading slash if present to avoid double slashes
            val path = if (fixedUrl.startsWith("/")) fixedUrl.substring(1) else fixedUrl
            BASE_URL + path
        }
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
    val productApiService: ProductApiService = retrofit.create(ProductApiService::class.java)
    val paymentApiService: PaymentApiService = retrofit.create(PaymentApiService::class.java)
    val orderApiService: OrderApiService = retrofit.create(OrderApiService::class.java)
    val sellerApiService: SellerApiService = retrofit.create(SellerApiService::class.java)
    val cartApiService: CartApiService = retrofit.create(CartApiService::class.java)
}

