package com.example.hotroid.chatbot;

import com.example.hotroid.bean.ChatBotResponse;
import com.example.hotroid.bean.Hotel;
import com.example.hotroid.bean.Reserva;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.ArrayList;

public class HotelChatBot {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Hotel hotel;
    private String userId;

    public interface ChatBotCallback {
        void onResponse(ChatBotResponse response);
        void onError(String error);
    }

    public HotelChatBot(Hotel hotel) {
        this.hotel = hotel;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public ChatBotResponse getWelcomeMessage() {
        String welcomeText = "¡Hola! 👋 Bienvenido al asistente virtual de " + hotel.getName() +
                "\n\nSelecciona una opción:\n" +
                "1️⃣ Información del hotel\n" +
                "2️⃣ Ver mis reservas\n" +
                "3️⃣ Servicios disponibles\n" +
                "4️⃣ Contactar atención al cliente\n" +
                "5️⃣ Políticas de cancelación\n\n" +
                "Escribe el número de la opción que deseas 😊";

        return new ChatBotResponse(welcomeText, "options", true);
    }

    public void processUserInput(String userInput, ChatBotCallback callback) {
        String input = userInput.trim();

        switch (input) {
            case "1":
                callback.onResponse(getHotelInfo());
                break;
            case "2":
                getUserReservations(callback);
                break;
            case "3":
                callback.onResponse(getHotelServices());
                break;
            case "4":
                callback.onResponse(getCustomerSupport());
                break;
            case "5":
                callback.onResponse(getCancellationPolicy());
                break;
            default:
                callback.onResponse(getInvalidOptionResponse());
                break;
        }
    }

    private ChatBotResponse getHotelInfo() {
        String info = "🏨 *" + hotel.getName() + "*\n\n" +
                "⭐ Calificación: " + hotel.getRating() + "/5\n" +
       //         "💰 Precio desde: S/. " + String.format("%.2f", hotel.getPrice()) + " por noche\n" +
                "📍 Ubicación: " + hotel.getDireccion() + "\n\n" +
                "📝 Descripción:\n" + hotel.getDescription() + "\n\n" +
                "¿Necesitas algo más? Escribe el número de otra opción 😊";

        return new ChatBotResponse(info, "text", true);
    }

    private void getUserReservations(ChatBotCallback callback) {
        if (userId == null) {
            callback.onResponse(new ChatBotResponse(
                    "❌ Debes iniciar sesión para ver tus reservas.", "text", true));
            return;
        }

        db.collection("reservas")
                .whereEqualTo("idPersona", userId)
                .whereEqualTo("idHotel", hotel.getIdHotel())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Reserva> reservas = queryDocumentSnapshots.toObjects(Reserva.class);
                    callback.onResponse(formatReservationsResponse(reservas));
                })
                .addOnFailureListener(e -> callback.onError("Error al consultar reservas: " + e.getMessage()));
    }

    private ChatBotResponse formatReservationsResponse(List<Reserva> reservas) {
        if (reservas.isEmpty()) {
            return new ChatBotResponse(
                    "📋 No tienes reservas en " + hotel.getName() + "\n\n" +
                            "¿Te gustaría hacer una nueva reserva? 🏨\n\n" +
                            "Escribe otra opción para continuar 😊", "text", true);
        }

        StringBuilder response = new StringBuilder("📋 *Tus reservas en " + hotel.getName() + ":*\n\n");

        for (int i = 0; i < reservas.size(); i++) {
            Reserva reserva = reservas.get(i);
            response.append(String.format("🔹 *Reserva %d:*\n", i + 1));
            response.append("📅 Check-in: ").append(formatDate(reserva.getFechaInicio())).append("\n");
            response.append("📅 Check-out: ").append(formatDate(reserva.getFechaFin())).append("\n");
            response.append("🏠 Habitaciones: ").append(reserva.getHabitaciones()).append("\n");
            response.append("👥 Huéspedes: ").append(reserva.getAdultos()).append(" adultos");
            if (reserva.getNinos() > 0) {
                response.append(", ").append(reserva.getNinos()).append(" niños");
            }
            response.append("\n💰 Total: S/. ").append(String.format("%.2f", reserva.getPrecioTotal())).append("\n");
            response.append("📊 Estado: ").append(getEstadoEmoji(reserva.getEstado())).append(" ").append(reserva.getEstado().toUpperCase()).append("\n\n");
        }

        response.append("¿Necesitas algo más? Escribe el número de otra opción 😊");
        return new ChatBotResponse(response.toString(), "text", true);
    }

    private ChatBotResponse getHotelServices() {
        String services = "🏨 *Servicios disponibles en " + hotel.getName() + ":*\n\n" +
                "🏋️‍♂️ Gimnasio\n" +
                "🍳 Servicio de desayuno\n" +
                "🏊‍♀️ Piscina\n" +
                "🚗 Estacionamiento\n" +
                "🧹 Servicio de limpieza\n" +
                "📞 Servicio a la habitación 24/7\n" +
                "📶 WiFi gratuito\n" +
                "🛡️ Seguridad 24/7\n\n" +
                "¿Quieres información específica de algún servicio? Escribe otra opción 😊";

        return new ChatBotResponse(services, "text", true);
    }

    private ChatBotResponse getCustomerSupport() {
        String support = "📞 *Contactar Atención al Cliente*\n\n" +
                "Para " + hotel.getName() + ":\n\n" +
                "🔹 Teléfono: +51 123 456 789\n" +
                "🔹 Email: atencion@" + hotel.getName().toLowerCase().replace(" ", "") + ".com\n" +
                "🔹 WhatsApp: +51 987 654 321\n" +
                "🔹 Horario: 24/7\n\n" +
                "También puedes solicitar que un agente se comunique contigo.\n\n" +
                "¿Necesitas algo más? Escribe el número de otra opción 😊";

        return new ChatBotResponse(support, "text", true);
    }

    private ChatBotResponse getCancellationPolicy() {
        String policy = "📋 *Políticas de Cancelación - " + hotel.getName() + "*\n\n" +
                "✅ *Cancelación Gratuita:*\n" +
                "• Hasta 24 horas antes del check-in\n\n" +
                "⚠️ *Cancelación con Cargo:*\n" +
                "• Entre 24 horas y check-in: 50% del total\n" +
                "• No show: 100% del total\n\n" +
                "💡 *Recomendaciones:*\n" +
                "• Cancela con anticipación para evitar cargos\n" +
                "• Verifica las condiciones específicas de tu tarifa\n\n" +
                "¿Necesitas cancelar una reserva? Contacta atención al cliente (opción 4) 📞\n\n" +
                "Escribe otra opción para continuar 😊";

        return new ChatBotResponse(policy, "text", true);
    }

    private ChatBotResponse getInvalidOptionResponse() {
        String invalid = "❌ Opción no válida\n\n" +
                "Por favor selecciona una de estas opciones:\n\n" +
                "1️⃣ Información del hotel\n" +
                "2️⃣ Ver mis reservas\n" +
                "3️⃣ Servicios disponibles\n" +
                "4️⃣ Contactar atención al cliente\n" +
                "5️⃣ Políticas de cancelación\n\n" +
                "Escribe solo el número de la opción 😊";

        return new ChatBotResponse(invalid, "options", true);
    }

    private String formatDate(java.util.Date date) {
        if (date == null) return "Fecha no disponible";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(date);
    }

    private String getEstadoEmoji(String estado) {
        switch (estado.toLowerCase()) {
            case "activo": return "✅";
            case "pasado": return "✔️";
            case "cancelado": return "❌";
            default: return "📋";
        }
    }
}