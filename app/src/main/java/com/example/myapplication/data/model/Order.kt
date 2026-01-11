package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Order(
    val id: String,
    @SerializedName("order_number") val orderNumber: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("shipping_address_id") val shippingAddressId: String,
    val subtotal: Int,
    @SerializedName("shipping_cost") val shippingCost: Int,
    @SerializedName("insurance_cost") val insuranceCost: Int,
    @SerializedName("warranty_cost") val warrantyCost: Int,
    @SerializedName("service_fee") val serviceFee: Int,
    @SerializedName("application_fee") val applicationFee: Int,
    @SerializedName("total_discount") val totalDiscount: Int,
    val bonus: Int,
    @SerializedName("total_amount") val totalAmount: Int,
    val status: String, // pending, processing, shipped, delivered, cancelled
    val notes: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("order_items") val orderItems: List<OrderItem> = emptyList(),
    @SerializedName("shipping_address") val shippingAddress: OrderShippingAddress? = null,
    val payment: Payment? = null
)

// ShippingAddress model for Order (from API response)
data class OrderShippingAddress(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val label: String,
    @SerializedName("recipient_name") val recipientName: String,
    val phone: String,
    @SerializedName("address_line1") val addressLine1: String,
    val city: String,
    val province: String,
    @SerializedName("postal_code") val postalCode: String,
    @SerializedName("is_default") val isDefault: Boolean = false
) {
    fun getFullAddress(): String {
        return "$addressLine1, $city, $province $postalCode"
    }
}

data class OrderItem(
    val id: String,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("product_name") val productName: String,
    val quantity: Int,
    val price: Int,
    val subtotal: Int,
    @SerializedName("created_at") val createdAt: String,
    val product: Product? = null
)

data class OrdersListResponse(
    @SerializedName("orders")
    val orders: List<Order>,
    
    @SerializedName("total")
    val total: Long,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("limit")
    val limit: Int
)

data class CreateOrderRequest(
    @SerializedName("shipping_address_id") val shippingAddressId: String,
    @SerializedName("order_items") val orderItems: List<CreateOrderItemRequest>,
    val subtotal: Int, // Required by backend
    @SerializedName("shipping_cost") val shippingCost: Int,
    @SerializedName("insurance_cost") val insuranceCost: Int,
    @SerializedName("warranty_cost") val warrantyCost: Int,
    @SerializedName("service_fee") val serviceFee: Int,
    @SerializedName("application_fee") val applicationFee: Int,
    @SerializedName("total_discount") val totalDiscount: Int,
    val bonus: Int,
    val notes: String? = null
)

data class CreateOrderItemRequest(
    @SerializedName("product_id") val productId: String,
    val quantity: Int,
    val price: Int
)
