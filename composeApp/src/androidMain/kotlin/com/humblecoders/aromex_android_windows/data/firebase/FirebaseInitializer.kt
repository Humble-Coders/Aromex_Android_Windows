package com.humblecoders.aromex_android_windows.data.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage

object FirebaseInitializer {
    private var initialized = false
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    
    fun initialize(context: Context) {
        if (initialized) return
        
        try {
            // Get Firestore instance (google-services plugin auto-initializes Firebase)
            firestore = FirebaseFirestore.getInstance()
            
            // Configure Firestore settings for online mode only
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false) // Disable offline persistence - online mode only
                .build()
            firestore.firestoreSettings = settings
            
            storage = FirebaseStorage.getInstance()
            
            initialized = true
        } catch (e: Exception) {
            // If auto-initialization failed, try manual initialization
            try {
                FirebaseApp.initializeApp(context)
                firestore = FirebaseFirestore.getInstance()
                
                // Configure Firestore settings for online mode only
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false) // Disable offline persistence - online mode only
                    .build()
                firestore.firestoreSettings = settings
                
                storage = FirebaseStorage.getInstance()
                initialized = true
            } catch (initException: Exception) {
                throw IllegalStateException("Failed to initialize Firebase: ${initException.message}", initException)
            }
        }
    }
    
    fun getFirestore(): FirebaseFirestore {
        checkInitialized()
        return firestore
    }
    
    fun getStorage(): FirebaseStorage {
        checkInitialized()
        return storage
    }
    
    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("FirebaseInitializer not initialized. Call initialize() first.")
        }
    }
}

