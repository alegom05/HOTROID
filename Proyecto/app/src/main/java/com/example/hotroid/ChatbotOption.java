package com.example.hotroid;

public class ChatbotOption {
    private String optionId;
    private String title;
    private String description;
    private int optionNumber;

    public ChatbotOption() {
        // Constructor vacío necesario para Firestore
    }

    public ChatbotOption(String optionId, String title, String description, int optionNumber) {
        this.optionId = optionId;
        this.title = title;
        this.description = description;
        this.optionNumber = optionNumber;
    }

    // Getters y Setters
    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOptionNumber() {
        return optionNumber;
    }

    public void setOptionNumber(int optionNumber) {
        this.optionNumber = optionNumber;
    }
}