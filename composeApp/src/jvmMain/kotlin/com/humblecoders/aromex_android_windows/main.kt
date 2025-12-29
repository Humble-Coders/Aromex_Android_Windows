package com.humblecoders.aromex_android_windows

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.humblecoders.aromex_android_windows.data.firebase.FirebaseInitializer
import com.humblecoders.aromex_android_windows.data.repository.FirestoreFinancialRepository
import com.humblecoders.aromex_android_windows.presentation.ui.WindowsHomeScreen
import com.humblecoders.aromex_android_windows.presentation.viewmodel.HomeViewModel

fun main() = application {
    // Initialize Firebase
    val credentialsPath = "firebase-credentials.json"
    FirebaseInitializer.initialize(credentialsPath)
    
    // Create repository and view model
    val financialRepository = FirestoreFinancialRepository(FirebaseInitializer.getFirestore())
    val homeViewModel = HomeViewModel(financialRepository)
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "AROMEX",
    ) {
        WindowsHomeScreen(viewModel = homeViewModel)
    }
}