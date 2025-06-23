package com.example.hotroid;

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
            MediaManager.init(context, config);
            isInitialized = true;
        }
    }
}


