package com.example.hotroid;

public class Message {

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE
    }

    private String id;
    private String content;
    private String timestamp;
    private boolean isFromUser;
    private MessageType messageType;
    private String imageUrl;
    private String fileName;

    // Nuevos campos para el chatbot
    private String senderName;
    private boolean isTyping;
    private boolean fromCurrentUser; // Para compatibilidad con el código anterior

    public Message() {
        // Constructor vacío
        this.messageType = MessageType.TEXT; // Valor por defecto
        this.isTyping = false;
    }

    public Message(String id, String content, String timestamp, boolean isFromUser, MessageType messageType) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.isFromUser = isFromUser;
        this.fromCurrentUser = isFromUser; // Sincronizar ambos campos
        this.messageType = messageType;
        this.isTyping = false;
    }

    public Message(String id, String content, String timestamp, boolean isFromUser, MessageType messageType, String imageUrl) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.isFromUser = isFromUser;
        this.fromCurrentUser = isFromUser; // Sincronizar ambos campos
        this.messageType = messageType;
        this.imageUrl = imageUrl;
        this.isTyping = false;
    }

    // --- Getters y Setters existentes ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFromUser() {
        return isFromUser;
    }

    public void setFromUser(boolean fromUser) {
        isFromUser = fromUser;
        this.fromCurrentUser = fromUser; // Mantener sincronización
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    // --- Nuevos getters y setters ---
    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }

    // Para compatibilidad con el código anterior
    public boolean isFromCurrentUser() {
        return fromCurrentUser;
    }

    public void setFromCurrentUser(boolean fromCurrentUser) {
        this.fromCurrentUser = fromCurrentUser;
        this.isFromUser = fromCurrentUser; // Mantener sincronización
    }

    // Métodos de utilidad
    public boolean isBotMessage() {
        return !isFromUser && senderName != null;
    }

    public boolean isUserMessage() {
        return isFromUser;
    }
}