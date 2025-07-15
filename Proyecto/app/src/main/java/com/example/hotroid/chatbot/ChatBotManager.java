package com.example.hotroid.chatbot;

import com.example.hotroid.bean.Hotel;
import com.example.hotroid.bean.ChatBotResponse;
import java.util.HashMap;
import java.util.Map;

public class ChatBotManager {

    private static ChatBotManager instance;
    private Map<String, HotelChatBot> chatBots;

    private ChatBotManager() {
        this.chatBots = new HashMap<>();
    }

    public static ChatBotManager getInstance() {
        if (instance == null) {
            instance = new ChatBotManager();
        }
        return instance;
    }

    public HotelChatBot getChatBot(Hotel hotel) {
        String hotelId = hotel.getIdHotel();

        if (!chatBots.containsKey(hotelId)) {
            chatBots.put(hotelId, new HotelChatBot(hotel));
        }

        return chatBots.get(hotelId);
    }

    public void removeChatBot(String hotelId) {
        chatBots.remove(hotelId);
    }

    public void clearAllChatBots() {
        chatBots.clear();
    }
}