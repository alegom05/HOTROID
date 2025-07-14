package com.example.hotroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatbotManager {
    private static ChatbotManager instance;
    private FirestoreService firestoreService;
    private List<ChatbotOption> options;

    private ChatbotManager() {
        firestoreService = FirestoreService.getInstance();
        initializeOptions();
    }

    public static ChatbotManager getInstance() {
        if (instance == null) {
            instance = new ChatbotManager();
        }
        return instance;
    }

    public interface ChatbotCallback {
        void onResponse(String response);
        void onError(String error);
        void onShowOptions(List<ChatbotOption> options);
    }

    private void initializeOptions() {
        options = new ArrayList<>();
        options.add(new ChatbotOption("1", "Información general del hotel", "Obtener información sobre el hotel", 1));
        options.add(new ChatbotOption("2", "Ver mis reservas", "Consultar tus reservas actuales", 2));
        options.add(new ChatbotOption("3", "Servicios disponibles", "Ver servicios y amenidades del hotel", 3));
        options.add(new ChatbotOption("4", "Atención al cliente/Contacto", "Información de contacto del hotel", 4));
        options.add(new ChatbotOption("5", "Políticas del hotel", "Consultar políticas y condiciones", 5));
        options.add(new ChatbotOption("0", "Volver al menú principal", "Regresar al menú de opciones", 0));
    }

    public void showMainMenu(ChatbotCallback callback) {
        callback.onShowOptions(options);
    }

    public void processUserInput(String input, String hotelId, String userId, ChatbotCallback callback) {
        try {
            int optionNumber = Integer.parseInt(input.trim());
            handleOption(optionNumber, hotelId, userId, callback);
        } catch (NumberFormatException e) {
            callback.onError("Por favor, ingrese un número válido (0-5).");
        }
    }

    private void handleOption(int optionNumber, String hotelId, String userId, ChatbotCallback callback) {
        switch (optionNumber) {
            case 1:
                getHotelInfo(hotelId, callback);
                break;
            case 2:
                getUserReservations(hotelId, userId, callback);
                break;
            case 3:
                getHotelServices(hotelId, callback);
                break;
            case 4:
                getContactInfo(hotelId, callback);
                break;
            case 5:
                getHotelPolicies(hotelId, callback);
                break;
            case 0:
                showMainMenu(callback);
                break;
            default:
                callback.onError("Opción no válida. Por favor, seleccione una opción del 0 al 5.");
                break;
        }
    }

    private void getHotelInfo(String hotelId, ChatbotCallback callback) {
        firestoreService.getHotelInfo(hotelId, new FirestoreService.FirestoreCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                StringBuilder response = new StringBuilder("📍 **Información del Hotel**\n\n");
                
                if (result.containsKey("name")) {
                    response.append("🏨 **Nombre:** ").append(result.get("name")).append("\n");
                }
                if (result.containsKey("description")) {
                    response.append("📝 **Descripción:** ").append(result.get("description")).append("\n");
                }
                if (result.containsKey("amenities")) {
                    response.append("🎯 **Amenidades:** ").append(result.get("amenities")).append("\n");
                }
                if (result.containsKey("address")) {
                    response.append("📍 **Dirección:** ").append(result.get("address")).append("\n");
                }
                
                response.append("\n¿Hay algo más en lo que pueda ayudarle? Digite 0 para ver el menú principal.");
                callback.onResponse(response.toString());
            }

            @Override
            public void onError(String error) {
                callback.onError("Lo siento, no pude obtener la información del hotel en este momento. Por favor, intente más tarde.");
            }
        });
    }

    private void getUserReservations(String hotelId, String userId, ChatbotCallback callback) {
        firestoreService.getUserReservations(userId, hotelId, new FirestoreService.FirestoreCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> result) {
                StringBuilder response = new StringBuilder("📋 **Sus Reservas**\n\n");
                
                if (result.isEmpty()) {
                    response.append("No tiene reservas activas en este hotel.\n");
                } else {
                    for (Map<String, Object> reservation : result) {
                        response.append("🔸 **Reserva ID:** ").append(reservation.get("id")).append("\n");
                        response.append("📅 **Check-in:** ").append(reservation.get("checkIn")).append("\n");
                        response.append("📅 **Check-out:** ").append(reservation.get("checkOut")).append("\n");
                        response.append("📊 **Estado:** ").append(reservation.get("status")).append("\n");
                        if (reservation.containsKey("details")) {
                            response.append("📋 **Detalles:** ").append(reservation.get("details")).append("\n");
                        }
                        response.append("\n");
                    }
                }
                
                response.append("¿Necesita ayuda con alguna reserva? Digite 0 para ver el menú principal.");
                callback.onResponse(response.toString());
            }

            @Override
            public void onError(String error) {
                callback.onError("No pude acceder a sus reservas en este momento. Por favor, intente más tarde.");
            }
        });
    }

    private void getHotelServices(String hotelId, ChatbotCallback callback) {
        firestoreService.getHotelServices(hotelId, new FirestoreService.FirestoreCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> result) {
                StringBuilder response = new StringBuilder("🛎️ **Servicios Disponibles**\n\n");
                
                if (result.isEmpty()) {
                    response.append("Actualmente no hay servicios disponibles.\n");
                } else {
                    for (Map<String, Object> service : result) {
                        response.append("🔸 **").append(service.get("name")).append("**\n");
                        if (service.containsKey("description")) {
                            response.append("   📝 ").append(service.get("description")).append("\n");
                        }
                        if (service.containsKey("price")) {
                            response.append("   💰 Precio: $").append(service.get("price")).append("\n");
                        }
                        response.append("\n");
                    }
                }
                
                response.append("¿Le interesa algún servicio en particular? Digite 0 para ver el menú principal.");
                callback.onResponse(response.toString());
            }

            @Override
            public void onError(String error) {
                callback.onError("No pude obtener la información de servicios en este momento. Por favor, intente más tarde.");
            }
        });
    }

    private void getContactInfo(String hotelId, ChatbotCallback callback) {
        firestoreService.getHotelContact(hotelId, new FirestoreService.FirestoreCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                StringBuilder response = new StringBuilder("📞 **Información de Contacto**\n\n");
                
                if (result.containsKey("phone")) {
                    response.append("📱 **Teléfono:** ").append(result.get("phone")).append("\n");
                }
                if (result.containsKey("email")) {
                    response.append("✉️ **Email:** ").append(result.get("email")).append("\n");
                }
                if (result.containsKey("address")) {
                    response.append("📍 **Dirección:** ").append(result.get("address")).append("\n");
                }
                
                response.append("\n🕐 **Horarios de Atención:**\n");
                response.append("Lunes a Domingo: 24 horas\n");
                response.append("\nEstamos aquí para ayudarle. Digite 0 para ver el menú principal.");
                
                callback.onResponse(response.toString());
            }

            @Override
            public void onError(String error) {
                callback.onError("No pude obtener la información de contacto en este momento. Por favor, intente más tarde.");
            }
        });
    }

    private void getHotelPolicies(String hotelId, ChatbotCallback callback) {
        firestoreService.getHotelPolicies(hotelId, new FirestoreService.FirestoreCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                StringBuilder response = new StringBuilder("📋 **Políticas del Hotel**\n\n");
                
                if (result.containsKey("checkInTime")) {
                    response.append("🕐 **Check-in:** ").append(result.get("checkInTime")).append("\n");
                }
                if (result.containsKey("checkOutTime")) {
                    response.append("🕐 **Check-out:** ").append(result.get("checkOutTime")).append("\n");
                }
                if (result.containsKey("cancellationPolicy")) {
                    response.append("❌ **Política de Cancelación:** ").append(result.get("cancellationPolicy")).append("\n");
                }
                if (result.containsKey("policies")) {
                    response.append("📝 **Políticas Generales:**\n").append(result.get("policies")).append("\n");
                }
                
                response.append("\n¿Tiene alguna pregunta sobre nuestras políticas? Digite 0 para ver el menú principal.");
                callback.onResponse(response.toString());
            }

            @Override
            public void onError(String error) {
                callback.onError("No pude obtener las políticas del hotel en este momento. Por favor, intente más tarde.");
            }
        });
    }

    public String getWelcomeMessage() {
        return "🤖 **¡Bienvenido al Asistente Virtual!**\n\n" +
               "Estoy aquí para ayudarle con sus consultas sobre el hotel.\n" +
               "Por favor, seleccione una opción digitando el número correspondiente:\n\n" +
               "1️⃣ Información general del hotel\n" +
               "2️⃣ Ver mis reservas\n" +
               "3️⃣ Servicios disponibles\n" +
               "4️⃣ Atención al cliente/Contacto\n" +
               "5️⃣ Políticas del hotel\n" +
               "0️⃣ Volver al menú principal\n\n" +
               "💡 Simplemente digite el número de la opción que desea.";
    }
}