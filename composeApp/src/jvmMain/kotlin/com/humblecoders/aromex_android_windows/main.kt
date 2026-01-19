package com.humblecoders.aromex_android_windows

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.humblecoders.aromex_android_windows.data.firebase.FirebaseInitializer
import com.humblecoders.aromex_android_windows.data.repository.FirestoreEntityRepository
import com.humblecoders.aromex_android_windows.data.repository.FirestoreExpenseRepository
import com.humblecoders.aromex_android_windows.data.repository.FirestoreFinancialRepository
import com.humblecoders.aromex_android_windows.data.repository.FirestoreSpecificationRepository
import com.humblecoders.aromex_android_windows.presentation.ui.WindowsHomeScreen
import com.humblecoders.aromex_android_windows.presentation.viewmodel.ExpenseViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.HomeViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.ProfilesViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.PurchaseViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.SpecificationViewModel
import com.humblecoders.aromex_android_windows.ui.theme.AromexTheme

fun main() = application {
    // Initialize Firebase
    val credentialsPath = "firebase-credentials.json"
    FirebaseInitializer.initialize(credentialsPath)
    
    val firestore = FirebaseInitializer.getFirestore()
    
    // Initialize singleton EntityRepository
    // Note: Listening will start when Purchase or Profiles screen is opened (whichever opens first)
    FirestoreEntityRepository.initialize(firestore)
    
    // Initialize singleton ExpenseRepository
    FirestoreExpenseRepository.initialize(firestore)
    
    // Initialize singleton SpecificationRepository
    FirestoreSpecificationRepository.initialize(firestore)
    
    // Create repositories and view models
    val financialRepository = FirestoreFinancialRepository(firestore)
    val homeViewModel = HomeViewModel(financialRepository)
    
    // SpecificationViewModel uses the singleton SpecificationRepository
    val specificationViewModel = SpecificationViewModel(FirestoreSpecificationRepository)
    
    // Both ViewModels use the same singleton EntityRepository
    val purchaseViewModel = PurchaseViewModel(FirestoreEntityRepository, specificationViewModel)
    val profilesViewModel = ProfilesViewModel(FirestoreEntityRepository)
    
    // ExpenseViewModel uses the singleton ExpenseRepository
    val expenseViewModel = ExpenseViewModel(FirestoreExpenseRepository)
    
    var isDarkTheme by mutableStateOf(false)
    
    Window(
        onCloseRequest = {
            // Stop listening when app closes
            FirestoreEntityRepository.stopListening()
            exitApplication()
        },
        title = "AROMEX",
    ) {
        AromexTheme(darkTheme = isDarkTheme) {
            WindowsHomeScreen(
                viewModel = homeViewModel,
                purchaseViewModel = purchaseViewModel,
                profilesViewModel = profilesViewModel,
                expenseViewModel = expenseViewModel,
                isDarkTheme = isDarkTheme,
                onThemeToggle = { isDarkTheme = !isDarkTheme }
            )
        }
    }
}