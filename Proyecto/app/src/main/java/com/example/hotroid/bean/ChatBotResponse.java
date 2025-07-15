package com.example.hotroid.bean;

public class ChatBotResponse {
    private String messageId;
    private String content;
    private String type; // "text", "options", "data"
    private String timestamp;
    private boolean isFromBot;

    public ChatBotResponse() {}

    public ChatBotResponse(String content, String type, boolean isFromBot) {
        this.content = content;
        this.type = type;
        this.isFromBot = isFromBot;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        this.messageId = "msg_" + timestamp;
    }

    // Getters y setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isFromBot() { return isFromBot; }
    public void setFromBot(boolean fromBot) { isFromBot = fromBot; }
}