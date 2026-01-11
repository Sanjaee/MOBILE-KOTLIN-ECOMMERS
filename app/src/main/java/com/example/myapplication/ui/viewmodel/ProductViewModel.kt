package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.*
import com.example.myapplication.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val productDetail: Product? = null,
    val isLoadingDetail: Boolean = false,
    val total: Long = 0,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
    val isTokenExpired: Boolean = false,
    val isCreateSuccess: Boolean = false,
    val createdProductId: String? = null,
    val categories: List<Category> = emptyList(),
    val isUploadingImages: Boolean = false,
    val uploadImagesSuccess: Boolean = false,
    // Search states
    val searchResults: List<Product> = emptyList(),
    val isSearching: Boolean = false,
    val searchKeyword: String = "",
    val searchTotal: Long = 0,
    val searchPage: Int = 1,
    val searchHasMore: Boolean = false,
    // Search suggestions
    val searchSuggestions: List<String> = emptyList(),
    val isSearchingSuggestions: Boolean = false
)

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductRepository(application)
    
    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()
    
    fun loadProducts(
        page: Int = 1,
        limit: Int = 10,
        categoryId: String? = null,
        featured: Boolean? = null,
        activeOnly: Boolean = true,
        append: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isTokenExpired = false
            )
            
            repository.getProducts(
                page = page,
                limit = limit,
                categoryId = categoryId,
                featured = featured,
                activeOnly = activeOnly
            ).fold(
                onSuccess = { response ->
                    val newProducts = if (append) {
                        _uiState.value.products + response.products
                    } else {
                        response.products
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        products = newProducts,
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
                        errorMessage = exception.message ?: "Failed to load products",
                        isTokenExpired = isTokenExpired
                    )
                }
            )
        }
    }
    
    fun loadFeaturedProducts() {
        viewModelScope.launch {
            repository.getProducts(
                page = 1,
                limit = 10,
                featured = true,
                activeOnly = true
            ).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        featuredProducts = response.products,
                        errorMessage = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Failed to load featured products"
                    )
                }
            )
        }
    }
    
    fun loadMoreProducts(
        categoryId: String? = null,
        activeOnly: Boolean = true
    ) {
        if (_uiState.value.isLoading || !_uiState.value.hasMore) {
            return
        }
        
        val nextPage = _uiState.value.currentPage + 1
        loadProducts(
            page = nextPage,
            categoryId = categoryId,
            activeOnly = activeOnly,
            append = true
        )
    }
    
    fun refreshProducts(
        categoryId: String? = null,
        activeOnly: Boolean = true
    ) {
        loadProducts(
            page = 1,
            categoryId = categoryId,
            activeOnly = activeOnly,
            append = false
        )
    }
    
    fun loadProductById(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingDetail = true,
                errorMessage = null,
                isTokenExpired = false
            )
            
            repository.getProductById(id).fold(
                onSuccess = { product ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingDetail = false,
                        productDetail = product,
                        errorMessage = null
                    )
                },
                onFailure = { exception ->
                    val isTokenExpired = exception is TokenExpiredException
                    _uiState.value = _uiState.value.copy(
                        isLoadingDetail = false,
                        errorMessage = exception.message ?: "Failed to load product",
                        isTokenExpired = isTokenExpired
                    )
                }
            )
        }
    }
    
    fun createProduct(request: CreateProductRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isCreateSuccess = false
            )
            
            repository.createProduct(request).fold(
                onSuccess = { product ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isCreateSuccess = true,
                        createdProductId = product.id
                    )
                },
                onFailure = { exception ->
                    val isTokenExpired = exception is TokenExpiredException
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to create product",
                        isTokenExpired = isTokenExpired
                    )
                }
            )
        }
    }
    
    fun resetCreateSuccess() {
        _uiState.value = _uiState.value.copy(
            isCreateSuccess = false,
            createdProductId = null
        )
    }
    
    fun loadCategories(activeOnly: Boolean? = null) {
        viewModelScope.launch {
            repository.getCategories(activeOnly = activeOnly).fold(
                onSuccess = { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Failed to load categories"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun uploadProductImages(productId: String, imageUris: List<android.net.Uri>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploadingImages = true,
                errorMessage = null,
                uploadImagesSuccess = false
            )
            
            repository.uploadProductImages(productId, imageUris).fold(
                onSuccess = { _ ->
                    _uiState.value = _uiState.value.copy(
                        isUploadingImages = false,
                        uploadImagesSuccess = true
                    )
                },
                onFailure = { exception ->
                    val isTokenExpired = exception is TokenExpiredException
                    _uiState.value = _uiState.value.copy(
                        isUploadingImages = false,
                        errorMessage = exception.message ?: "Failed to upload images",
                        isTokenExpired = isTokenExpired
                    )
                }
            )
        }
    }
    
    fun resetUploadImagesSuccess() {
        _uiState.value = _uiState.value.copy(
            uploadImagesSuccess = false
        )
    }
    
    fun searchProducts(keyword: String, page: Int = 1, limit: Int = 20, append: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearching = true,
                searchKeyword = keyword,
                errorMessage = null,
                isTokenExpired = false
            )
            
            repository.searchProducts(
                keyword = keyword,
                page = page,
                limit = limit,
                activeOnly = true
            ).fold(
                onSuccess = { response ->
                    val newProducts = if (append) {
                        _uiState.value.searchResults + response.products
                    } else {
                        response.products
                    }
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        searchResults = newProducts,
                        searchTotal = response.total,
                        searchPage = response.page,
                        searchHasMore = (response.page * response.limit) < response.total,
                        errorMessage = null
                    )
                },
                onFailure = { exception ->
                    val isTokenExpired = exception is TokenExpiredException
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        errorMessage = exception.message ?: "Failed to search products",
                        isTokenExpired = isTokenExpired
                    )
                }
            )
        }
    }
    
    fun resetSearchState() {
        _uiState.value = _uiState.value.copy(
            searchResults = emptyList(),
            searchKeyword = "",
            searchTotal = 0,
            searchPage = 1,
            searchHasMore = false,
            isSearching = false
        )
    }
    
    fun searchSuggestions(keyword: String) {
        if (keyword.isBlank() || keyword.length < 2) {
            _uiState.value = _uiState.value.copy(
                searchSuggestions = emptyList(),
                isSearchingSuggestions = false
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearchingSuggestions = true
            )
            
            repository.searchProducts(
                keyword = keyword,
                page = 1,
                limit = 10, // Limit kecil untuk suggestions
                activeOnly = true
            ).fold(
                onSuccess = { response ->
                    // Extract unique product names as suggestions
                    val suggestions = response.products.map { it.name }.distinct()
                    _uiState.value = _uiState.value.copy(
                        searchSuggestions = suggestions,
                        isSearchingSuggestions = false
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        searchSuggestions = emptyList(),
                        isSearchingSuggestions = false
                    )
                }
            )
        }
    }
    
    fun clearSearchSuggestions() {
        _uiState.value = _uiState.value.copy(
            searchSuggestions = emptyList(),
            isSearchingSuggestions = false
        )
    }
}
