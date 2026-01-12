package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.*
import com.example.myapplication.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CartUiState(
    val isLoading: Boolean = false,
    val cartItems: List<CartItem> = emptyList(),
    val selectedItems: Set<String> = emptySet(), // Set of cart item IDs
    val errorMessage: String? = null,
    val isTokenExpired: Boolean = false,
    val isAdding: Boolean = false,
    val isUpdating: Boolean = false,
    val isRemoving: Boolean = false
)

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CartRepository(application)
    
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
    
    fun loadCart() {
        viewModelScope.launch {
            val currentIsAdding = _uiState.value.isAdding
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isTokenExpired = false
            )
            
            repository.getCartItems().fold(
                onSuccess = { items ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cartItems = items,
                        errorMessage = null,
                        isAdding = currentIsAdding // Preserve isAdding state
                    )
                },
                onFailure = { exception ->
                    val isTokenExpired = exception is TokenExpiredException
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load cart",
                        isTokenExpired = isTokenExpired,
                        isAdding = currentIsAdding // Preserve isAdding state
                    )
                }
            )
        }
    }
    
    fun addItemToCart(productId: String, quantity: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAdding = true,
                errorMessage = null
            )
            
            repository.addItemToCart(AddCartItemRequest(productId, quantity)).fold(
                onSuccess = { cartItem ->
                    // Set isAdding to false first
                    _uiState.value = _uiState.value.copy(isAdding = false)
                    // Reload cart to get updated list
                    loadCart()
                },
                onFailure = { exception ->
                    val isTokenExpired = exception is TokenExpiredException
                    _uiState.value = _uiState.value.copy(
                        isAdding = false,
                        errorMessage = exception.message ?: "Failed to add item to cart",
                        isTokenExpired = isTokenExpired
                    )
                }
            )
        }
    }
    
    fun updateQuantity(cartItemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            // If quantity is 0 or less, remove the item
            removeItem(cartItemId)
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdating = true,
                errorMessage = null
            )
            
            repository.updateCartItem(cartItemId, UpdateCartItemRequest(newQuantity)).fold(
                onSuccess = { updatedItem ->
                    // Update local state
                    val updatedItems = _uiState.value.cartItems.map { item ->
                        if (item.id == cartItemId) updatedItem else item
                    }
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        cartItems = updatedItems
                    )
                },
                onFailure = { exception ->
                    val isTokenExpired = exception is TokenExpiredException
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = exception.message ?: "Failed to update quantity",
                        isTokenExpired = isTokenExpired
                    )
                    // Reload cart on error
                    loadCart()
                }
            )
        }
    }
    
    fun incrementQuantity(cartItemId: String) {
        val currentItem = _uiState.value.cartItems.find { it.id == cartItemId }
        currentItem?.let {
            updateQuantity(cartItemId, it.quantity + 1)
        }
    }
    
    fun decrementQuantity(cartItemId: String) {
        val currentItem = _uiState.value.cartItems.find { it.id == cartItemId }
        currentItem?.let {
            if (it.quantity > 1) {
                updateQuantity(cartItemId, it.quantity - 1)
            } else {
                // If quantity is 1, remove item when decremented
                removeItem(cartItemId)
            }
        }
    }
    
    fun removeItem(cartItemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRemoving = true,
                errorMessage = null
            )
            
            repository.removeCartItem(cartItemId).fold(
                onSuccess = {
                    // Remove from local state
                    val updatedItems = _uiState.value.cartItems.filter { it.id != cartItemId }
                    val updatedSelected = _uiState.value.selectedItems - cartItemId
                    _uiState.value = _uiState.value.copy(
                        isRemoving = false,
                        cartItems = updatedItems,
                        selectedItems = updatedSelected
                    )
                },
                onFailure = { exception ->
                    val isTokenExpired = exception is TokenExpiredException
                    _uiState.value = _uiState.value.copy(
                        isRemoving = false,
                        errorMessage = exception.message ?: "Failed to remove item",
                        isTokenExpired = isTokenExpired
                    )
                    // Reload cart on error
                    loadCart()
                }
            )
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            repository.clearCart().fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cartItems = emptyList(),
                        selectedItems = emptySet()
                    )
                },
                onFailure = { exception ->
                    val isTokenExpired = exception is TokenExpiredException
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to clear cart",
                        isTokenExpired = isTokenExpired
                    )
                }
            )
        }
    }
    
    fun toggleItemSelection(cartItemId: String) {
        val currentSelected = _uiState.value.selectedItems
        _uiState.value = _uiState.value.copy(
            selectedItems = if (currentSelected.contains(cartItemId)) {
                currentSelected - cartItemId
            } else {
                currentSelected + cartItemId
            }
        )
    }
    
    fun selectAll() {
        _uiState.value = _uiState.value.copy(
            selectedItems = _uiState.value.cartItems.map { it.id }.toSet()
        )
    }
    
    fun deselectAll() {
        _uiState.value = _uiState.value.copy(
            selectedItems = emptySet()
        )
    }
    
    fun getSelectedItems(): List<CartItem> {
        return _uiState.value.cartItems.filter { 
            _uiState.value.selectedItems.contains(it.id) 
        }
    }
    
    fun getTotalPrice(): Int {
        return getSelectedItems().sumOf { it.price * it.quantity }
    }
    
    fun getTotalQuantity(): Int {
        return getSelectedItems().sumOf { it.quantity }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Remove cart items by product IDs (used when order is successfully paid)
     */
    fun removeItemsByProductIds(productIds: List<String>) {
        viewModelScope.launch {
            val itemsToRemove = _uiState.value.cartItems.filter { 
                productIds.contains(it.productId) 
            }
            
            // Remove each item
            itemsToRemove.forEach { item ->
                removeItem(item.id)
            }
        }
    }
}
