package com.example.hibyassistant;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class HibyAssistant extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Enable offline persistence
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
    }
} 