package com.example.myapplication.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.data.model.Product
import com.example.myapplication.ui.screen.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    
    // Main screens with bottom navigation
    object Main : Screen("main")
    object MainHome : Screen("main/home")
    object MainFood : Screen("main/food")
    object MainPromo : Screen("main/promo")
    object MainTransaksi : Screen("main/transaksi")
    object MainAkun : Screen("main/akun")
    
    // Profile
    object Profile : Screen("profile")
    
    object VerifyOTP : Screen("verify_otp/{email}") {
        fun createRoute(email: String) = "verify_otp/$email"
    }
    
    object VerifyOTPReset : Screen("verify_otp_reset/{email}") {
        fun createRoute(email: String) = "verify_otp_reset/$email"
    }
    
    object SetNewPassword : Screen("set_new_password/{email}/{otpCode}") {
        fun createRoute(email: String, otpCode: String) = "set_new_password/${android.net.Uri.encode(email)}/${android.net.Uri.encode(otpCode)}"
    }
    
    object VerifyEmail : Screen("verify_email/{token}") {
        fun createRoute(token: String) = "verify_email/$token"
    }
    
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    
    object Checkout : Screen("checkout/{productId}/{quantity}") {
        fun createRoute(productId: String, quantity: Int = 1) = "checkout/$productId/$quantity"
    }
    
    object PaymentStatus : Screen("payment_status/{paymentId}") {
        fun createRoute(paymentId: String) = "payment_status/$paymentId"
    }
    
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
    
    object CreateStore : Screen("create_store")
    
    object StoreDetail : Screen("store_detail/{sellerId}") {
        fun createRoute(sellerId: String) = "store_detail/$sellerId"
    }
    
    object MyStoreDetail : Screen("my_store_detail")
    
    object CreateProduct : Screen("create_product")
    
    object Search : Screen("search")
    
    object SearchResult : Screen("search_result/{keyword}") {
        fun createRoute(keyword: String) = "search_result/${android.net.Uri.encode(keyword)}"
    }
}

// Slide animation helper - Gojek style
// Masuk: slide dari kanan ke kiri
// Keluar (back): slide dari kiri ke kanan
private fun slideInFromRight() = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(300)
) + fadeIn(animationSpec = tween(300))

private fun slideOutToLeft() = slideOutHorizontally(
    targetOffsetX = { -it },
    animationSpec = tween(300)
) + fadeOut(animationSpec = tween(300))

private fun slideInFromLeft() = slideInHorizontally(
    initialOffsetX = { -it },
    animationSpec = tween(300)
) + fadeIn(animationSpec = tween(300))

private fun slideOutToRight() = slideOutHorizontally(
    targetOffsetX = { it },
    animationSpec = tween(300)
) + fadeOut(animationSpec = tween(300))

