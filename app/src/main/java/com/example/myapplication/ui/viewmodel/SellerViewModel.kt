package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.CreateSellerRequest
import com.example.myapplication.data.model.Seller
import com.example.myapplication.data.repository.SellerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SellerUiState(
    val isLoading: Boolean = false,
    val seller: Seller? = null,
    val errorMessage: String? = null,
    val isCreateSuccess: Boolean = false,
    val createdSellerId: String? = null,
    val hasStore: Boolean = false,
    val isCheckingStore: Boolean = false
)

class SellerViewModel(application: Application) : AndroidViewModel(application) {
    private val sellerRepository = SellerRepository(application)
    
    private val _uiState = MutableStateFlow(SellerUiState())
    val uiState: StateFlow<SellerUiState> = _uiState.asStateFlow()
    
    fun createSeller(request: CreateSellerRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isCreateSuccess = false
            )
            
            sellerRepository.createSeller(request).fold(
                onSuccess = { seller ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        seller = seller,
                        isCreateSuccess = true,
                        createdSellerId = seller.id
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to create store"
                    )
                }
            )
        }
    }
    
    fun getMySeller() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            sellerRepository.getMySeller().fold(
                onSuccess = { seller ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        seller = seller
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load store"
                    )
                }
            )
        }
    }
    
    fun getSeller(sellerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            sellerRepository.getSeller(sellerId).fold(
                onSuccess = { seller ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        seller = seller
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load store"
                    )
                }
            )
        }
    }
    
    fun resetCreateSuccess() {
        _uiState.value = _uiState.value.copy(
            isCreateSuccess = false,
            createdSellerId = null
        )
    }
    
    fun checkHasStore() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCheckingStore = true,
                errorMessage = null
            )
            
            sellerRepository.getMySeller().fold(
                onSuccess = { seller ->
                    _uiState.value = _uiState.value.copy(
                        isCheckingStore = false,
                        hasStore = true,
                        seller = seller
                    )
                },
                onFailure = { exception ->
                    // If 404 or "belum memiliki toko", user doesn't have a store yet
                    val message = exception.message ?: ""
                    val hasStore = !message.contains("404") && 
                                  !message.contains("belum memiliki toko") &&
                                  !message.contains("Seller not found")
                    _uiState.value = _uiState.value.copy(
                        isCheckingStore = false,
                        hasStore = hasStore,
                        errorMessage = if (hasStore) exception.message else null
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}