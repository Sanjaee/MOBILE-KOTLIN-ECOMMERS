package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.*
import com.example.myapplication.data.model.CheckoutOrder
import com.example.myapplication.data.preferences.PreferencesManager
import com.example.myapplication.data.repository.OrderRepository
import com.example.myapplication.data.repository.PaymentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PaymentUiState(
    val isLoading: Boolean = false,
    val payment: Payment? = null,
    val order: Order? = null,
    val errorMessage: String? = null,
    val isTokenExpired: Boolean = false,
    val shouldLogout: Boolean = false,
    val isPolling: Boolean = false,
    val countdownSeconds: Long = 0
)

class PaymentViewModel(application: Application) : AndroidViewModel(application) {
    private val paymentRepository = PaymentRepository()
    private val orderRepository = OrderRepository()
    private val preferencesManager = PreferencesManager(application)
    
    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()
    
    private var pollingJob: Job? = null
    
    fun createOrderAndPayment(
        checkoutOrder: CheckoutOrder,
        shippingAddressId: String,
        paymentMethod: String,
        bank: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isTokenExpired = false,
                shouldLogout = false
            )
            
            val token = preferencesManager.accessToken.first()
            if (token == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isTokenExpired = true,
                    shouldLogout = true,
                    errorMessage = "Session expired. Please login again."
                )
                return@launch
            }
            
            try {
                // 1. Create Order first
                val orderItems = checkoutOrder.items.map { item ->
                    CreateOrderItemRequest(
                        productId = item.product.id,
                        quantity = item.quantity,
                        price = item.price
                    )
                }
                
                // Calculate service fee properly
                // Total = subtotal + shipping + insurance + warranty + serviceFee - discount - bonus
                // So: serviceFee = total - subtotal - shipping - insurance - warranty + discount + bonus
                val actualInsuranceCost = if (checkoutOrder.useInsurance) checkoutOrder.insuranceCost else 0
                val actualWarrantyCost = if (checkoutOrder.useWarrantyProtection) checkoutOrder.warrantyCost else 0
                val serviceFee = maxOf(0, checkoutOrder.total - checkoutOrder.subtotal - checkoutOrder.shippingCost - actualInsuranceCost - actualWarrantyCost + checkoutOrder.totalDiscount + checkoutOrder.bonus)
                
                val createOrderResult = orderRepository.createOrder(
                    shippingAddressId = shippingAddressId,
                    orderItems = orderItems,
                    shippingCost = checkoutOrder.shippingCost,
                    insuranceCost = if (checkoutOrder.useInsurance) checkoutOrder.insuranceCost else 0,
                    warrantyCost = if (checkoutOrder.useWarrantyProtection) checkoutOrder.warrantyCost else 0,
                    serviceFee = serviceFee,
                    applicationFee = 0,
                    totalDiscount = checkoutOrder.totalDiscount,
                    bonus = checkoutOrder.bonus,
                    notes = checkoutOrder.note,
                    token = token
                )
                
                when (val order = createOrderResult.getOrNull()) {
                    null -> {
                        val exception = createOrderResult.exceptionOrNull()
                        val isTokenExpired = exception is TokenExpiredException
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception?.message ?: "Failed to create order",
                            isTokenExpired = isTokenExpired,
                            shouldLogout = isTokenExpired
                        )
                    }
                    else -> {
                        // 2. Create Payment with order ID
                        val paymentMethodMap = mapOf(
                            "virtual_account" to "bank_transfer",
                            "alfamart" to "alfamart",
                            "gopay" to "gopay",
                            "qris" to "qris",
                            "credit_card" to "credit_card"
                        )
                        
                        val backendPaymentMethod = paymentMethodMap[paymentMethod] ?: "bank_transfer"
                        
                        val createPaymentResult = paymentRepository.createPayment(
                            orderId = order.id,
                            paymentMethod = backendPaymentMethod,
                            bank = bank?.lowercase(),
                            token = token
                        )
                        
                        when (val payment = createPaymentResult.getOrNull()) {
                            null -> {
                                val exception = createPaymentResult.exceptionOrNull()
                                val isTokenExpired = exception is TokenExpiredException
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = exception?.message ?: "Failed to create payment",
                                    isTokenExpired = isTokenExpired,
                                    shouldLogout = isTokenExpired,
                                    order = order
                                )
                            }
                            else -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    payment = payment,
                                    order = order,
                                    countdownSeconds = calculateCountdown(payment.expiryTime)
                                )
                                
                                // Start polling if payment is pending
                                if (payment.status == PaymentStatus.PENDING) {
                                    startPolling(payment.id, token)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "An error occurred"
                )
            }
        }
    }
    
    fun loadPayment(paymentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isTokenExpired = false,
                shouldLogout = false
            )
            
            val token = preferencesManager.accessToken.first()
            if (token == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isTokenExpired = true,
                    shouldLogout = true,
                    errorMessage = "Session expired. Please login again."
                )
                return@launch
            }
            
            paymentRepository.getPayment(paymentId, token).fold(
                onSuccess = { payment ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        payment = payment,
                        countdownSeconds = calculateCountdown(payment.expiryTime)
                    )
                    
                    // Start polling if payment is pending
                    if (payment.status == PaymentStatus.PENDING) {
                        startPolling(payment.id, token)
                    }
                },
                onFailure = { exception ->
                    val isTokenExpired = exception is TokenExpiredException
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load payment",
                        isTokenExpired = isTokenExpired,
                        shouldLogout = isTokenExpired
                    )
                }
            )
        }
    }
    
    fun checkPaymentStatus(paymentId: String) {
        viewModelScope.launch {
            val token = preferencesManager.accessToken.first()
            if (token == null) {
                _uiState.value = _uiState.value.copy(
                    isTokenExpired = true,
                    errorMessage = "Session expired. Please login again."
                )
                return@launch
            }
            
            paymentRepository.checkPaymentStatus(paymentId, token).fold(
                onSuccess = { payment ->
                    _uiState.value = _uiState.value.copy(
                        payment = payment,
                        countdownSeconds = calculateCountdown(payment.expiryTime)
                    )
                    
                    // Stop polling if payment is no longer pending
                    if (payment.status != PaymentStatus.PENDING) {
                        stopPolling()
                    }
                },
                onFailure = { exception ->
                    val isTokenExpired = exception is TokenExpiredException
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Failed to check payment status",
                        isTokenExpired = isTokenExpired,
                        shouldLogout = isTokenExpired
                    )
                    
                    if (isTokenExpired) {
                        stopPolling()
                    }
                }
            )
        }
    }
    
    private fun startPolling(paymentId: String, token: String) {
        stopPolling() // Stop any existing polling
        
        _uiState.value = _uiState.value.copy(isPolling = true)
        
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(5000) // Poll every 5 seconds
                
                val currentPayment = _uiState.value.payment
                if (currentPayment?.status != PaymentStatus.PENDING) {
                    break
                }
                
                paymentRepository.checkPaymentStatus(paymentId, token).fold(
                    onSuccess = { payment ->
                        _uiState.value = _uiState.value.copy(
                            payment = payment,
                            countdownSeconds = calculateCountdown(payment.expiryTime)
                        )
                        
                        if (payment.status != PaymentStatus.PENDING) {
                            stopPolling()
                        }
                    },
                    onFailure = {
                        // Continue polling on error, but don't update state
                    }
                )
            }
        }
    }
    
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        _uiState.value = _uiState.value.copy(isPolling = false)
    }
    
    private fun calculateCountdown(expiryTime: String?): Long {
        if (expiryTime == null) return 0
        
        return try {
            // Parse ISO 8601 format: "2026-01-11T22:02:00Z" or similar
            val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
            val expiry = java.time.ZonedDateTime.parse(expiryTime, formatter)
            val now = java.time.ZonedDateTime.now()
            val duration = java.time.Duration.between(now, expiry)
            duration.seconds.coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
