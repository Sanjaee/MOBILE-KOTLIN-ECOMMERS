package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val orders: List<CheckoutOrder> = emptyList(),
    val selectedShippingAddress: ShippingAddress? = null,
    val shippingMethods: List<ShippingMethod> = emptyList(),
    val selectedShippingMethod: ShippingMethod? = null,
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val useInsurance: Boolean = false,
    val useWarrantyProtection: Boolean = false,
    val note: String? = null,
    val bonus: Int = 1700,
    val summary: CheckoutSummary? = null,
    val errorMessage: String? = null
)

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()
    
    fun initializeCheckout(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            // Create checkout order from product
            // Untuk demo, kita buat discount 20% (original price lebih tinggi)
            val originalPrice = (product.price * 1.2).toInt() // Statik: harga asli 20% lebih tinggi untuk show discount
            val checkoutItem = CheckoutItem(
                product = product,
                quantity = quantity,
                price = product.price,
                originalPrice = originalPrice // Original price untuk perhitungan discount
            )
            
            val discountAmount = checkoutItem.getDiscount() ?: 0
            
            val order = CheckoutOrder(
                id = "ORDER_${System.currentTimeMillis()}",
                sellerName = "KAHF_NEW", // Statik: seller name seperti di gambar
                items = listOf(checkoutItem),
                subtotal = checkoutItem.getSubtotal(),
                shippingCost = 0,
                insuranceCost = 0,
                warrantyCost = 0,
                totalDiscount = discountAmount,
                total = checkoutItem.getSubtotal()
            )
            
            // Initialize default shipping methods
            val defaultShippingMethods = listOf(
                ShippingMethod(
                    id = "REGULER_1",
                    name = "Reguler",
                    carrier = "J&T",
                    cost = 7000,
                    estimatedDays = "12 - 14 Jan",
                    hasInsurance = true
                ),
                ShippingMethod(
                    id = "EXPRESS_1",
                    name = "Express",
                    carrier = "JNE",
                    cost = 15000,
                    estimatedDays = "8 - 10 Jan",
                    hasInsurance = true
                )
            )
            
            // Initialize payment methods
            val defaultPaymentMethods = listOf(
                PaymentMethod(
                    id = "BCA_VA",
                    name = "BCA Virtual Account",
                    type = "virtual_account"
                ),
                PaymentMethod(
                    id = "ALFAMART",
                    name = "Alfamart / Alfamidi / Lawson / Dan+Dan",
                    type = "alfamart"
                ),
                PaymentMethod(
                    id = "MANDIRI_VA",
                    name = "Mandiri Virtual Account",
                    type = "virtual_account"
                ),
                PaymentMethod(
                    id = "BRI_VA",
                    name = "BRI Virtual Account",
                    type = "virtual_account"
                ),
                PaymentMethod(
                    id = "QRIS",
                    name = "QRIS",
                    type = "qris"
                )
            )
            
            // Initialize default shipping address (mock data)
            val defaultAddress = ShippingAddress(
                id = "ADDR_1",
                label = "Rumah",
                recipientName = "Ahmad",
                phone = "+6281234567890",
                addressLine1 = "JL.PELITA RT07/RW01 KONTRAKAN HJ.KEPOY",
                city = "Jakarta",
                province = "DKI Jakarta",
                postalCode = "12345",
                isDefault = true
            )
            
            _uiState.value = _uiState.value.copy(
                orders = listOf(order),
                selectedShippingAddress = defaultAddress,
                shippingMethods = defaultShippingMethods,
                selectedShippingMethod = defaultShippingMethods.firstOrNull(),
                paymentMethods = defaultPaymentMethods,
                selectedPaymentMethod = defaultPaymentMethods.firstOrNull(),
                summary = calculateSummary(
                    order = order,
                    shippingMethod = defaultShippingMethods.firstOrNull(),
                    useInsurance = false,
                    useWarrantyProtection = false,
                    bonus = 1700
                )
            )
        }
    }
    
    fun updateQuantity(orderId: String, itemIndex: Int, newQuantity: Int) {
        if (newQuantity < 1) return
        
        val updatedOrders = _uiState.value.orders.map { order ->
            if (order.id == orderId) {
                val updatedItems = order.items.mapIndexed { index, item ->
                    if (index == itemIndex) {
                        item.copy(quantity = newQuantity)
                    } else {
                        item
                    }
                }
                val newSubtotal = updatedItems.sumOf { it.getSubtotal() }
                order.copy(
                    items = updatedItems,
                    subtotal = newSubtotal,
                    total = calculateTotal(order, newSubtotal)
                )
            } else {
                order
            }
        }
        
        updateOrders(updatedOrders)
    }
    
    fun selectShippingMethod(method: ShippingMethod) {
        _uiState.value = _uiState.value.copy(
            selectedShippingMethod = method,
            summary = calculateSummary(
                order = _uiState.value.orders.firstOrNull(),
                shippingMethod = method,
                useInsurance = _uiState.value.useInsurance,
                useWarrantyProtection = _uiState.value.useWarrantyProtection,
                bonus = _uiState.value.bonus
            )
        )
    }
    
    fun toggleInsurance(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            useInsurance = enabled,
            summary = calculateSummary(
                order = _uiState.value.orders.firstOrNull(),
                shippingMethod = _uiState.value.selectedShippingMethod,
                useInsurance = enabled,
                useWarrantyProtection = _uiState.value.useWarrantyProtection,
                bonus = _uiState.value.bonus
            )
        )
    }
    
    fun toggleWarrantyProtection(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            useWarrantyProtection = enabled,
            summary = calculateSummary(
                order = _uiState.value.orders.firstOrNull(),
                shippingMethod = _uiState.value.selectedShippingMethod,
                useInsurance = _uiState.value.useInsurance,
                useWarrantyProtection = enabled,
                bonus = _uiState.value.bonus
            )
        )
    }
    
    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethod = method)
    }
    
    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }
    
    private fun updateOrders(orders: List<CheckoutOrder>) {
        val firstOrder = orders.firstOrNull()
        _uiState.value = _uiState.value.copy(
            orders = orders,
            summary = calculateSummary(
                order = firstOrder,
                shippingMethod = _uiState.value.selectedShippingMethod,
                useInsurance = _uiState.value.useInsurance,
                useWarrantyProtection = _uiState.value.useWarrantyProtection,
                bonus = _uiState.value.bonus
            )
        )
    }
    
    private fun calculateSummary(
        order: CheckoutOrder?,
        shippingMethod: ShippingMethod?,
        useInsurance: Boolean,
        useWarrantyProtection: Boolean,
        bonus: Int
    ): CheckoutSummary? {
        if (order == null) return null
        
        val subtotal = order.items.sumOf { it.getSubtotal() }
        val shippingCost = shippingMethod?.cost ?: 0
        val insuranceCost = if (useInsurance && shippingMethod?.hasInsurance == true) {
            shippingMethod.insuranceCost
        } else {
            0
        }
        val warrantyCost = if (useWarrantyProtection) {
            order.items.sumOf { it.quantity * order.warrantyCostPerItem }
        } else {
            0
        }
        val totalDiscount = order.items.sumOf { it.getDiscount() ?: 0 }
        val serviceFee = 1000 // Statik: Biaya Layanan
        val applicationFee = 0 // Statik: Biaya Jasa Aplikasi (Diskon)
        val totalItems = order.items.sumOf { it.quantity }
        val total = subtotal + shippingCost + insuranceCost + warrantyCost + serviceFee + applicationFee - bonus - totalDiscount
        val savings = totalDiscount
        
        return CheckoutSummary(
            subtotal = subtotal,
            shippingCost = shippingCost,
            insuranceCost = insuranceCost,
            warrantyCost = warrantyCost,
            serviceFee = serviceFee,
            applicationFee = applicationFee,
            totalDiscount = totalDiscount,
            bonus = bonus,
            total = total,
            savings = savings,
            totalItems = totalItems
        )
    }
    
    private fun calculateTotal(order: CheckoutOrder, newSubtotal: Int): Int {
        val shippingCost = _uiState.value.selectedShippingMethod?.cost ?: 0
        val insuranceCost = if (_uiState.value.useInsurance) {
            _uiState.value.selectedShippingMethod?.insuranceCost ?: 0
        } else {
            0
        }
        val warrantyCost = if (_uiState.value.useWarrantyProtection) {
            order.items.sumOf { it.quantity * order.warrantyCostPerItem }
        } else {
            0
        }
        val serviceFee = 1000 // Statik
        val applicationFee = 0 // Statik
        val totalDiscount = order.items.sumOf { it.getDiscount() ?: 0 }
        return newSubtotal + shippingCost + insuranceCost + warrantyCost + serviceFee + applicationFee - _uiState.value.bonus - totalDiscount
    }
}
