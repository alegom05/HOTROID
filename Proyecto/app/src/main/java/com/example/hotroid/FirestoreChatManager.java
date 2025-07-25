package com.example.hotroid;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

import com.example.hotroid.bean.ChatSession;
import com.example.hotroid.bean.ChatMessage;

public class FirestoreChatManager {
    private static FirestoreChatManager instance;
    private FirebaseFirestore db;
    private static final String CHATS_COLLECTION = "chats";

    // Agregar esta línea al inicio de los métodos importantes:

    public void createOrGetChat(String clientId, String hotelId, String hotelName, ChatCreationListener listener) {
        Log.d("FirestoreChatManager", "createOrGetChat called: clientId=" + clientId + ", hotelId=" + hotelId + ", hotelName=" + hotelName);

        String chatId = generateChatId(clientId, hotelId);

        db.collection(CHATS_COLLECTION)
                .document(chatId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("FirestoreChatManager", "Chat existente encontrado");
                        ChatSession existingChat = documentSnapshot.toObject(ChatSession.class);
                        if (existingChat != null) {
                            listener.onChatCreated(existingChat);
                        }
                    } else {
                        Log.d("FirestoreChatManager", "Creando nuevo chat");
                        // Crear nuevo chat
                        ChatSession newChat = new ChatSession(chatId, clientId, hotelId, hotelName);

                        // Mensaje de bienvenida
                        ChatMessage welcomeMessage = new ChatMessage(
                                generateMessageId(),
                                "¡Hola! Soy el asistente virtual de " + hotelName + ". ¿En qué puedo ayudarte?",
                                true,
                                System.currentTimeMillis(),
                                "text"
                        );
                        newChat.addMessage(welcomeMessage);

                        newChat.setLastMessageTime(System.currentTimeMillis());

                        db.collection(CHATS_COLLECTION)
                                .document(chatId)
                                .set(newChat)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FirestoreChatManager", "Chat creado exitosamente");
                                    listener.onChatCreated(newChat);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirestoreChatManager", "Error al crear chat", e);
                                    listener.onError("Error al crear chat: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreChatManager", "Error al verificar chat", e);
                    listener.onError("Error al verificar chat: " + e.getMessage());
                });
    }

    public void getUserChats(String clientId, ChatListListener listener) {
        Log.d("FirestoreChatManager", "getUserChats called for clientId: " + clientId);

        db.collection(CHATS_COLLECTION)
                .whereEqualTo("clientId", clientId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("FirestoreChatManager", "Error al obtener chats", e);
                            listener.onError("Error al obtener chats: " + e.getMessage());
                            return;
                        }

                        List<ChatSession> chats = new ArrayList<>();
                        if (queryDocumentSnapshots != null) {
                            Log.d("FirestoreChatManager", "Documentos encontrados: " + queryDocumentSnapshots.size());
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                ChatSession chat = document.toObject(ChatSession.class);
                                if (chat != null) {
                                    Log.d("FirestoreChatManager", "Chat encontrado: " + chat.getHotelName());
                                    chats.add(chat);
                                }
                            }
                        }
                        Log.d("FirestoreChatManager", "Total chats procesados: " + chats.size());
                        listener.onChatsLoaded(chats);
                    }
                });
    }

    // NUEVO MÉTODO: Eliminar chat
    public void deleteChat(String chatId, ChatDeletionListener listener) {
        Log.d("FirestoreChatManager", "deleteChat called for chatId: " + chatId);

        db.collection(CHATS_COLLECTION)
                .document(chatId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreChatManager", "Chat eliminado exitosamente: " + chatId);
                    listener.onChatDeleted();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreChatManager", "Error al eliminar chat: " + chatId, e);
                    listener.onError("Error al eliminar chat: " + e.getMessage());
                });
    }

    public interface ChatListListener {
        void onChatsLoaded(List<ChatSession> chats);
        void onChatAdded(ChatSession chat);
        void onChatUpdated(ChatSession chat);
        void onError(String error);
    }

    public interface ChatCreationListener {
        void onChatCreated(ChatSession chat);
        void onError(String error);
    }

    // NUEVA INTERFAZ: Listener para eliminación de chats
    public interface ChatDeletionListener {
        void onChatDeleted();
        void onError(String error);
    }

    private FirestoreChatManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static FirestoreChatManager getInstance() {
        if (instance == null) {
            instance = new FirestoreChatManager();
        }
        return instance;
    }

    // Agregar mensaje al chat
    public void addMessageToChat(String chatId, ChatMessage message) {
        db.collection(CHATS_COLLECTION)
                .document(chatId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ChatSession chat = documentSnapshot.toObject(ChatSession.class);
                        if (chat != null) {
                            chat.addMessage(message);
                            chat.setLastMessageTime(System.currentTimeMillis()); // Agregar esta línea

                            db.collection(CHATS_COLLECTION)
                                    .document(chatId)
                                    .set(chat)
                                    .addOnFailureListener(e -> {
                                        Log.e("FirestoreChatManager", "Error updating chat", e);
                                    });
                        }
                    }
                });
    }

    // Marcar mensajes como leídos
    public void markAsRead(String chatId) {
        db.collection(CHATS_COLLECTION)
                .document(chatId)
                .update("unreadCount", 0);
    }

    // Utilidades
    private String generateChatId(String clientId, String hotelId) {
        return clientId + "_" + hotelId;
    }

    private String generateMessageId() {
        return String.valueOf(System.currentTimeMillis());
    }
}