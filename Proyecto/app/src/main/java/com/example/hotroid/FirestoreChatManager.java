package com.example.hotroid;

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

    private FirestoreChatManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static FirestoreChatManager getInstance() {
        if (instance == null) {
            instance = new FirestoreChatManager();
        }
        return instance;
    }

    // Crear o obtener chat existente
    public void createOrGetChat(String clientId, String hotelId, String hotelName, ChatCreationListener listener) {
        String chatId = generateChatId(clientId, hotelId);

        db.collection(CHATS_COLLECTION)
                .document(chatId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Chat ya existe, lo devolvemos
                        ChatSession existingChat = documentSnapshot.toObject(ChatSession.class);
                        if (existingChat != null) {
                            listener.onChatCreated(existingChat);
                        }
                    } else {
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

                        db.collection(CHATS_COLLECTION)
                                .document(chatId)
                                .set(newChat)
                                .addOnSuccessListener(aVoid -> listener.onChatCreated(newChat))
                                .addOnFailureListener(e -> listener.onError("Error al crear chat: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error al verificar chat: " + e.getMessage()));
    }

    // Obtener chats del cliente
    public void getUserChats(String clientId, ChatListListener listener) {
        db.collection(CHATS_COLLECTION)
                .whereEqualTo("clientId", clientId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            listener.onError("Error al obtener chats: " + e.getMessage());
                            return;
                        }

                        List<ChatSession> chats = new ArrayList<>();
                        if (queryDocumentSnapshots != null) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                ChatSession chat = document.toObject(ChatSession.class);
                                if (chat != null) {
                                    chats.add(chat);
                                }
                            }
                        }
                        listener.onChatsLoaded(chats);
                    }
                });
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

                            db.collection(CHATS_COLLECTION)
                                    .document(chatId)
                                    .set(chat)
                                    .addOnFailureListener(e -> {
                                        // Log error
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