@Composable
private fun MainScreenWrapper(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    onProductClick: (String) -> Unit,
    content: @Composable () -> Unit
) {
    MainScreen(
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        onLogout = onLogout,
        onProductClick = onProductClick,
        onProfileClick = onProfileClick
    ) {
        CompositionLocalProvider(
            com.example.myapplication.ui.screen.LocalOnLogout provides onLogout
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(
    startDestination: String = Screen.Login.route,
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = Screen.Login.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onNavigateToVerifyOTP = { email ->
                    navController.navigate(Screen.VerifyOTP.createRoute(email))
                }
            )
        }
        
        composable(
            route = Screen.Register.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            RegisterScreen(
                onRegisterSuccess = {
                    // Will navigate to VerifyOTP from ViewModel
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToVerifyOTP = { email ->
                    navController.navigate(Screen.VerifyOTP.createRoute(email)) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.VerifyOTP.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyOTPScreen(
                email = email,
                onVerifySuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.ForgotPassword.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            ForgotPasswordScreen(
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                },
                onNavigateToVerifyReset = { email ->
                    navController.navigate(Screen.VerifyOTPReset.createRoute(email))
                }
            )
        }
        
        composable(
            route = Screen.VerifyOTPReset.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyOTPResetScreen(
                email = email,
                onOtpVerified = { verifiedEmail, otpCode ->
                    navController.navigate(Screen.SetNewPassword.createRoute(verifiedEmail, otpCode))
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.SetNewPassword.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("otpCode") { type = NavType.StringType }
            ),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val email = android.net.Uri.decode(backStackEntry.arguments?.getString("email") ?: "")
            val otpCode = android.net.Uri.decode(backStackEntry.arguments?.getString("otpCode") ?: "")
            SetNewPasswordScreen(
                email = email,
                otpCode = otpCode,
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.VerifyEmail.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            VerifyEmailScreen(
                token = token,
                onVerifySuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Main screen with bottom navigation (default to Home)
        composable(
            route = Screen.Main.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            navController.navigate(Screen.MainHome.route) {
                popUpTo(Screen.Main.route) { inclusive = true }
            }
        }
        
        // Main Home (with bottom nav)
        composable(
            route = Screen.MainHome.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            MainScreenWrapper(
                selectedTab = Screen.MainHome.route,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Main.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                }
            ) {
                HomeScreenContent(
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }
        }
        
        // Main Food
        composable(
            route = Screen.MainFood.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            MainScreenWrapper(
                selectedTab = Screen.MainFood.route,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Main.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onProductClick = { }
            ) {
                FoodScreen()
            }
        }
        
        // Main Promo
        composable(
            route = Screen.MainPromo.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            MainScreenWrapper(
                selectedTab = Screen.MainPromo.route,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Main.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onProductClick = { }
            ) {
                PromoScreen()
            }
        }
        
        // Main Transaksi
        composable(
            route = Screen.MainTransaksi.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            MainScreenWrapper(
                selectedTab = Screen.MainTransaksi.route,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Main.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onProductClick = { }
            ) {
                TransaksiScreen(
                    onOrderClick = { orderId ->
                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                    },
                    onPaymentClick = { paymentId ->
                        navController.navigate(Screen.PaymentStatus.createRoute(paymentId))
                    }
                )
            }
        }
        
        // Main Akun
        composable(
            route = Screen.MainAkun.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            MainScreenWrapper(
                selectedTab = Screen.MainAkun.route,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Main.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onProductClick = { }
            ) {
                AkunScreen(
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onLogout = {
                        onLogout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
        
        // Profile Screen
        composable(
            route = Screen.Profile.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onCreateStoreClick = {
                    navController.navigate(Screen.CreateStore.route)
                },
                onNavigateToStore = {
                    navController.navigate(Screen.MyStoreDetail.route)
                }
            )
        }
        
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId = productId,
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBuyClick = { product ->
                    navController.navigate(Screen.Checkout.createRoute(product.id, 1))
                }
            )
        }
        
        composable(
            route = Screen.Checkout.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType },
                navArgument("quantity") { type = NavType.IntType; defaultValue = 1 }
            ),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val quantity = backStackEntry.arguments?.getInt("quantity") ?: 1
            
            CheckoutPage1Screen(
                productId = productId,
                quantity = quantity,
                onBack = {
                    navController.popBackStack()
                },
                onPayClick = { paymentId ->
                    // Navigate to PaymentStatusScreen after payment is created
                    navController.navigate(Screen.PaymentStatus.createRoute(paymentId)) {
                        // Remove checkout from backstack so user can't go back
                        popUpTo(Screen.Checkout.route) { inclusive = true }
                    }
                },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.PaymentStatus.route,
            arguments = listOf(navArgument("paymentId") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val paymentId = backStackEntry.arguments?.getString("paymentId") ?: ""
            
            PaymentStatusScreen(
                paymentId = paymentId,
                orderId = null,
                onBack = {
                    navController.popBackStack()
                },
                onHome = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOrderDetail = { orderId ->
                    // Navigate to OrderDetailScreen when payment is successful
                    navController.navigate(Screen.OrderDetail.createRoute(orderId)) {
                        // Remove PaymentStatus from backstack so user goes back to home
                        popUpTo(Screen.PaymentStatus.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            
            OrderDetailScreen(
                orderId = orderId,
                onBack = {
                    navController.popBackStack()
                },
                onHome = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.CreateStore.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            CreateStoreScreen(
                onBack = {
                    navController.popBackStack()
                },
                onStoreCreated = { sellerId ->
                    navController.navigate(Screen.StoreDetail.createRoute(sellerId)) {
                        popUpTo(Screen.CreateStore.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.StoreDetail.route,
            arguments = listOf(navArgument("sellerId") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
            
            StoreDetailScreen(
                sellerId = sellerId,
                onBack = {
                    navController.popBackStack()
                },
                onAddProductClick = {
                    navController.navigate(Screen.CreateProduct.route)
                }
            )
        }
        
        composable(
            route = Screen.MyStoreDetail.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            StoreDetailScreen(
                sellerId = null, // Load current user's store
                onBack = {
                    navController.popBackStack()
                },
                onAddProductClick = {
                    navController.navigate(Screen.CreateProduct.route)
                }
            )
        }
        
        composable(
            route = Screen.CreateProduct.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            CreateProductScreen(
                onBack = {
                    navController.popBackStack()
                },
                onProductCreated = { _ ->
                    // Navigate to home after product created successfully
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Search.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            SearchScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSearchClick = { keyword ->
                    navController.navigate(Screen.SearchResult.createRoute(keyword))
                }
            )
        }
        
        composable(
            route = Screen.SearchResult.route,
            arguments = listOf(navArgument("keyword") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val keyword = backStackEntry.arguments?.getString("keyword")?.let {
                android.net.Uri.decode(it)
            } ?: ""
            
            SearchResultScreen(
                keyword = keyword,
                onBack = {
                    navController.popBackStack()
                },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                }
            )
        }
    }
}

