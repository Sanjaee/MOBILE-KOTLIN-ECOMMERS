package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.*
import com.example.myapplication.data.preferences.PreferencesManager
import com.example.myapplication.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class OrderUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val total: Long = 0,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
    val isTokenExpired: Boolean = false
)

class OrderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OrderRepository()
    private val preferencesManager = PreferencesManager(application)
    
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()
    
    fun loadOrders(
        page: Int = 1,
        limit: Int = 10,
        status: String? = null,
        paymentStatus: String? = null,
        append: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isTokenExpired = false
            )
            
            val token = preferencesManager.accessToken.first()
            if (token == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Session expired. Please login again.",
                    isTokenExpired = true
                )
                return@launch
            }
            
            repository.getOrders(page, limit, status, paymentStatus, token).fold(
                onSuccess = { response ->
                    val newOrders = if (append) {
                        _uiState.value.orders + response.orders
                    } else {
                        response.orders
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orders = newOrders,
                        total = response.total,
                        currentPage = response.page,
                        hasMore = (response.page * response.limit) < response.total,
                        errorMessage = null
                    )
                },
                onFailure = { exception ->
                    val isTokenExpired = exception is TokenExpiredException
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load orders",
                        isTokenExpired = isTokenExpired
                    )
                }
            )
        }
    }
    
    fun refreshOrders(status: String? = null, paymentStatus: String? = null) {
        loadOrders(page = 1, limit = 10, status = status, paymentStatus = paymentStatus, append = false)
    }
}
