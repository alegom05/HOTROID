package com.example.hotroid;

public class Hotel {
    private String name;
    private float rating;
    private String price;
    private int imageResourceId;  // ID del recurso drawable para la imagen

    public Hotel(String name, float rating, String price, int imageResourceId) {
        this.name = name;
        this.rating = rating;
        this.price = price;
        this.imageResourceId = imageResourceId;
    }

    public String getName() {
        return name;
    }

    public float getRating() {
        return rating;
    }

    public String getPrice() {
        return price;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}