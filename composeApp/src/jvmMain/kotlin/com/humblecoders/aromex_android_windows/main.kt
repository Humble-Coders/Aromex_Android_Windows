package com.humblecoders.aromex_android_windows

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.humblecoders.aromex_android_windows.data.firebase.FirebaseInitializer
import com.humblecoders.aromex_android_windows.data.repository.FirestoreFinancialRepository
import com.humblecoders.aromex_android_windows.data.repository.FirestorePurchaseRepository
import com.humblecoders.aromex_android_windows.presentation.ui.WindowsHomeScreen
import com.humblecoders.aromex_android_windows.presentation.viewmodel.HomeViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.PurchaseViewModel
import com.humblecoders.aromex_android_windows.ui.theme.AromexTheme

fun main() = application {
    // Initialize Firebase
    val credentialsPath = "firebase-credentials.json"
    FirebaseInitializer.initialize(credentialsPath)
    
    // Create repositories and view models
    val financialRepository = FirestoreFinancialRepository(FirebaseInitializer.getFirestore())
    val homeViewModel = HomeViewModel(financialRepository)
    
    val purchaseRepository = FirestorePurchaseRepository(FirebaseInitializer.getFirestore())
    val purchaseViewModel = PurchaseViewModel(purchaseRepository)
    
    var isDarkTheme by mutableStateOf(false)
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "AROMEX",
    ) {
        AromexTheme(darkTheme = isDarkTheme) {
            WindowsHomeScreen(
                viewModel = homeViewModel,
                purchaseViewModel = purchaseViewModel,
                isDarkTheme = isDarkTheme,
                onThemeToggle = { isDarkTheme = !isDarkTheme }
            )
        }
    }
}