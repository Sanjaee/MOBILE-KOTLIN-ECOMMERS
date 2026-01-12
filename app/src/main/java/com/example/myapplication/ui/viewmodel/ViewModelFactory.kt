package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(application) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(application) as T
            }
            modelClass.isAssignableFrom(VerifyOTPViewModel::class.java) -> {
                VerifyOTPViewModel(application) as T
            }
            modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java) -> {
                ForgotPasswordViewModel(application) as T
            }
            modelClass.isAssignableFrom(VerifyEmailViewModel::class.java) -> {
                VerifyEmailViewModel(application) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(application) as T
            }
            modelClass.isAssignableFrom(ProductViewModel::class.java) -> {
                ProductViewModel(application) as T
            }
            modelClass.isAssignableFrom(CheckoutViewModel::class.java) -> {
                CheckoutViewModel(application) as T
            }
            modelClass.isAssignableFrom(PaymentViewModel::class.java) -> {
                PaymentViewModel(application) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(application) as T
            }
            modelClass.isAssignableFrom(OrderViewModel::class.java) -> {
                OrderViewModel(application) as T
            }
            modelClass.isAssignableFrom(SellerViewModel::class.java) -> {
                SellerViewModel(application) as T
            }
            modelClass.isAssignableFrom(CartViewModel::class.java) -> {
                CartViewModel(application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

