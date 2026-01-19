package com.humblecoders.aromex_android_windows

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.humblecoders.aromex_android_windows.data.firebase.FirebaseInitializer
import com.humblecoders.aromex_android_windows.data.repository.FirestoreEntityRepository
import com.humblecoders.aromex_android_windows.data.repository.FirestoreExpenseRepository
import com.humblecoders.aromex_android_windows.data.repository.FirestoreFinancialRepository
import com.humblecoders.aromex_android_windows.data.repository.FirestoreSpecificationRepository
import com.humblecoders.aromex_android_windows.presentation.ui.AndroidHomeScreen
import com.humblecoders.aromex_android_windows.presentation.viewmodel.ExpenseViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.HomeViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.ProfilesViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.PurchaseViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.SpecificationViewModel
import com.humblecoders.aromex_android_windows.ui.theme.AromexTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize Firebase using application context
        FirebaseInitializer.initialize(applicationContext)
        
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

        setContent {
            AromexTheme {
                AndroidHomeScreen(
                    viewModel = homeViewModel,
                    purchaseViewModel = purchaseViewModel,
                    profilesViewModel = profilesViewModel,
                    expenseViewModel = expenseViewModel
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Stop listening when activity is destroyed
        FirestoreEntityRepository.stopListening()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    // Preview without Firebase initialization
    // AndroidHomeScreen(viewModel = remember { HomeViewModel(...) })
}