package com.example.myapplication.data.model

data class ShippingAddress(
    val id: String,
    val label: String, // "Rumah", "Kantor", dll
    val recipientName: String,
    val phone: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String,
    val province: String,
    val postalCode: String,
    val isDefault: Boolean = false
) {
    fun getFullAddress(): String {
        val line2 = addressLine2?.let { ", $it" } ?: ""
        return "$addressLine1$line2, $city, $province $postalCode"
    }
}

data class CheckoutItem(
    val product: Product,
    val quantity: Int,
    val price: Int, // Price at checkout time
    val originalPrice: Int? = null // Original price if discounted
) {
    fun getSubtotal(): Int = price * quantity
    fun getDiscount(): Int? = originalPrice?.let { (it - price) * quantity }
}

data class ShippingMethod(
    val id: String,
    val name: String, // "Reguler", "Express", dll
    val carrier: String, // "J&T", "JNE", dll
    val cost: Int,
    val estimatedDays: String, // "12 - 14 Jan"
    val hasInsurance: Boolean = false,
    val insuranceCost: Int = 300
)

data class PaymentMethod(
    val id: String,
    val name: String, // "BCA Virtual Account", dll
    val logo: String? = null,
    val type: String // "virtual_account", "alfamart", dll
)

data class CheckoutOrder(
    val id: String,
    val sellerName: String,
    val items: List<CheckoutItem>,
    val shippingMethod: ShippingMethod? = null,
    val shippingAddress: ShippingAddress? = null,
    val useInsurance: Boolean = false,
    val useWarrantyProtection: Boolean = false,
    val warrantyCostPerItem: Int = 1100, // Default warranty cost per item
    val note: String? = null,
    val bonus: Int = 0,
    val subtotal: Int,
    val shippingCost: Int,
    val insuranceCost: Int,
    val warrantyCost: Int, // Actual warranty cost calculated
    val totalDiscount: Int,
    val total: Int
)

data class CheckoutSummary(
    val subtotal: Int,
    val shippingCost: Int,
    val insuranceCost: Int,
    val warrantyCost: Int,
    val serviceFee: Int = 1000, // Biaya Layanan (statik)
    val applicationFee: Int = 0, // Biaya Jasa Aplikasi (statik)
    val totalDiscount: Int,
    val bonus: Int,
    val total: Int,
    val savings: Int, // Total savings
    val totalItems: Int = 1 // Total barang (statik untuk demo)
)
