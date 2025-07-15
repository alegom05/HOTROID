// src/main/java/com/example/hotroid/CloudinaryManager.java
package com.example.hotroid; // Asegúrate que el paquete sea correcto

import android.content.Context;
import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {
    private static boolean isInitialized = false;

    public static void init(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dssoxggz3");
            config.put("api_key", "996445227885663");
            config.put("api_secret", "2WqO6YaSuZyO0WXc8cxj4JkArxU"); // ⚠️ Solo para pruebas
            try {
                MediaManager.init(context, config);
                isInitialized = true;
                android.util.Log.d("CloudinaryManager", "Cloudinary inicializado con éxito.");
            } catch (IllegalStateException e) {
                android.util.Log.e("CloudinaryManager", "Fallo la inicialización de Cloudinary: " + e.getMessage());
                // Considera manejar este error, quizás mostrando un Toast si es crítico para el inicio.
            }
        }
    }
}