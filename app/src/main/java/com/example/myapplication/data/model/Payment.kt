package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Payment(
    val id: String,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("order_uuid") val orderUuid: String,
    @SerializedName("midtrans_transaction_id") val midtransTransactionId: String? = null,
    val amount: Int,
    @SerializedName("total_amount") val totalAmount: Int,
    val status: PaymentStatus,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("payment_type") val paymentType: String = "midtrans",
    @SerializedName("fraud_status") val fraudStatus: String? = null,
    @SerializedName("va_number") val vaNumber: String? = null,
    @SerializedName("bank_type") val bankType: String? = null,
    @SerializedName("qr_code_url") val qrCodeUrl: String? = null,
    @SerializedName("expiry_time") val expiryTime: String? = null,
    @SerializedName("midtrans_response") val midtransResponse: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val order: Order? = null
)

enum class PaymentStatus {
    @SerializedName("pending")
    PENDING,
    
    @SerializedName("success")
    SUCCESS,
    
    @SerializedName("failed")
    FAILED,
    
    @SerializedName("cancelled")
    CANCELLED,
    
    @SerializedName("expired")
    EXPIRED
}

data class CreatePaymentRequest(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("payment_method") val paymentMethod: String,
    val bank: String? = null // For bank_transfer: "bca", "bni", "mandiri", etc
)
