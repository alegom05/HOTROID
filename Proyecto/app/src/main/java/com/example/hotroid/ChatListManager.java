package com.example.hotroid;

import com.example.hotroid.bean.ChatHotelItem;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatListManager {

    private static ChatListManager instance;
    private List<ChatHotelItem> activeChats;
    private List<ChatListUpdateListener> listeners;

    public interface ChatListUpdateListener {
        void onChatListUpdated(List<ChatHotelItem> chatList);
        void onNewChatAdded(ChatHotelItem chatItem);
    }

    private ChatListManager() {
        this.activeChats = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    public static ChatListManager getInstance() {
        if (instance == null) {
            instance = new ChatListManager();
        }
        return instance;
    }

    public void addListener(ChatListUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(ChatListUpdateListener listener) {
        listeners.remove(listener);
    }

    public void addOrUpdateChat(ChatHotelItem chatItem) {
        // Buscar si ya existe
        for (int i = 0; i < activeChats.size(); i++) {
            if (activeChats.get(i).getHotelId().equals(chatItem.getHotelId())) {
                // Actualizar existente
                activeChats.set(i, chatItem);
                // Mover al principio
                activeChats.add(0, activeChats.remove(i));
                notifyListeners();
                return;
            }
        }

        // Agregar nuevo al principio
        activeChats.add(0, chatItem);
        notifyNewChatAdded(chatItem);
        notifyListeners();
    }

    public void updateLastMessage(String hotelId, String lastMessage) {
        for (ChatHotelItem chat : activeChats) {
            if (chat.getHotelId().equals(hotelId)) {
                chat.setLastMessage(lastMessage);
                chat.setLastMessageTime(new Date());
                // Mover al principio
                activeChats.remove(chat);
                activeChats.add(0, chat);
                notifyListeners();
                break;
            }
        }
    }

    public List<ChatHotelItem> getActiveChats() {
        return new ArrayList<>(activeChats);
    }

    public void clearChats() {
        activeChats.clear();
        notifyListeners();
    }

    private void notifyListeners() {
        for (ChatListUpdateListener listener : listeners) {
            listener.onChatListUpdated(getActiveChats());
        }
    }

    private void notifyNewChatAdded(ChatHotelItem chatItem) {
        for (ChatListUpdateListener listener : listeners) {
            listener.onNewChatAdded(chatItem);
        }
    }
}