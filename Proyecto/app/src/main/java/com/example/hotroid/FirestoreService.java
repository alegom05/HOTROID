package com.example.hotroid;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreService {
    private FirebaseFirestore db;
    private static FirestoreService instance;

    private FirestoreService() {
        db = FirebaseFirestore.getInstance();
    }

    public static FirestoreService getInstance() {
        if (instance == null) {
            instance = new FirestoreService();
        }
        return instance;
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    // Obtener información general del hotel
    public void getHotelInfo(String hotelId, FirestoreCallback<Map<String, Object>> callback) {
        db.collection("hotels").document(hotelId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            callback.onSuccess(document.getData());
                        } else {
                            callback.onError("Hotel no encontrado");
                        }
                    } else {
                        callback.onError("Error al obtener información del hotel");
                    }
                });
    }

    // Obtener servicios del hotel
    public void getHotelServices(String hotelId, FirestoreCallback<List<Map<String, Object>>> callback) {
        db.collection("hotels").document(hotelId)
                .collection("services")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        List<Map<String, Object>> services = new ArrayList<>();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            if (document.exists()) {
                                services.add(document.getData());
                            }
                        }
                        callback.onSuccess(services);
                    } else {
                        callback.onError("Error al obtener servicios del hotel");
                    }
                });
    }

    // Obtener reservas del usuario
    public void getUserReservations(String userId, String hotelId, FirestoreCallback<List<Map<String, Object>>> callback) {
        db.collection("users").document(userId)
                .collection("reservations")
                .whereEqualTo("hotelId", hotelId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        List<Map<String, Object>> reservations = new ArrayList<>();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            if (document.exists()) {
                                reservations.add(document.getData());
                            }
                        }
                        callback.onSuccess(reservations);
                    } else {
                        callback.onError("Error al obtener reservas del usuario");
                    }
                });
    }

    // Obtener información de contacto del hotel
    public void getHotelContact(String hotelId, FirestoreCallback<Map<String, Object>> callback) {
        db.collection("hotels").document(hotelId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> contactInfo = new HashMap<>();
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                contactInfo.put("contact", data.get("contact"));
                                contactInfo.put("phone", data.get("phone"));
                                contactInfo.put("email", data.get("email"));
                                contactInfo.put("address", data.get("address"));
                            }
                            callback.onSuccess(contactInfo);
                        } else {
                            callback.onError("Información de contacto no encontrada");
                        }
                    } else {
                        callback.onError("Error al obtener información de contacto");
                    }
                });
    }

    // Obtener políticas del hotel
    public void getHotelPolicies(String hotelId, FirestoreCallback<Map<String, Object>> callback) {
        db.collection("hotels").document(hotelId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> policies = new HashMap<>();
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                policies.put("policies", data.get("policies"));
                                policies.put("checkInTime", data.get("checkInTime"));
                                policies.put("checkOutTime", data.get("checkOutTime"));
                                policies.put("cancellationPolicy", data.get("cancellationPolicy"));
                            }
                            callback.onSuccess(policies);
                        } else {
                            callback.onError("Políticas no encontradas");
                        }
                    } else {
                        callback.onError("Error al obtener políticas del hotel");
                    }
                });
    }

    // Obtener respuestas predefinidas del chatbot
    public void getChatbotResponses(String hotelId, FirestoreCallback<Map<String, Object>> callback) {
        db.collection("chatbot_responses").document(hotelId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            callback.onSuccess(document.getData());
                        } else {
                            // Si no existen respuestas predefinidas, crear respuestas por defecto
                            Map<String, Object> defaultResponses = createDefaultResponses();
                            callback.onSuccess(defaultResponses);
                        }
                    } else {
                        callback.onError("Error al obtener respuestas del chatbot");
                    }
                });
    }

    private Map<String, Object> createDefaultResponses() {
        Map<String, Object> responses = new HashMap<>();
        Map<String, String> options = new HashMap<>();
        options.put("1", "info");
        options.put("2", "reservations");
        options.put("3", "services");
        options.put("4", "contact");
        options.put("5", "policies");
        
        Map<String, String> responseTexts = new HashMap<>();
        responseTexts.put("info", "Información general del hotel disponible.");
        responseTexts.put("contact", "Para contactarnos, puede llamar a nuestro número principal o enviar un email.");
        responseTexts.put("policies", "Nuestras políticas incluyen check-in a las 3:00 PM y check-out a las 12:00 PM.");
        
        responses.put("options", options);
        responses.put("responses", responseTexts);
        
        return responses;
    }
}