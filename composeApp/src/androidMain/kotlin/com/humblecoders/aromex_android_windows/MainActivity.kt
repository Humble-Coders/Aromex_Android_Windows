package com.humblecoders.aromex_android_windows

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.humblecoders.aromex_android_windows.data.firebase.FirebaseInitializer
import com.humblecoders.aromex_android_windows.data.repository.FirestoreFinancialRepository
import com.humblecoders.aromex_android_windows.presentation.ui.AndroidHomeScreen
import com.humblecoders.aromex_android_windows.presentation.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize Firebase using application context
        FirebaseInitializer.initialize(applicationContext)
        
        // Create repository and view model
        val financialRepository = FirestoreFinancialRepository(FirebaseInitializer.getFirestore())
        val homeViewModel = HomeViewModel(financialRepository)

        setContent {
            AndroidHomeScreen(viewModel = homeViewModel)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    // Preview without Firebase initialization
    // AndroidHomeScreen(viewModel = remember { HomeViewModel(...) })
}