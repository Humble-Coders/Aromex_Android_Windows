package com.humblecoders.aromex_android_windows.data.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import java.io.FileInputStream
import java.nio.file.Path

object FirebaseInitializer {
    private var initialized = false
    private lateinit var firebaseApp: FirebaseApp
    private lateinit var firestore: Firestore
    private lateinit var storage: Storage
    private lateinit var bucketName: String
    
    fun initialize(credentialsPath: String) {
        if (initialized) return
        
        try {
            val credentialsFile = Path.of(credentialsPath).toFile()
            if (!credentialsFile.exists()) {
                throw IllegalArgumentException("Firebase credentials file not found at: $credentialsPath")
            }
            
            val credentials = GoogleCredentials.fromStream(FileInputStream(credentialsFile))
            val projectId = extractProjectId(credentialsFile.readText())
            
            bucketName = "jewellery-app-f6302.firebasestorage.app"
            
            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()
            
            firebaseApp = FirebaseApp.initializeApp(options)
            firestore = FirestoreClient.getFirestore(firebaseApp)
            
            storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()
                .service
            
            initialized = true
            println("Firebase initialized successfully with project ID: $projectId")
        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize Firebase: ${e.message}", e)
        }
    }
    
    fun getFirestore(): Firestore {
        checkInitialized()
        return firestore
    }
    
    fun getStorage(): Storage {
        checkInitialized()
        return storage
    }
    
    fun getBucketName(): String {
        checkInitialized()
        return bucketName
    }
    
    private fun extractProjectId(jsonContent: String): String {
        val projectIdPattern = "\"project_id\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val matchResult = projectIdPattern.find(jsonContent)
        return matchResult?.groupValues?.get(1)
            ?: throw IllegalArgumentException("Could not extract project_id from credentials file")
    }
    
    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("FirebaseInitializer not initialized. Call initialize() first.")
        }
    }
}

